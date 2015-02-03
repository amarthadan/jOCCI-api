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
import cz.cesnet.cloud.occi.exception.RenderingException;
import cz.cesnet.cloud.occi.parser.CollectionType;
import cz.cesnet.cloud.occi.parser.MediaType;
import cz.cesnet.cloud.occi.parser.TextParser;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HTTPClient extends Client {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPClient.class);
    private static final String ACTION_URL_PARAMETER = "?action=";
    private final MediaType mediaType = MediaType.TEXT_PLAIN;
    private final HTTPConnection connection = new HTTPConnection();
    private HttpHost target;
    private final TextParser parser = new TextParser();

    public HTTPClient(URI endpoint, Authentication authentication, boolean autoconnect) throws CommunicationException {
        //to avoid SSL handshake unrecognized_name error
        System.setProperty("jsse.enableSNIExtension", "false");

        if (endpoint == null) {
            throw new NullPointerException("endpoint cannot be null");
        }
        if (authentication == null) {
            authentication = new NoAuthentication();
        }

        setEndpoint(endpoint);
        target = new HttpHost(endpoint.getHost(), endpoint.getPort(), endpoint.getScheme());
        setAuthentication(authentication);

        connection.addHeader(new BasicHeader(HTTP.CONTENT_TYPE, "text/plain"));
        connection.addHeader(new BasicHeader("Accept", "text/plain"));

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
        Authentication auth = getAuthentication();
        if (!(auth instanceof HTTPAuthentication)) {
            throw new CommunicationException("authentication method '" + auth + "' is not a valid HTTP authentication method");
        }

        HTTPAuthentication httpAuth = (HTTPAuthentication) auth;
        httpAuth.setTarget(target);
        httpAuth.setConnection(connection);
        httpAuth.authenticate();

        setConnected(true);
        obtainModel();
    }

    private void checkConnection() throws CommunicationException {
        if (!isConnected()) {
            connect();
        }
    }

    private void obtainModel() throws CommunicationException {
        try {
            LOGGER.debug("Obtaining model...");
            checkConnection();
            HttpGet httpGet = HTTPHelper.prepareGet(Client.MODEL_URI, connection.getHeaders());
            String modelString = HTTPHelper.runRequestReturnResponseBody(httpGet, target, connection.getClient(), connection.getContext());
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
            httpGet = HTTPHelper.prepareGet("/", connection.getHeaders());
        } else {
            Kind kind;
            try {
                kind = getModel().findKind(resourceType);
            } catch (AmbiguousIdentifierException ex) {
                throw new CommunicationException(ex);
            }
            if (kind == null) {
                throw new CommunicationException("unknown resource type '" + resourceType + "'");
            }
            httpGet = HTTPHelper.prepareGet(kind.getLocation(), connection.getHeaders());
        }

        return runListGet(httpGet);
    }

    @Override
    public List<URI> list(URI resourceIdentifier) throws CommunicationException {
        Kind kind = getModel().findKind(resourceIdentifier);
        if (kind == null) {
            throw new CommunicationException("unknown resource identifier '" + resourceIdentifier + "'");
        }
        HttpGet httpGet = HTTPHelper.prepareGet(kind.getLocation(), connection.getHeaders());
        return runListGet(httpGet);
    }

    private List<URI> runListGet(HttpGet httpGet) throws CommunicationException {
        try {
            checkConnection();
            String locationsString = HTTPHelper.runRequestReturnResponseBody(httpGet, target, connection.getClient(), connection.getContext());
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
            Kind kind = model.findKind(resourceType);
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
        Kind kind = model.findKind(resourceIdentifier);
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
        location = getFullUri(location);
        String path = location.getPath();
        String[] segments = TextParser.divideUriByLastSegment(path);
        CollectionType type = getModel().findKindType(segments[1]);
        if (type == null) {
            throw new CommunicationException("unknown resource identifier '" + location + "'");
        }

        HttpGet httpGet = HTTPHelper.prepareGet(location, connection.getHeaders());
        return runDescribeGet(httpGet, type);
    }

    private List<Entity> describe(List<URI> locations, CollectionType type) throws CommunicationException {
        Collection collection = new Collection();
        for (URI location : locations) {
            HttpGet httpGet = HTTPHelper.prepareGet(location, connection.getHeaders());
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
            checkConnection();
            String entityString = HTTPHelper.runRequestReturnResponseBody(httpGet, target, connection.getClient(), connection.getContext());
            Collection collection = parser.parseCollection(mediaType, entityString, null, type);
            LOGGER.debug("Collection: {}", collection);
            return collection;
        } catch (ParsingException ex) {
            throw new CommunicationException(ex);
        }
    }

    @Override
    public URI create(Entity entity) throws CommunicationException {
        Kind kind = entity.getKind();
        if (kind == null) {
            throw new CommunicationException("entity with empty kind");
        }

        HttpPost httpPost = HTTPHelper.preparePost(kind.getLocation(), connection.getHeaders());
        try {
            HttpEntity httpEntity = new StringEntity(entity.toText());
            httpPost.setEntity(httpEntity);

            checkConnection();
            String responseString = HTTPHelper.runRequestReturnResponseBody(httpPost, target, connection.getClient(), connection.getContext(), HttpStatus.SC_CREATED);
            List<URI> locations = parser.parseLocations(mediaType, responseString, null);
            if (locations == null || locations.isEmpty()) {
                throw new CommunicationException("no location returned");
            }

            return locations.get(0);
        } catch (UnsupportedEncodingException | RenderingException | ParsingException ex) {
            throw new CommunicationException(ex);
        }
    }

    @Override
    public boolean delete(String resourceType) throws CommunicationException {
        Kind kind;
        try {
            kind = getModel().findKind(resourceType);
        } catch (AmbiguousIdentifierException ex) {
            throw new CommunicationException(ex);
        }
        if (kind == null) {
            throw new CommunicationException("unknown resource type '" + resourceType + "'");
        }
        HttpDelete httpDelete = HTTPHelper.prepareDelete(kind.getLocation(), connection.getHeaders());

        checkConnection();
        return HTTPHelper.runRequestReturnStatus(httpDelete, target, connection.getClient(), connection.getContext());
    }

    @Override
    public boolean delete(URI resourceIdentifier) throws CommunicationException {
        Kind kind = getModel().findKind(resourceIdentifier);
        HttpDelete httpDelete;
        if (kind != null) {
            httpDelete = HTTPHelper.prepareDelete(kind.getLocation(), connection.getHeaders());
        } else {
            resourceIdentifier = getFullUri(resourceIdentifier);
            httpDelete = HTTPHelper.prepareDelete(resourceIdentifier, connection.getHeaders());
        }

        checkConnection();
        return HTTPHelper.runRequestReturnStatus(httpDelete, target, connection.getClient(), connection.getContext());
    }

    @Override
    public boolean trigger(String resourceType, ActionInstance action) throws CommunicationException {
        Kind kind;
        try {
            kind = getModel().findKind(resourceType);
        } catch (AmbiguousIdentifierException ex) {
            throw new CommunicationException(ex);
        }
        if (kind == null) {
            throw new CommunicationException("unknown resource type '" + resourceType + "'");
        }

        try {
            String url = kind.getLocation().toString() + ACTION_URL_PARAMETER + action.getAction().getTerm();
            HttpEntity httpEntity = new StringEntity(action.toText());
            HttpPost httpPost = HTTPHelper.preparePost(url, connection.getHeaders());
            httpPost.setEntity(httpEntity);

            checkConnection();
            return HTTPHelper.runRequestReturnStatus(httpPost, target, connection.getClient(), connection.getContext());
        } catch (UnsupportedEncodingException ex) {
            throw new CommunicationException(ex);
        }
    }

    @Override
    public boolean trigger(URI resourceIdentifier, ActionInstance action) throws CommunicationException {
        Kind kind = getModel().findKind(resourceIdentifier);
        String url;
        if (kind != null) {
            url = kind.getLocation().toString() + ACTION_URL_PARAMETER + action.getAction().getTerm();
        } else {
            resourceIdentifier = getFullUri(resourceIdentifier);
            url = resourceIdentifier.toString() + ACTION_URL_PARAMETER + action.getAction().getTerm();
        }

        try {
            HttpEntity httpEntity = new StringEntity(action.toText());
            HttpPost httpPost = HTTPHelper.preparePost(url, connection.getHeaders());
            httpPost.setEntity(httpEntity);

            checkConnection();
            return HTTPHelper.runRequestReturnStatus(httpPost, target, connection.getClient(), connection.getContext());
        } catch (UnsupportedEncodingException ex) {
            throw new CommunicationException(ex);
        }
    }

    private URI getFullUri(URI uri) throws CommunicationException {
        if (uri.getHost() == null) {
            try {
                uri = new URI(getEndpoint().toString() + uri.toString()).normalize();
            } catch (URISyntaxException ex) {
                throw new CommunicationException(ex);
            }
        }

        return uri;
    }

    @Override
    public void refresh() throws CommunicationException {
        obtainModel();
    }
}
