package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;

/**
 * Dummy authentication method representing no authentication.
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class NoAuthentication extends HTTPAuthentication {

    public static final String IDENTIFIER = "OCCINoAuthentication";

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Authentication getFallback() {
        return null;
    }

}
