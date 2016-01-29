package cz.cesnet.cloud.occi.api.http.auth;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.exception.AuthenticationException;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.HTTPConnection;
import cz.cesnet.cloud.occi.api.http.HTTPHelper;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing OpenStack's Keystone HTTP authentication method.
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class KeystoneAuthentication extends HTTPAuthentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(KeystoneAuthentication.class);
    public static final String IDENTIFIER = "OCCIKeystoneAuthentication";
    private static final String HEADER_AUTH = "Www-Authenticate";
    private static final String HEADER_X_AUTH_TOKEN = "X-Auth-Token";
    private static final String GROUP_URI = "uri";
    private static final String REGEXP_KEYSTONE_URI = "^(?:Keystone|snf-auth) uri='(?<" + GROUP_URI + ">.+)'$";
    private static final Pattern PATTERN_KEYSTONE_URI = Pattern.compile(REGEXP_KEYSTONE_URI);
    private static final String PATH_DEFAULT = "/v2.0";
    private final HTTPAuthentication originalAuthentication;
    private CloseableHttpResponse originalResponse = null;
    private String authToken = null;

    public KeystoneAuthentication(HTTPAuthentication originalAuthentication) {
        this.originalAuthentication = originalAuthentication;
    }

    public CloseableHttpResponse getOriginalResponse() {
        return originalResponse;
    }

    public void setOriginalResponse(CloseableHttpResponse response) {
        this.originalResponse = response;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Authentication getFallback() {
        return null;
    }

    private void checkResponse() throws AuthenticationException {
        if (originalResponse == null) {
            throw new AuthenticationException("no response to react to");
        }

        if (!originalResponse.containsHeader(HEADER_AUTH)) {
            throw new AuthenticationException("missing '" + HEADER_AUTH + "' header");
        }
    }

    @Override
    public void authenticate() throws CommunicationException {
        checkResponse();

        Matcher matcher = PATTERN_KEYSTONE_URI.matcher(originalResponse.getFirstHeader(HEADER_AUTH).getValue());
        if (!matcher.find()) {
            throw new AuthenticationException("incorrect " + HEADER_AUTH + " content");
        }

        URI keystoneURI = URI.create(matcher.group(GROUP_URI));
        HttpHost target = new HttpHost(keystoneURI.getHost(), keystoneURI.getPort(), keystoneURI.getScheme());
        //TODO
        //this path normalization should be handled in a better way
        String path = keystoneURI.getPath();
        if (path == null) {
            path = "";
        }

        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        if (!path.endsWith(PATH_DEFAULT)) {
            path = path + PATH_DEFAULT;
        }

        HTTPConnection connection = originalAuthentication.getConnection();
        CloseableHttpClient client = connection.getClient();
        HttpContext context = connection.getContext();

        String response = authenticateAgainstKeystone(target, path, client, context, null);
        authToken = parseId(response);
        response = getTenants(target, path, client, context);
        tryTenants(response, target, path, client, context);

        LOGGER.debug("Scoped token: " + authToken);
        connection.addHeader(new BasicHeader(HEADER_X_AUTH_TOKEN, authToken));
    }

    private String authenticateAgainstKeystone(HttpHost target, String path, CloseableHttpClient client, HttpContext context, String tenant) throws CommunicationException {
        try {
            HttpPost httpPost = HTTPHelper.preparePost(path + "/tokens", getHeaders());
            httpPost.setEntity(new StringEntity(getRequestBody(tenant)));

            try (CloseableHttpResponse response = HTTPHelper.runRequest(httpPost, target, client, context)) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    private String getTenants(HttpHost target, String path, CloseableHttpClient client, HttpContext context) throws CommunicationException {
        try {
            HttpGet httpGet = HTTPHelper.prepareGet(path + "/tenants", getHeaders());
            try (CloseableHttpResponse response = HTTPHelper.runRequest(httpGet, target, client, context)) {
                return EntityUtils.toString(response.getEntity());
            }
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    private void tryTenants(String json, HttpHost target, String path, CloseableHttpClient client, HttpContext context) throws AuthenticationException, CommunicationException {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (!name.equals("tenants")) {
                    reader.skipValue();
                    continue;
                }

                reader.beginArray();
                while (reader.hasNext()) {
                    reader.beginObject();
                    while (reader.hasNext()) {
                        name = reader.nextName();
                        if (!name.equals("name")) {
                            reader.skipValue();
                            continue;
                        }

                        String tenant = reader.nextString();
                        try {
                            String response = authenticateAgainstKeystone(target, path, client, context, tenant);
                            authToken = parseId(response);
                            return;
                        } catch (CommunicationException ex) {
                            //ignoring and trying the next tenant
                        }
                    }
                    reader.endObject();
                }
                reader.endArray();
                throw new AuthenticationException("no suitable tenant found");
            }
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    private String getRequestBody(String tenant) throws AuthenticationException, CommunicationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (JsonWriter writer = new JsonWriter(new OutputStreamWriter(out))) {
            writer.beginObject();
            writer.name("auth");
            writer.beginObject();

            String identifier = originalAuthentication.getIdentifier();
            switch (identifier) {
                //case X509Authentication.IDENTIFIER: // not sure if should be here or not
                case VOMSAuthentication.IDENTIFIER: {
                    writer.name("voms").value(true);
                }
                break;
                case BasicAuthentication.IDENTIFIER:
                case DigestAuthentication.IDENTIFIER: {
                    BasicAuthentication ba = (BasicAuthentication) originalAuthentication;
                    writer.name("passwordCredentials");
                    writer.beginObject();
                    writer.name("username").value(ba.getUsername());
                    writer.name("password").value(ba.getPassword());
                    writer.endObject();
                }
                break;
                default:
                    throw new AuthenticationException("unknown original authentication method");
            }

            if (tenant != null) {
                writer.name("tenantName").value(tenant);
            }
            writer.endObject();
            writer.endObject();
            writer.close();

            return out.toString();
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    private String parseId(String json) throws CommunicationException {
        try (JsonReader reader = new JsonReader(new StringReader(json))) {
            String id = null;
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (!name.equals("access")) {
                    reader.skipValue();
                    continue;
                }

                reader.beginObject();
                while (reader.hasNext()) {
                    name = reader.nextName();
                    if (!name.equals("token")) {
                        reader.skipValue();
                        continue;
                    }

                    reader.beginObject();
                    while (reader.hasNext()) {
                        name = reader.nextName();
                        if (!name.equals("id")) {
                            reader.skipValue();
                            continue;
                        }

                        id = reader.nextString();
                        break;
                    }
                    break;
                }
                break;
            }

            return id;
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    private Header[] getHeaders() {
        Header[] headers;
        if (authToken != null) {
            headers = new Header[3];
            headers[2] = new BasicHeader(HEADER_X_AUTH_TOKEN, authToken);
        } else {
            headers = new Header[2];
        }

        headers[0] = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        headers[1] = new BasicHeader(HttpHeaders.ACCEPT, "application/json");

        return headers;
    }
}
