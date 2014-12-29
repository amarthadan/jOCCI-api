package cz.cesnet.cloud.occi.api;

import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Entity;
import java.net.URI;
import java.util.List;

public abstract class Client {

    public static final String MODEL_URI = "/-/";
    private URI endpoint;
    private Model model;
    private boolean connected;
    private Authentication authentication;

    public URI getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(URI endpoint) {
        this.endpoint = endpoint.normalize();
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Authentication getAuthentication() {
        return authentication;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Retrieves all available resources represented by resource locations
     * (URIs).
     *
     * @return resources represented by resource locations (URIs)
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract List<URI> list() throws CommunicationException;

    /**
     * Retrieves available resources of a certain type represented by resource
     * locations (URIs).
     *
     * @param resourceType resource type in shortened format (e.g. "compute",
     * "storage", "network")
     * @return resources represented by resource locations (URIs)
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract List<URI> list(String resourceType) throws CommunicationException;

    /**
     * Retrieves available resources of a certain type represented by resource
     * locations (URIs).
     *
     * @param resourceIdentifier full resource type identifier
     * @return resources represented by resource locations (URIs)
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract List<URI> list(URI resourceIdentifier) throws CommunicationException;

    /**
     * Retrieves descriptions for all available resources.
     *
     * @return list of resource or link descriptions
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract List<Entity> describe() throws CommunicationException;

    /**
     * Retrieves descriptions for available resources of a certain type.
     *
     * @param resourceType resource type in shortened format (e.g. "compute",
     * "storage", "network")
     * @return
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract List<Entity> describe(String resourceType) throws CommunicationException;

    /**
     * Retrieves descriptions for available resources specified by a type
     * identifier or resource identifier.
     *
     * @param resourceIdentifier either full resource type identifier or full
     * resource identifier
     * @return
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract List<Entity> describe(URI resourceIdentifier) throws CommunicationException;

    /**
     * Creates a new resource on the server.
     *
     * @param entity Creates a new resource on the server.
     * @return URI of the new resource
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract URI create(Entity entity) throws CommunicationException;

    /**
     * Deletes all resource of a certain resource type from the server.
     *
     * @param resourceType resource type in shortened format (e.g. "compute",
     * "storage", "network")
     * @return status
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract boolean delete(String resourceType) throws CommunicationException;

    /**
     * Deletes all resource of a certain resource type or specific resource from
     * the server.
     *
     * @param resourceIdentifier either full resource type identifier or full
     * resource identifier
     * @return status
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract boolean delete(URI resourceIdentifier) throws CommunicationException;

    /**
     * Triggers given action on a specified set of resources.
     *
     * @param resourceType resource type in shortened format (e.g. "compute",
     * "storage", "network")
     * @param action type of action
     * @return status
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract boolean trigger(String resourceType, ActionInstance action) throws CommunicationException;

    /**
     * Triggers given action on a set of resources or on a specified resource.
     *
     * @param resourceIdentifier either full resource type identifier or full
     * resource identifier
     * @param action type of action
     * @return status
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract boolean trigger(URI resourceIdentifier, ActionInstance action) throws CommunicationException;

    /**
     * Refreshes the Model used inside the client. Useful for updating the model
     * without creating a new instance or reconnecting. Saves a lot of time in
     * an interactive mode.
     *
     * @throws cz.cesnet.cloud.occi.api.exception.CommunicationException
     */
    public abstract void refresh() throws CommunicationException;

    public abstract void connect() throws CommunicationException;
}
