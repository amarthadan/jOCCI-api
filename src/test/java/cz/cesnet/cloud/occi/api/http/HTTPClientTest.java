package cz.cesnet.cloud.occi.api.http;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import cz.cesnet.cloud.occi.api.http.auth.BasicAuthentication;
import cz.cesnet.cloud.occi.api.http.auth.NoAuthentication;
import java.net.URI;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import org.junit.Rule;

/**
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class HTTPClientTest {

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(8123);

    HTTPClient client;

    @Before
    public void setUp() throws Exception {
        client = new HTTPClient(URI.create("http://localhost:8123"), null, "text/plain", false);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testFullConstructor() throws Exception {
        HTTPClient client = new HTTPClient(URI.create("http://localhost:8123"), null, "text/plain", true);

        assertEquals(client.getMediaType(), "text/plain");
        assertTrue(client.getAuthentication() instanceof NoAuthentication);
        assertTrue(client.isConnected());

        client = new HTTPClient(URI.create("http://localhost:8123"), new BasicAuthentication("username", "password"), "text/occi", false);

        assertEquals(client.getMediaType(), "text/occi");
        assertTrue(client.getAuthentication() instanceof BasicAuthentication);
        assertFalse(client.isConnected());
    }

    @Test
    public void testPartialConstructor() throws Exception {
        HTTPClient client = new HTTPClient(URI.create("http://localhost:8123"), new NoAuthentication());

        assertEquals(client.getMediaType(), "text/plain");
        assertTrue(client.getAuthentication() instanceof NoAuthentication);
        assertTrue(client.isConnected());
    }

    @Test
    public void testMinimalConstructor() throws Exception {
        HTTPClient client = new HTTPClient(URI.create("http://localhost:8123"));

        assertEquals(client.getMediaType(), "text/plain");
        assertTrue(client.getAuthentication() instanceof NoAuthentication);
        assertFalse(client.isConnected());
    }

    @Test
    public void testSetMediaType() {
        client.setMediaType("xyz/uvw");
        assertEquals(client.getMediaType(), "xyz/uvw");
    }

    @Test
    public void testConnect() throws Exception {
        client.connect();

        assertTrue(client.isConnected());
    }

    @Test
    public void testList() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testListWithString() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testListWithURI() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testDescribe() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testDescribeWithString() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testDescribeWithURI() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testCreate() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testDeleteWithString() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testDeleteWithURI() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testTriggerWithString_ActionInstance() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testTriggerWithURI_ActionInstance() throws Exception {
        fail("The test case is a prototype.");
    }

    @Test
    public void testRefresh() throws Exception {
        fail("The test case is a prototype.");
    }

}
