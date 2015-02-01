package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.exception.AuthenticationException;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import org.apache.http.conn.ssl.SSLContexts;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class X509Authentication extends HTTPAuthentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(X509Authentication.class);
    public static final String IDENTIFIER = "OCCIX509Authentication";
    private static final String CERT_BEGIN = "-----BEGIN CERTIFICATE-----";
    private static final String CERT_END = "-----END CERTIFICATE-----";
    private static final String GROUP_WHOLE = "whole";
    private static final String GROUP_TYPE = "type";
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
        Security.addProvider(new BouncyCastleProvider());
        KeyStore trustStore = loadCAs();

        try {
            KeyStore keyStore;
            if (certificate.endsWith(".p12")) {
                keyStore = loadUserCertificateFromPK12();
            } else {
                keyStore = loadUserCertificateFromPEM();
            }

            SSLContext sslContext = SSLContexts.custom()
                    .loadTrustMaterial(trustStore)
                    .loadKeyMaterial(keyStore, password.toCharArray())
                    .build();

            return sslContext;
        } catch (KeyStoreException | KeyManagementException | NoSuchAlgorithmException | UnrecoverableKeyException ex) {
            throw new AuthenticationException(ex);
        }

    }

    private KeyStore loadUserCertificateFromPK12() throws AuthenticationException {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            FileInputStream instream = new FileInputStream(new File(certificate));
            keyStore.load(instream, password.toCharArray());

            return keyStore;
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException | CertificateException ex) {
            throw new AuthenticationException(ex);
        }
    }

    private KeyStore loadUserCertificateFromPEM() throws AuthenticationException {
        try {
            String certFileString = new String(Files.readAllBytes(Paths.get(certificate)));
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<X509Certificate> certChain = new ArrayList<>();
            int startIndex = certFileString.indexOf(CERT_BEGIN, 0);
            int endIndex;

            PEMReader reader;
            while (startIndex != -1) {
                endIndex = certFileString.indexOf(CERT_END, startIndex);
                String oneCert = certFileString.substring(startIndex, endIndex + CERT_END.length());
                reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(oneCert.getBytes())));
                X509Certificate cert = (X509Certificate) reader.readObject();
                if (cert == null) {
                    throw new AuthenticationException("cannot load user certificate");
                }
                certChain.add(cert);

                startIndex = certFileString.indexOf(CERT_BEGIN, startIndex + 1);
            }

            Pattern pattern = Pattern.compile("(?<" + GROUP_WHOLE + ">-----BEGIN (?<" + GROUP_TYPE + ">RSA |DSA |EC |DH )*PRIVATE KEY-----)");
            Matcher matcher = pattern.matcher(certFileString);
            if (!matcher.find()) {
                throw new AuthenticationException("cannot read certificate key");
            }
            startIndex = matcher.start(GROUP_WHOLE);

            pattern = Pattern.compile("(?<" + GROUP_WHOLE + ">-----END (?<" + GROUP_TYPE + ">RSA |DSA |EC |DH )*PRIVATE KEY-----)");
            matcher = pattern.matcher(certFileString);
            if (!matcher.find(startIndex)) {
                throw new AuthenticationException("cannot read certificate key");
            }
            endIndex = matcher.end(GROUP_WHOLE);

            String key = certFileString.substring(startIndex, endIndex).trim();
            reader = new PEMReader(new InputStreamReader(new ByteArrayInputStream(key.getBytes())));

            Object object = reader.readObject();
            PrivateKey pk = null;
            if (object instanceof PrivateKey) {
                pk = (PrivateKey) object;
            }
            if (object instanceof KeyPair) {
                pk = ((KeyPair) object).getPrivate();
            }

            if (pk == null) {
                throw new AuthenticationException("cannot load private key");
            }

            KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null);
            for (X509Certificate x509Cert : certChain) {
                ks.setCertificateEntry(x509Cert.getSubjectX500Principal().getName(), x509Cert);
                LOGGER.debug("adding certificate: " + x509Cert.getSubjectX500Principal().getName());
            }

            ks.setKeyEntry("private_key", pk, password.toCharArray(), certChain.toArray(new Certificate[0]));
            return ks;
        } catch (IOException | CertificateException | NoSuchAlgorithmException | KeyStoreException ex) {
            throw new AuthenticationException(ex);
        }
    }

    @Override
    public void authenticate() throws CommunicationException {
        super.authenticate();
    }
}
