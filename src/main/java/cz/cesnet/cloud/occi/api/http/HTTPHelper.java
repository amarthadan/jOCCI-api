package cz.cesnet.cloud.occi.api.http;

import java.net.URI;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.protocol.HTTP;

public class HTTPHelper {

    public static HttpGet prepareGet(String uri, String contentType) {
        HttpGet httpGet = new HttpGet(uri);
        prepareHeaders(httpGet, contentType);
        return httpGet;
    }

    public static HttpGet prepareGet(String uri) {
        return prepareGet(uri, "text/plain");
    }

    public static HttpHead prepareHead(String uri, String contentType) {
        HttpHead httpHead = new HttpHead(uri);
        prepareHeaders(httpHead, contentType);
        return httpHead;
    }

    public static HttpHead prepareHead(String uri) {
        return prepareHead(uri, "text/plain");
    }

    public static HttpGet prepareGet(URI uri, String contentType) {
        HttpGet httpGet = new HttpGet(uri);
        prepareHeaders(httpGet, contentType);
        return httpGet;
    }

    public static HttpGet prepareGet(URI uri) {
        return prepareGet(uri, "text/plain");
    }

    public static HttpHead prepareHead(URI uri, String contentType) {
        HttpHead httpHead = new HttpHead(uri);
        prepareHeaders(httpHead, contentType);
        return httpHead;
    }

    public static HttpHead prepareHead(URI uri) {
        return prepareHead(uri, "text/plain");
    }

    public static HttpDelete prepareDelete(String uri, String contentType) {
        HttpDelete httpDelete = new HttpDelete(uri);
        prepareHeaders(httpDelete, contentType);
        return httpDelete;
    }

    public static HttpDelete prepareDelete(String uri) {
        return prepareDelete(uri, "text/plain");
    }

    public static HttpDelete prepareDelete(URI uri, String contentType) {
        HttpDelete httpDelete = new HttpDelete(uri);
        prepareHeaders(httpDelete, contentType);
        return httpDelete;
    }

    public static HttpDelete prepareDelete(URI uri) {
        return prepareDelete(uri, "text/plain");
    }

    private static void prepareHeaders(HttpMessage httpmessage, String contentType) {
        httpmessage.setHeader(HTTP.CONTENT_TYPE, contentType);
        httpmessage.setHeader("Accept", contentType);
    }
}
