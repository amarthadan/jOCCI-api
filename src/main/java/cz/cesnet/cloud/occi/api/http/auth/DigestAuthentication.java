package cz.cesnet.cloud.occi.api.http.auth;

import org.apache.http.client.config.AuthSchemes;

/**
 * Class representing Digest HTTP authentication method. This method has a
 * Keystone authentication method as fallback.
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
