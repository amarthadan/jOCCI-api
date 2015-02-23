package cz.cesnet.cloud.occi.api.http.auth;

import org.apache.http.client.config.AuthSchemes;

/**
 * Class representing Digest HTTP authentication method.
 *
 * <p>
 * This method has a Keystone authentication method as fallback.</p>
 *
 * <p>
 * Example:</p>
 *
 * <pre>{@code
 * HTTPAuthentication auth = new DigestAuthentication("username", "password");
 *auth.setCAPath("/etc/grid-security/certificates/"); //path to CA directory
 *Client client = new HTTPClient(URI.create("https://remote.server.net"), auth);}</pre>
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class DigestAuthentication extends BasicAuthentication {

    public static final String IDENTIFIER = "OCCIDigestAuthentication";

    public DigestAuthentication(String username, String password) {
        super(username, password);
        setAuthScheme(AuthSchemes.DIGEST);
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
