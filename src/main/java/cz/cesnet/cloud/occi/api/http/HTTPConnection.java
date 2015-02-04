package cz.cesnet.cloud.occi.api.http;

import java.util.ArrayList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

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

    public void addHeader(Header header) {
        for (Header h : headers) {
            if (h.getName().equals(header.getName())) {
                headers.remove(h);
            }
        }

        headers.add(header);
    }

    public void clearHeaders() {
        headers = new ArrayList<>();
    }
}
