package cz.cesnet.cloud.occi.api.http;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HttpContext;

/**
 * Class containing context of HTTP connections.
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class HTTPConnection {

    private CloseableHttpClient client = null;
    private HttpContext context = HttpClientContext.create();
    private List<Header> headers = new ArrayList<>();

    public CloseableHttpClient getClient() {
        return client;
    }

    public void setClient(CloseableHttpClient client) {
        this.client = client;
    }

    public HttpContext getContext() {
        return context;
    }

    public void setContext(HttpContext context) {
        this.context = context;
    }

    public Header[] getHeaders() {
        return headers.toArray(new Header[0]);
    }

    /**
     * Adds header that will be used in HTTP requests. If connection already
     * have the header set, its value will be replaced.
     *
     * @param header
     */
    public void addHeader(Header header) {
        for (Header h : headers) {
            if (h.getName().equals(header.getName())) {
                headers.remove(h);
            }
        }

        headers.add(header);
    }

    /**
     * Removes all headers from connection.
     */
    public void clearHeaders() {
        headers = new ArrayList<>();
    }

    /**
     * Sets headers 'Content-type' and 'Accept' to given media type.
     *
     * @param mediaType media type
     */
    public void setMediaType(String mediaType) {
        addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, mediaType));
        addHeader(new BasicHeader(HttpHeaders.ACCEPT, mediaType));
    }
}
