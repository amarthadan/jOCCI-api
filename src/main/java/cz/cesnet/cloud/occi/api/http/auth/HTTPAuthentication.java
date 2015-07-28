package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.Client;
import cz.cesnet.cloud.occi.api.exception.AuthenticationException;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.api.http.HTTPConnection;
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
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import javax.net.ssl.SSLContext;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContexts;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.util.EntityUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract class representing HTTP authentication methods. Lets set either
 * directory path or file containing CAs and uses them during establishing of
 * connection.
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public abstract class HTTPAuthentication implements Authentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(HTTPAuthentication.class);
    private HttpHost target;
    private HTTPConnection connection;
    private CredentialsProvider credentialsProvider;
    private String CAPath;
    private String CAFile;

    /**
     * Returns server that authentication is run against.
     *
     * @return
     */
    public HttpHost getTarget() {
        return target;
    }

    /**
     * Sets server to run authentication against.
     *
     * @param target server
     */
    public void setTarget(HttpHost target) {
        this.target = target;
    }

    public HTTPConnection getConnection() {
        return connection;
    }

    public void setConnection(HTTPConnection connection) {
        this.connection = connection;
    }

    public CredentialsProvider getCredentialsProvider() {
        return credentialsProvider;
    }

    public void setCredentialsProvider(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
    }

    /**
     * Returns path to the custom CA directory.
     *
     * @return path to the custom CA directory
     */
    public String getCAPath() {
        return CAPath;
    }

    /**
     * Sets path to custom CA directory.
     *
     * @param CAPath path to custom CA directory
     */
    public void setCAPath(String CAPath) {
        this.CAPath = CAPath;
    }

    /**
     * Returns path to custom CA file.
     *
     * @return path to custom CA file
     */
    public String getCAFile() {
        return CAFile;
    }

    /**
     * Sets path to custom CA file
     *
     * @param CAFile path to custom CA file
     */
    public void setCAFile(String CAFile) {
        this.CAFile = CAFile;
    }

    @Override
    public abstract String getIdentifier();

    @Override
    public abstract Authentication getFallback();

    /**
     * Creates a ssl context with custom CAs if set.
     *
     * @return ssl context
     * @throws AuthenticationException
     */
    protected SSLContext createSSLContext() throws AuthenticationException {
        Security.addProvider(new BouncyCastleProvider());
        KeyStore keyStore = loadCAs();

        try {
            SSLContext sslContext;
            if (keyStore == null) {
                sslContext = SSLContexts.createSystemDefault();
            } else {
                sslContext = SSLContexts.custom().loadTrustMaterial(keyStore).build();
            }
            return sslContext;
        } catch (NoSuchAlgorithmException | KeyStoreException | KeyManagementException ex) {
            throw new AuthenticationException(ex);
        }
    }

    @Override
    public void authenticate() throws CommunicationException {
        SSLContext sslContext = createSSLContext();
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslContext);

        LOGGER.debug("Running authentication...");
        try {

            RequestConfig defaultRequestConfig = RequestConfig.custom()
                    .setSocketTimeout(5000)
                    .setConnectTimeout(5000)
                    .setConnectionRequestTimeout(5000)
                    .build();

            HttpClientBuilder builder = HttpClients.custom()
                    .setDefaultCredentialsProvider(credentialsProvider)
                    .setSSLSocketFactory(sslsf)
                    .setDefaultRequestConfig(defaultRequestConfig);
            if (LOGGER.isDebugEnabled()) {
                builder.disableContentCompression();
            }

            CloseableHttpClient client = builder.build();
            connection.setClient(client);
            HttpHead httpHead = HTTPHelper.prepareHead(Client.MODEL_URI, connection.getHeaders());
            try (CloseableHttpResponse response = connection.getClient().execute(target, httpHead, connection.getContext())) {
                if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                    Authentication fallback = getFallback();
                    if (response.getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED && fallback != null) {
                        if (fallback instanceof KeystoneAuthentication) {
                            LOGGER.debug("Running Keystone fallback...");
                            KeystoneAuthentication ka = (KeystoneAuthentication) fallback;
                            ka.setOriginalResponse(response);
                            ka.authenticate();
                        } else {
                            throw new AuthenticationException("unknown fallback method");
                        }
                    } else {
                        if (response.getEntity() == null) {
                            LOGGER.error("Response: {}\nHeaders: {}\nBody:\n", response.getStatusLine().toString(), response.getAllHeaders());
                        } else {
                            LOGGER.error("Response: {}\nHeaders: {}\nBody: {}", response.getStatusLine().toString(), response.getAllHeaders(), EntityUtils.toString(response.getEntity()));
                        }
                        throw new AuthenticationException(response.getStatusLine().toString());
                    }
                }
            }
        } catch (IOException ex) {
            throw new CommunicationException(ex);
        }
    }

    /**
     * Loads custom CAs either from file or directory. If both set, CA file has
     * higher priority.
     *
     * @return keystore with custom CAs loaded
     * @throws AuthenticationException
     */
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
