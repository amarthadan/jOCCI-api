package cz.cesnet.cloud.occi.api;

import cz.cesnet.cloud.occi.api.exception.CommunicationException;

public interface Authentication {

    String getIdentifier();

    Authentication getFallback();

    void authenticate() throws CommunicationException;
}
