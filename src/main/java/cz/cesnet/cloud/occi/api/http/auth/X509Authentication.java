package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.exception.AuthenticationException;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLContexts;

public class X509Authentication extends HTTPAuthentication {

    private static final String IDENTIFIER = "OCCIX509Authentication";
    private String certificate;
    private String password;

    public X509Authentication(String certificate, String password) {
        if (certificate == null) {
            throw new NullPointerException("certificate cannot be null");
        }
        if (certificate.isEmpty()) {
            throw new IllegalArgumentException("certificate cannot be empty");
        }
        if (password == null) {
            throw new NullPointerException("password cannot be null");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("password cannot be empty");
        }

        this.certificate = certificate;
        this.password = password;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        if (certificate == null) {
            throw new NullPointerException("certificate cannot be null");
        }
        if (certificate.isEmpty()) {
            throw new IllegalArgumentException("certificate cannot be empty");
        }

        this.certificate = certificate;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        if (password == null) {
            throw new NullPointerException("password cannot be null");
        }
        if (password.isEmpty()) {
            throw new IllegalArgumentException("password cannot be empty");
        }

        this.password = password;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Authentication getFallback() {
        return new KeystoneAuthentication(this);
    }

    @Override
    protected SSLContext createSSLContext() throws AuthenticationException {
        KeyStore trustStore = loadCAs();
        if (trustStore == null) {
            return null;
        }

        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            FileInputStream instream = new FileInputStream(new File(certificate));
            keyStore.load(instream, password.toCharArray());

            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(trustStore)
                    .loadKeyMaterial(keyStore, password.toCharArray())
                    .build();

            return sslContext;
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException |
                CertificateException | UnrecoverableKeyException | IOException ex) {
            throw new AuthenticationException(ex);
        }

    }

    @Override
    public void authenticate() throws CommunicationException {
        super.authenticate();
    }
}
