package cz.cesnet.cloud.occi.api.http.auth;

import com.owlike.genson.stream.JsonReader;
import com.owlike.genson.stream.JsonWriter;
import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.exception.AuthenticationException;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.HTTPConnection;
import cz.cesnet.cloud.occi.api.http.HTTPHelper;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String path = keystoneURI.getPath();
        if (path == null || path.isEmpty() || path.equals("/")) {
            path = PATH_DEFAULT;
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

            return HTTPHelper.runRequestReturnResponseBody(httpPost, target, client, context);
        } catch (UnsupportedEncodingException ex) {
            throw new CommunicationException(ex);
        }
    }

    private String getTenants(HttpHost target, String path, CloseableHttpClient client, HttpContext context) throws CommunicationException {
        HttpGet httpGet = HTTPHelper.prepareGet(path + "/tenants", getHeaders());
        return HTTPHelper.runRequestReturnResponseBody(httpGet, target, client, context);
    }

    private void tryTenants(String json, HttpHost target, String path, CloseableHttpClient client, HttpContext context) throws AuthenticationException {
        try (JsonReader reader = new JsonReader(json)) {
            reader.beginObject();
            while (reader.hasNext()) {
                reader.next();
                if (!reader.name().equals("tenants")) {
                    reader.skipValue();
                    continue;
                }

                reader.beginArray();
                while (reader.hasNext()) {
                    reader.next();
                    reader.beginObject();
                    while (reader.hasNext()) {
                        reader.next();
                        if (!reader.name().equals("name")) {
                            reader.skipValue();
                            continue;
                        }

                        String tenant = reader.valueAsString();
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
        }
    }

    private String getRequestBody(String tenant) throws AuthenticationException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
        writer.beginObject();
        writer.writeName("auth");
        writer.beginObject();

        String identifier = originalAuthentication.getIdentifier();
        switch (identifier) {
            case X509Authentication.IDENTIFIER: {
                writer.writeBoolean("voms", true);
            }
            break;
            case BasicAuthentication.IDENTIFIER:
            case DigestAuthentication.IDENTIFIER: {
                BasicAuthentication ba = (BasicAuthentication) originalAuthentication;
                writer.writeName("passwordCredentials");
                writer.beginObject();
                writer.writeString("username", ba.getUsername());
                writer.writeString("password", ba.getPassword());
                writer.endObject();
            }
            break;
            default:
                throw new AuthenticationException("unknown original authentication method");
        }

        if (tenant != null) {
            writer.writeString("tenantName", tenant);
        }
        writer.endObject();
        writer.endObject();
        writer.close();

        return out.toString();
    }

    private String parseId(String json) {
        try (JsonReader reader = new JsonReader(json)) {
            String id = null;
            reader.beginObject();
            while (reader.hasNext()) {
                reader.next();
                if (!reader.name().equals("access")) {
                    reader.skipValue();
                    continue;
                }

                reader.beginObject();
                while (reader.hasNext()) {
                    reader.next();
                    if (!reader.name().equals("token")) {
                        reader.skipValue();
                        continue;
                    }

                    reader.beginObject();
                    while (reader.hasNext()) {
                        reader.next();
                        if (!reader.name().equals("id")) {
                            reader.skipValue();
                            continue;
                        }

                        id = reader.valueAsString();
                        break;
                    }
                    break;
                }
                break;
            }

            return id;
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

        headers[0] = new BasicHeader(HTTP.CONTENT_TYPE, "application/json");
        headers[1] = new BasicHeader("Accept", "application/json");

        return headers;
    }
}
