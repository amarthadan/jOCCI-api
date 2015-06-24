package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;

/**
 * Class representing HTTP authentication method via VOMS certificates.
 *
 * <p>
 * Supports certificates in pk12 or pem format. This method has a Keystone
 * authentication method as fallback.</p>
 *
 * <p>
 * Example:</p>
 *
 * <pre>{@code
 * HTTPAuthentication auth = new VOMSAuthentication("/path/to/certificate.pem");
 * auth.setCAPath("/etc/grid-security/certificates/"); //path to CA directory
 * Client client = new HTTPClient(URI.create("https://remote.server.net"), auth);}</pre>
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class VOMSAuthentication extends CertificateAuthentication {

    public static final String IDENTIFIER = "OCCIVOMSAuthentication";

    /**
     * Constructor.
     *
     * @param certificate cannot be null nor empty
     */
    public VOMSAuthentication(String certificate) {
        setCertificate(certificate);
        setPassword("");
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Authentication getFallback() {
        return new KeystoneAuthentication(this);
    }
}
