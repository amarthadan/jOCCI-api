package cz.cesnet.cloud.occi.api;

import cz.cesnet.cloud.occi.api.exception.CommunicationException;

/**
 * Authentication method interface
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public interface Authentication {

    /**
     * Returns unique identifier for this authentication method.
     *
     * @return unique authentication identifier
     */
    String getIdentifier();

    /**
     * Returns authentication's fallback authentication method.
     *
     * @return fallback authentication method
     */
    Authentication getFallback();

    /**
     * Runs the authentication.
     *
     * @throws CommunicationException when error occures during the
     * communication
     */
    void authenticate() throws CommunicationException;
}
