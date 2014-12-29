package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;

public class KeystoneAuthentication extends HTTPAuthentication {

    private static final String IDENTIFIER = "OCCIKeystoneAuthentication";
    private final Authentication originalAuthentication;

    public KeystoneAuthentication(Authentication originalAuthentication) {
        this.originalAuthentication = originalAuthentication;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }

    @Override
    public Authentication getFallback() {
        return null;
    }

    @Override
    public void authenticate() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
