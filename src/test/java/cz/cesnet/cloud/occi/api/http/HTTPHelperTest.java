package cz.cesnet.cloud.occi.api.http;

import java.net.URI;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;

/**
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class HTTPHelperTest {

    Header[] headers;
    String uri;
    URI uuri;
    CloseableHttpClient client;
    HttpContext context;
    int status;
    HttpHost target;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8123);

    @Before
    public void setUp() {
        headers = new Header[2];
        headers[0] = new BasicHeader(HttpHeaders.ACCEPT, "text/plain");
        headers[1] = new BasicHeader(HttpHeaders.CONTENT_TYPE, "text/plain");
        uri = "http://some.nonexisting.uri.net";
        uuri = URI.create(uri);
        client = HttpClients.createDefault();
        context = HttpClientContext.create();
        status = HttpStatus.SC_ACCEPTED;
        target = new HttpHost("localhost", 8123, "http");
    }

    @Test
    public void testPrepareGetWithStringAndHeaders() {
        HttpGet message = HTTPHelper.prepareGet(uri, headers);

        assertArrayEquals(headers, message.getAllHeaders());
        assertEquals(uri, message.getURI().toString());
    }

    @Test
    public void testPrepareGetWithString() {
        HttpGet message = HTTPHelper.prepareGet(uri);

        assertEquals(0, message.getAllHeaders().length);
        assertEquals(uri, message.getURI().toString());
    }

    @Test
    public void testPrepareHeadWithStringAndHeaders() {
        HttpHead message = HTTPHelper.prepareHead(uri, headers);

        assertArrayEquals(headers, message.getAllHeaders());
        assertEquals(uri, message.getURI().toString());
    }

    @Test
    public void testPrepareHeadWithString() {
        HttpHead message = HTTPHelper.prepareHead(uri);

        assertEquals(0, message.getAllHeaders().length);
        assertEquals(uri, message.getURI().toString());
    }

    @Test
    public void testPrepareGetWithURIAndHeaders() {
        HttpGet message = HTTPHelper.prepareGet(uuri, headers);

        assertArrayEquals(headers, message.getAllHeaders());
        assertEquals(uuri, message.getURI());
    }

    @Test
    public void testPrepareGetWithURI() {
        HttpGet message = HTTPHelper.prepareGet(uuri);

        assertEquals(0, message.getAllHeaders().length);
        assertEquals(uuri, message.getURI());
    }

    @Test
    public void testPrepareHeadWithURIAndHeaders() {
        HttpHead message = HTTPHelper.prepareHead(uuri, headers);

        assertArrayEquals(headers, message.getAllHeaders());
        assertEquals(uuri, message.getURI());
    }

    @Test
    public void testPrepareHeadWithURI() {
        HttpHead message = HTTPHelper.prepareHead(uuri);

        assertEquals(0, message.getAllHeaders().length);
        assertEquals(uuri, message.getURI());
    }

    @Test
    public void testPrepareDeleteWithStringAndHeaders() {
        HttpDelete message = HTTPHelper.prepareDelete(uri, headers);

        assertArrayEquals(headers, message.getAllHeaders());
        assertEquals(uri, message.getURI().toString());
    }

    @Test
    public void testPrepareDeleteWithString() {
        HttpDelete message = HTTPHelper.prepareDelete(uri);

        assertEquals(0, message.getAllHeaders().length);
        assertEquals(uri, message.getURI().toString());
    }

    @Test
    public void testPrepareDeleteWithURIAndHeaders() {
        HttpDelete message = HTTPHelper.prepareDelete(uuri, headers);

        assertArrayEquals(headers, message.getAllHeaders());
        assertEquals(uuri, message.getURI());
    }

    @Test
    public void testPrepareDeleteWithURI() {
        HttpDelete message = HTTPHelper.prepareDelete(uuri);

        assertEquals(0, message.getAllHeaders().length);
        assertEquals(uuri, message.getURI());
    }

    @Test
    public void testPreparePostWithStringAndHeaders() {
        HttpPost message = HTTPHelper.preparePost(uri, headers);

        assertArrayEquals(headers, message.getAllHeaders());
        assertEquals(uri, message.getURI().toString());
    }

    @Test
    public void testPreparePostWithString() {
        HttpPost message = HTTPHelper.preparePost(uri);

        assertEquals(0, message.getAllHeaders().length);
        assertEquals(uri, message.getURI().toString());
    }

    @Test
    public void testPreparePostWithURIAndHeaders() {
        HttpPost message = HTTPHelper.preparePost(uuri, headers);

        assertArrayEquals(headers, message.getAllHeaders());
        assertEquals(uuri, message.getURI());
    }

    @Test
    public void testPreparePostWithURI() {
        HttpPost message = HTTPHelper.preparePost(uuri);

        assertEquals(0, message.getAllHeaders().length);
        assertEquals(uuri, message.getURI());
    }

    @Test
    public void testRunRequestWithStatus() throws Exception {
        HttpRequest httpRequest = HTTPHelper.prepareGet("/differentcode/", headers);
        CloseableHttpResponse response = HTTPHelper.runRequest(httpRequest, target, client, context, status);

        assertNotNull(response);
        assertEquals(HttpStatus.SC_ACCEPTED, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidRunRequestWithStatus() throws Exception {
        HttpRequest httpRequest = HTTPHelper.prepareGet("/xyz/", headers);
        try {
            CloseableHttpResponse response = HTTPHelper.runRequest(httpRequest, target, client, context, status);
        } catch (CommunicationException ex) {
            //cool
        }

        target = new HttpHost("nonexisting", 8123, "http");
        try {
            CloseableHttpResponse response = HTTPHelper.runRequest(httpRequest, target, client, context, status);
        } catch (CommunicationException ex) {
            //cool
        }
    }

    @Test
    public void testRunRequestWithoutStatus() throws Exception {
        HttpRequest httpRequest = HTTPHelper.prepareGet("/", headers);
        CloseableHttpResponse response = HTTPHelper.runRequest(httpRequest, target, client, context);

        assertNotNull(response);
        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
    }

    @Test
    public void testInvalidRunRequestWithoutStatus() throws Exception {
        HttpRequest httpRequest = HTTPHelper.prepareGet("/xyz/", headers);
        try {
            CloseableHttpResponse response = HTTPHelper.runRequest(httpRequest, target, client, context);
        } catch (CommunicationException ex) {
            //cool
        }

        target = new HttpHost("nonexisting", 8123, "http");
        try {
            CloseableHttpResponse response = HTTPHelper.runRequest(httpRequest, target, client, context);
        } catch (CommunicationException ex) {
            //cool
        }
    }

    @Test
    public void testRunRequestForStatusWithStatus() throws Exception {
        HttpRequest httpRequest = HTTPHelper.prepareGet("/differentcode/", headers);
        boolean isOk = HTTPHelper.runRequestForStatus(httpRequest, target, client, context, status);
        assertTrue(isOk);

        httpRequest = HTTPHelper.prepareGet("/", headers);
        isOk = HTTPHelper.runRequestForStatus(httpRequest, target, client, context, status);
        assertFalse(isOk);
    }

    @Test
    public void testRunRequestForStatusWithoutStatus() throws Exception {
        HttpRequest httpRequest = HTTPHelper.prepareGet("/", headers);
        boolean isOk = HTTPHelper.runRequestForStatus(httpRequest, target, client, context);
        assertTrue(isOk);

        httpRequest = HTTPHelper.prepareGet("/differentcode/", headers);
        isOk = HTTPHelper.runRequestForStatus(httpRequest, target, client, context);
        assertFalse(isOk);
    }
}
