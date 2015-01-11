package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.exception.AuthenticationException;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.HTTPHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HTTPAuthentication implements Authentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPAuthentication.class);
    private HttpHost target;
    private HttpClientContext context;
    private CredentialsProvider credentialsProvider;
    private String CAPath;
    private String CAFile;

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

    public String getCAPath() {
        return CAPath;
    }

    public void setCAPath(String CAPath) {
        this.CAPath = CAPath;
    }

    public String getCAFile() {
        return CAFile;
    }

    public void setCAFile(String CAFile) {
        this.CAFile = CAFile;
    }

    @Override
    public abstract String getIdentifier();

    @Override
    public abstract Authentication getFallback();

    protected SSLContext createSSLContext() throws AuthenticationException {
        KeyStore keyStore = loadCAs();
        if (keyStore == null) {
            return null;
        }

        try {
            SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(keyStore).build();
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            throw new AuthenticationException(ex);
        }
    }

    @Override
    public void authenticate() throws CommunicationException {
        createContextIfNotExists();
        SSLContext sslContext = createSSLContext();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

        LOGGER.debug("Running authentication...");
        try {
            try (CloseableHttpClient httpclient = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setSSLSocketFactory(sslsf)
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

    protected KeyStore loadCAs() throws AuthenticationException {
        KeyStore keyStore = null;
        if (CAFile != null && !CAFile.isEmpty()) {
            keyStore = loadCAsFromFile();
        } else {
            if (CAPath != null && !CAPath.isEmpty()) {
                keyStore = loadCAsFromPath();
            }
        }

        return keyStore;
    }

    private KeyStore loadCAsFromFile() throws AuthenticationException {
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream instream = new FileInputStream(new File(CAFile));
            trustStore.load(instream, null);

            return trustStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new AuthenticationException(ex);
        }
    }

    private KeyStore loadCAsFromPath() throws AuthenticationException {
        try {
            File CADir = new File(CAPath);
            if (!CADir.isDirectory()) {
                throw new AuthenticationException("'" + CAPath + "' is not a directory.");
            }
            String[] certs = CADir.list();
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<Certificate> rootCertificates = new ArrayList<>();
            for (String cert : certs) {
                rootCertificates.addAll(cf.generateCertificates(new FileInputStream(new File(cert))));
            }

            for (Certificate cert : rootCertificates) {
                X509Certificate x509Cert = (X509Certificate) cert;
                ks.setCertificateEntry(x509Cert.getSerialNumber().toString(), x509Cert);
            }

            return ks;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new AuthenticationException(ex);
        }
    }
}
