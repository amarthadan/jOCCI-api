package cz.cesnet.cloud.occi.api.http;

import org.apache.http.HttpHeaders;
import org.apache.http.message.BasicHeader;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class HTTPConnectionTest {

    HTTPConnection con;

    @Before
    public void setUp() {
        con = new HTTPConnection();
        con.addHeader(new BasicHeader(HttpHeaders.AGE, "aaa"));
        con.addHeader(new BasicHeader(HttpHeaders.DEPTH, "ddd"));
        con.addHeader(new BasicHeader(HttpHeaders.FROM, "fff"));
    }

    @Test
    public void testGetHeaders() {
        assertEquals(3, con.getHeaders().length);
        assertEquals(HttpHeaders.AGE, con.getHeaders()[0].getName());
        assertEquals("aaa", con.getHeaders()[0].getValue());
        assertEquals(HttpHeaders.DEPTH, con.getHeaders()[1].getName());
        assertEquals("ddd", con.getHeaders()[1].getValue());
        assertEquals(HttpHeaders.FROM, con.getHeaders()[2].getName());
        assertEquals("fff", con.getHeaders()[2].getValue());
    }

    @Test
    public void testAddHeader() {
        con.addHeader(new BasicHeader(HttpHeaders.TE, "ttt"));

        assertEquals(4, con.getHeaders().length);
        assertEquals(HttpHeaders.AGE, con.getHeaders()[0].getName());
        assertEquals("aaa", con.getHeaders()[0].getValue());
        assertEquals(HttpHeaders.DEPTH, con.getHeaders()[1].getName());
        assertEquals("ddd", con.getHeaders()[1].getValue());
        assertEquals(HttpHeaders.FROM, con.getHeaders()[2].getName());
        assertEquals("fff", con.getHeaders()[2].getValue());
        assertEquals(HttpHeaders.TE, con.getHeaders()[3].getName());
        assertEquals("ttt", con.getHeaders()[3].getValue());
    }

    @Test
    public void testClearHeaders() {
        assertEquals(3, con.getHeaders().length);
        con.clearHeaders();
        assertEquals(0, con.getHeaders().length);
    }

    @Test
    public void testSetMediaType() {
        assertEquals(3, con.getHeaders().length);
        con.setMediaType("mediaType");
        assertEquals(5, con.getHeaders().length);
        assertEquals(HttpHeaders.CONTENT_TYPE, con.getHeaders()[3].getName());
        assertEquals("mediaType", con.getHeaders()[3].getValue());
        assertEquals(HttpHeaders.ACCEPT, con.getHeaders()[4].getName());
        assertEquals("mediaType", con.getHeaders()[4].getValue());
    }
}
