package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.exception.AuthenticationException;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.HTTPHelper;
import java.io.IOException;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HTTPAuthentication implements Authentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPAuthentication.class);
    private HttpHost target;
    private HttpClientContext context;
    private CredentialsProvider credentialsProvider;

    public HttpHost getTarget() {
        return target;
    }

    public void setTarget(HttpHost target) {
        this.target = target;
    }

    public HttpClientContext getContext() {
        return context;
    }

    public void setContext(HttpClientContext context) {
        this.context = context;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    @Override
    public abstract String getIdentifier();

    @Override
    public abstract Authentication getFallback();

    @Override
    public void authenticate() throws CommunicationException {
        createContextIfNotExists();
        LOGGER.debug("Running authentication...");
        try {
            try (CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .build()) {
                HttpHead httpHead = HTTPHelper.prepareHead(Client.MODEL_URI);
                LOGGER.debug("Executing request {} to target {}", httpHead.getRequestLine(), target);
                try (CloseableHttpResponse response = httpclient.execute(target, httpHead, context)) {
                    LOGGER.debug("Response: {}\nHeaders: {}", response.getStatusLine().toString(), response.getAllHeaders());
                    if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                        throw new AuthenticationException(response.getStatusLine().toString());
                    }
                }
            }
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    private void createContextIfNotExists() {
        if (context == null) {
            context = HttpClientContext.create();
        }
    }
}
