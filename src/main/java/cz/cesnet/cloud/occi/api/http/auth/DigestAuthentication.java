package cz.cesnet.cloud.occi.api.http.auth;

import org.apache.http.client.config.AuthSchemes;

public class DigestAuthentication extends BasicAuthentication {

    private static final String IDENTIFIER = "OCCIDigestAuthentication";

    public DigestAuthentication(String username, String password) {
        super(username, password);
        setAuthScheme(AuthSchemes.DIGEST);
    }

    public DigestAuthentication() {
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
