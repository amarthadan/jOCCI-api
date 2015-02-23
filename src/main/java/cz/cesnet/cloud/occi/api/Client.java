package cz.cesnet.cloud.occi.api;

import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.exception.CommunicationException;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Entity;
import java.net.URI;
import java.util.List;

/**
 * Abstract class representing an OCCI client.
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
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
     * <p>
     * Retrieves all available resources represented by resource locations
     * (URIs).</p>
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * List<URI> list = client.list();}</pre>
     *
     * @return resources represented by resource locations (URIs)
     * @throws CommunicationException
     */
    public abstract List<URI> list() throws CommunicationException;

    /**
     * Retrieves available resources of a certain type represented by resource
     * locations (URIs).
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * List<URI> list = client.list("compute");}</pre>
     *
     * @param resourceType resource type in shortened format (e.g. "compute",
     * "storage", "network")
     * @return resources represented by resource locations (URIs)
     * @throws CommunicationException
     */
    public abstract List<URI> list(String resourceType) throws CommunicationException;

    /**
     * Retrieves available resources of a certain type represented by resource
     * locations (URIs).
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * List<URI> list = client.list(URI.create("http://schemas.ogf.org/occi/infrastructure#network"));}</pre>
     *
     * @param resourceIdentifier full resource type identifier
     * @return resources represented by resource locations (URIs)
     * @throws CommunicationException
     */
    public abstract List<URI> list(URI resourceIdentifier) throws CommunicationException;

    /**
     * Retrieves descriptions for all available resources.
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * List<Entity> list = client.describe();}</pre>
     *
     * @return list of resource or link descriptions
     * @throws CommunicationException
     */
    public abstract List<Entity> describe() throws CommunicationException;

    /**
     * Retrieves descriptions for available resources of a certain type.
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * List<Entity> list = client.describe("compute");}</pre>
     *
     * @param resourceType resource type in shortened format (e.g. "compute",
     * "storage", "network")
     * @return list of resource or link descriptions
     * @throws CommunicationException
     */
    public abstract List<Entity> describe(String resourceType) throws CommunicationException;

    /**
     * Retrieves descriptions for available resources specified by a type
     * identifier or resource identifier.
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * List<Entity> list = client.describe(URI.create("http://schemas.ogf.org/occi/infrastructure#network"));
     *...
     *list = client.describe(URI.create("https://remote.server.net/storage/123"));}</pre>
     *
     * @param resourceIdentifier either full resource type identifier or full
     * resource identifier
     * @return list of resource or link descriptions
     * @throws CommunicationException
     */
    public abstract List<Entity> describe(URI resourceIdentifier) throws CommunicationException;

    /**
     * Creates a new resource on the server.
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * Model model = client.getModel();
     *EntityBuilder entityBuilder = new EntityBuilder(model);
     *Resource resource = entityBuilder.getResource("compute");
     *resource.addMixin(model.findMixin("debian7", "os_tpl"));
     *resource.addMixin(model.findMixin("small", "resource_tpl"));
     *resource.addAttribute(Compute.MEMORY_ATTRIBUTE_NAME, "2048");
     *URI location = client.create(resource);}</pre>
     *
     * @param entity Creates a new resource on the server.
     * @return URI of the new resource
     * @throws CommunicationException
     */
    public abstract URI create(Entity entity) throws CommunicationException;

    /**
     * Deletes all resource of a certain resource type from the server.
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * boolean wasSuccessful = client.delete("compute");}</pre>
     *
     * @param resourceType resource type in shortened format (e.g. "compute",
     * "storage", "network")
     * @return status
     * @throws CommunicationException
     */
    public abstract boolean delete(String resourceType) throws CommunicationException;

    /**
     * Deletes all resource of a certain resource type or specific resource from
     * the server.
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * boolean wasSuccessful = client.delete(URI.create("http://schemas.ogf.org/occi/infrastructure#network"));
     *...
     *wasSuccessful = client.delete(URI.create("https://remote.server.net/storage/123"));}</pre>
     *
     * @param resourceIdentifier either full resource type identifier or full
     * resource identifier
     * @return status
     * @throws CommunicationException
     */
    public abstract boolean delete(URI resourceIdentifier) throws CommunicationException;

    /**
     * Triggers given action on a specified set of resources.
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * Model model = client.getModel();
     *EntityBuilder entityBuilder = new EntityBuilder(model);
     *ActionInstance actionInstance = entityBuilder.getActionInstance("start");
     *boolean wasSuccessful = client.trigger("compute", actionInstance);}</pre>
     *
     * @param resourceType resource type in shortened format (e.g. "compute",
     * "storage", "network")
     * @param action type of action
     * @return status
     * @throws CommunicationException
     */
    public abstract boolean trigger(String resourceType, ActionInstance action) throws CommunicationException;

    /**
     * Triggers given action on a set of resources or on a specified resource.
     *
     * <p>
     * Example:</p>
     *
     * <pre>{@code
     * Model model = client.getModel();
     *EntityBuilder entityBuilder = new EntityBuilder(model);
     *ActionInstance actionInstance = entityBuilder.getActionInstance("start");
     *boolean wasSuccessful = client.trigger(URI.create("http://schemas.ogf.org/occi/infrastructure#network"), actionInstance);
     *...
     *wasSuccessful = client.trigger(URI.create("https://remote.server.net/compute/456"), actionInstance);}</pre>
     *
     * @param resourceIdentifier either full resource type identifier or full
     * resource identifier
     * @param action type of action
     * @return status
     * @throws CommunicationException
     */
    public abstract boolean trigger(URI resourceIdentifier, ActionInstance action) throws CommunicationException;

    /**
     * Refreshes the Model used inside the client. Useful for updating the model
     * without creating a new instance or reconnecting. Saves a lot of time in
     * an interactive mode.
     *
     * @throws CommunicationException
     */
    public abstract void refresh() throws CommunicationException;

    /**
     * Establishes a connection.
     *
     * @throws CommunicationException
     */
    public abstract void connect() throws CommunicationException;
}
