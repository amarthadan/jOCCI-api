package cz.cesnet.cloud.occi.api.http;

import cz.cesnet.cloud.occi.Collection;
import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.auth.HTTPAuthentication;
import cz.cesnet.cloud.occi.api.http.auth.NoAuthentication;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Entity;
import cz.cesnet.cloud.occi.core.Kind;
import cz.cesnet.cloud.occi.exception.AmbiguousIdentifierException;
import cz.cesnet.cloud.occi.exception.ParsingException;
import cz.cesnet.cloud.occi.parser.CollectionType;
import cz.cesnet.cloud.occi.parser.MediaType;
import cz.cesnet.cloud.occi.parser.TextParser;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPClient extends Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPClient.class);
    private final MediaType mediaType = MediaType.TEXT_PLAIN;
    private HttpClientContext context;
    private final TextParser parser = new TextParser();

    public HTTPClient(URI endpoint, Authentication authentication, boolean autoconnect) throws CommunicationException {
        if (endpoint == null) {
            throw new NullPointerException("endpoint cannot be null");
        }
        if (authentication == null) {
            authentication = new NoAuthentication();
        }

        setEndpoint(endpoint);
        setAuthentication(authentication);

        if (autoconnect) {
            connect();
        }
    }

    public HTTPClient(URI endpoint, Authentication authentication) throws CommunicationException {
        this(endpoint, authentication, true);
    }

    public HTTPClient(URI endpoint) throws CommunicationException {
        this(endpoint, null, false);
    }

    @Override
    public void connect() throws CommunicationException {
        HTTPAuthentication auth = (HTTPAuthentication) getAuthentication();
        auth.setTarget(new HttpHost(getEndpoint().getHost(), getEndpoint().getPort()));
        auth.authenticate();
        context = auth.getContext();
        setConnected(true);
        obtainModel();
    }

    private String runGet(HttpGet httpGet) throws CommunicationException {
        if (!isConnected()) {
            connect();
        }
        LOGGER.debug("Running get...");
        HttpHost target = new HttpHost(getEndpoint().getHost(), getEndpoint().getPort());
        try {
            try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
                LOGGER.debug("Executing request {} to target {}", httpGet.getRequestLine(), target);
                try (CloseableHttpResponse response = httpclient.execute(target, httpGet, context)) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    LOGGER.debug("Response: {}\nHeaders: {}\nBody: {}", response.getStatusLine().toString(), response.getAllHeaders(), responseBody);
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        throw new CommunicationException(response.getStatusLine().toString());
                    }

                    return responseBody;
                }
            }
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    private void obtainModel() throws CommunicationException {
        try {
            LOGGER.debug("Obtaining model...");
            HttpGet httpGet = HTTPHelper.prepareGet(Client.MODEL_URI);
            String modelString = runGet(httpGet);
            setModel(parser.parseModel(mediaType, modelString, null));
            LOGGER.debug("Model: {}", getModel());
        } catch (ParsingException ex) {
            throw new CommunicationException(ex);
        }
    }

    @Override
    public List<URI> list() throws CommunicationException {
        return list("");
    }

    @Override
    public List<URI> list(String resourceType) throws CommunicationException {
        HttpGet httpGet;
        if (resourceType.isEmpty()) {
            httpGet = HTTPHelper.prepareGet("/");
        } else {
            Kind kind;
            try {
                kind = getModel().findKindByTerm(resourceType);
            } catch (AmbiguousIdentifierException ex) {
                throw new CommunicationException(ex);
            }
            if (kind == null) {
                throw new CommunicationException("unknown resource type '" + resourceType + "'");
            }
            httpGet = HTTPHelper.prepareGet(kind.getLocation());
        }

        return runListGet(httpGet);
    }

    @Override
    public List<URI> list(URI resourceIdentifier) throws CommunicationException {
        Kind kind = getModel().findKindByIdentifier(resourceIdentifier);
        if (kind == null) {
            throw new CommunicationException("unknown resource identifier '" + resourceIdentifier + "'");
        }
        HttpGet httpGet = HTTPHelper.prepareGet(kind.getLocation());
        return runListGet(httpGet);
    }

    private List<URI> runListGet(HttpGet httpGet) throws CommunicationException {
        try {
            String locationsString = runGet(httpGet);
            List<URI> locations = parser.parseLocations(mediaType, locationsString, null);
            LOGGER.debug("Locations: {}", locations);
            return locations;
        } catch (ParsingException ex) {
            throw new CommunicationException(ex);
        }
    }

    @Override
    public List<Entity> describe() throws CommunicationException {
        List<URI> locations = list();
        Collection collection = new Collection();
        for (URI location : locations) {
            collection.merge(describeLocation(location));
        }

        return generateEntityListFromCollection(collection);
    }

    @Override
    public List<Entity> describe(String resourceType) throws CommunicationException {
        Model model = getModel();
        try {
            Kind kind = model.findKindByTerm(resourceType);
            if (kind == null) {
                throw new CommunicationException("unknown resource type '" + resourceType + "'");
            }
            CollectionType type = model.findKindType(kind);
            if (type == null) {
                throw new CommunicationException("unknown resource type '" + resourceType + "'");
            }

            return describe(list(resourceType), type);
        } catch (AmbiguousIdentifierException ex) {
            throw new CommunicationException(ex);
        }
    }

    @Override
    public List<Entity> describe(URI resourceIdentifier) throws CommunicationException {
        Model model = getModel();
        Kind kind = model.findKindByIdentifier(resourceIdentifier);
        if (kind != null) {
            CollectionType type = model.findKindType(kind);
            if (type == null) {
                throw new CommunicationException("unknown resource identifier '" + resourceIdentifier + "'");
            }

            return describe(list(resourceIdentifier), type);
        } else {
            Collection collection = describeLocation(resourceIdentifier);

            return generateEntityListFromCollection(collection);
        }
    }

    private Collection describeLocation(URI location) throws CommunicationException {
        if (location.getHost() == null) {
            try {
                location = new URI(getEndpoint().toString() + location.toString()).normalize();
            } catch (URISyntaxException ex) {
                throw new CommunicationException(ex);
            }
        }
        String path = location.getPath();
        String[] segments = TextParser.divideUriByLastSegment(path);
        CollectionType type = getModel().findKindType(segments[1]);
        if (type == null) {
            throw new CommunicationException("unknown resource identifier '" + location + "'");
        }

        HttpGet httpGet = HTTPHelper.prepareGet(location);
        return runDescribeGet(httpGet, type);
    }

    private List<Entity> describe(List<URI> locations, CollectionType type) throws CommunicationException {
        Collection collection = new Collection();
        for (URI location : locations) {
            HttpGet httpGet = HTTPHelper.prepareGet(location);
            collection.merge(runDescribeGet(httpGet, type));
        }

        return generateEntityListFromCollection(collection);
    }

    private List<Entity> generateEntityListFromCollection(Collection collection) {
        List<Entity> list = new ArrayList();
        list.addAll(collection.getLinks());
        list.addAll(collection.getResources());

        return list;
    }

    private Collection runDescribeGet(HttpGet httpGet, CollectionType type) throws CommunicationException {
        try {
            String entityString = runGet(httpGet);
            Collection collection = parser.parseCollection(mediaType, entityString, null, type);
            LOGGER.debug("Collection: {}", collection);
            return collection;
        } catch (ParsingException ex) {
            throw new CommunicationException(ex);
        }
    }

    @Override
    public URI create(Entity entity) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete(String resourceType) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean delete(URI resourceIdentifier) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean trigger(String resourceType, ActionInstance action) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean trigger(URI resourceIdentifier, ActionInstance action) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void refresh() throws CommunicationException {
        obtainModel();
    }
}
