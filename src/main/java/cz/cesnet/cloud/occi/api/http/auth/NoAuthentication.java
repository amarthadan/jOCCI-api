package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;

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
