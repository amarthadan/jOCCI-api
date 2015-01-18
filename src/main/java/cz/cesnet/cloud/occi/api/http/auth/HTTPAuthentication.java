package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.exception.AuthenticationException;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.HTTPHelper;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class HTTPAuthentication implements Authentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPAuthentication.class);
    private HttpHost target;
    private CloseableHttpClient client;
    private CredentialsProvider credentialsProvider;
    private String CAPath;
    private String CAFile;

    public HttpHost getTarget() {
        return target;
    }

    public void setTarget(HttpHost target) {
        this.target = target;
    }

    public CloseableHttpClient getClient() {
        return client;
    }

    public void setClient(CloseableHttpClient client) {
        this.client = client;
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
        Security.addProvider(new BouncyCastleProvider());
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
        //createContextIfNotExists();
        HttpClientContext context = HttpClientContext.create();
        SSLContext sslContext = createSSLContext();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

        LOGGER.debug("Running authentication...");
        try {
            client = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setSSLSocketFactory(sslsf)
                    .build();
            HttpHead httpHead = HTTPHelper.prepareHead(Client.MODEL_URI);
            LOGGER.debug("Executing request {} to target {}", httpHead.getRequestLine(), target);
            try (CloseableHttpResponse response = client.execute(target, httpHead, context)) {
                LOGGER.debug("Response: {}\nHeaders: {}", response.getStatusLine().toString(), response.getAllHeaders());
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    throw new AuthenticationException(response.getStatusLine().toString());
                }

            }
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

//    private void createContextIfNotExists() {
//        if (context == null) {
//            context = HttpClientContext.create();
//        }
//    }
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

            FilenameFilter fileNameFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    if (name.lastIndexOf('.') > 0) {
                        int lastIndex = name.lastIndexOf('.');
                        String str = name.substring(lastIndex);
                        if (str.equals(".pem")) {
                            return true;
                        }
                    }
                    return false;
                }
            };

            File[] certs = CADir.listFiles(fileNameFilter);
            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<Certificate> rootCertificates = new ArrayList<>();
            PEMReader reader;
            for (File cert : certs) {
                reader = new PEMReader(new InputStreamReader(new FileInputStream(cert)));
                rootCertificates.add((X509Certificate) reader.readObject());
            }

            for (Certificate cert : rootCertificates) {
                X509Certificate x509Cert = (X509Certificate) cert;
                ks.setCertificateEntry(x509Cert.getSubjectX500Principal().getName(), x509Cert);
                LOGGER.debug("adding certificate: " + x509Cert.getSubjectX500Principal().getName());
            }

            return ks;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new AuthenticationException(ex);
        }
    }
}
