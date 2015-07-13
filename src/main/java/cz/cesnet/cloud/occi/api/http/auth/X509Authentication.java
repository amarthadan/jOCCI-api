package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;

/**
 * Class representing HTTP authentication method via X509 certificates.
 *
 * <p>
 * Supports certificates in pk12 or pem format. This method has a Keystone
 * authentication method as fallback.</p>
 *
 * <p>
 * Example:</p>
 *
 * <pre>{@code
 * HTTPAuthentication auth = new X509Authentication("/path/to/certificate.pem", "password");
 * auth.setCAPath("/etc/grid-security/certificates/"); //path to CA directory
 * Client client = new HTTPClient(URI.create("https://remote.server.net"), auth);}</pre>
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class X509Authentication extends CertificateAuthentication {

    public static final String IDENTIFIER = "OCCIX509Authentication";

    /**
     * Constructor.
     *
     * @param certificate cannot be null nor empty
     * @param password cannot be null nor empty
     */
    public X509Authentication(String certificate, String password) {
        if (password.isEmpty()) {
            throw new IllegalArgumentException("password cannot be empty");
        }

        setCertificate(certificate);
        super.setPassword(password);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Authentication getFallback() {
        return new KeystoneAuthentication(this);
    }

    /**
     * Sets user's password.
     *
     * @param password user's password, cannot be null nor empty
     */
    @Override
    public void setPassword(String password) {
        if (password.isEmpty()) {
            throw new IllegalArgumentException("password cannot be empty");
        }

        super.setPassword(password);
    }
}
