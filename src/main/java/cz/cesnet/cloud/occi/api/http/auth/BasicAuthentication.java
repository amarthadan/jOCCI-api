package cz.cesnet.cloud.occi.api.http.auth;

import cz.cesnet.cloud.occi.api.Authentication;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class representing BASIC HTTP authentication method.
 *
 * <p>
 * This method has a Keystone authentication method as fallback.</p>
 *
 * <p>
 * Example:</p>
 *
 * <pre>{@code
 * HTTPAuthentication auth = new BasicAuthentication("username", "password");
 *auth.setCAPath("/etc/grid-security/certificates/"); //path to CA directory
 *Client client = new HTTPClient(URI.create("https://remote.server.net"), auth);}</pre>
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class BasicAuthentication extends HTTPAuthentication {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthentication.class);
    public static final String IDENTIFIER = "OCCIBasicAuthentication";
    private String username;
    private String password;
    private String authScheme;

    public BasicAuthentication(String username, String password) {
        this.username = username;
        this.password = password;
        this.authScheme = AuthSchemes.BASIC;
    }

    protected void setAuthScheme(String authScheme) {
        this.authScheme = authScheme;
    }

    /**
     * Returns username.
     *
     * @return username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets username.
     *
     * @param username username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns password.
     *
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets password
     *
     * @param password password
     */
    public void setPassword(String password) {
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
    public void authenticate() throws CommunicationException {
        LOGGER.debug("Creating credentials provider with username: '{}' and password: '{}'", username, password);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(getTarget().getHostName(), getTarget().getPort(), null, authScheme),
                new UsernamePasswordCredentials(username, password));
        setCredentialsProvider(credsProvider);
        super.authenticate();
    }
}
