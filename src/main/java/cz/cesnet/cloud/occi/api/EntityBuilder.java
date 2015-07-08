package cz.cesnet.cloud.occi.api;

import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.exception.EntityBuildingException;
import cz.cesnet.cloud.occi.core.Action;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Kind;
import cz.cesnet.cloud.occi.core.Link;
import cz.cesnet.cloud.occi.core.Mixin;
import cz.cesnet.cloud.occi.core.Resource;
import cz.cesnet.cloud.occi.exception.AmbiguousIdentifierException;
import cz.cesnet.cloud.occi.exception.InvalidAttributeValueException;
import cz.cesnet.cloud.occi.infrastructure.Compute;
import cz.cesnet.cloud.occi.infrastructure.IPNetwork;
import cz.cesnet.cloud.occi.infrastructure.IPNetworkInterface;
import cz.cesnet.cloud.occi.infrastructure.Network;
import cz.cesnet.cloud.occi.infrastructure.NetworkInterface;
import cz.cesnet.cloud.occi.infrastructure.Storage;
import cz.cesnet.cloud.occi.infrastructure.StorageLink;
import java.net.URI;
import java.util.UUID;

/**
 * Builder class that helps with creation of OCCI entities.
 *
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
public class EntityBuilder {

    private Model model;

    /**
     * Default constructor.
     *
     * @param model cannot be null
     */
    public EntityBuilder(Model model) {
        if (model == null) {
            throw new NullPointerException("model cannot be null");
        }

        this.model = model;
    }

    private Kind getKind(String type) throws EntityBuildingException, AmbiguousIdentifierException {
        Kind kind = model.findKind(type);
        if (kind == null) {
            throw new EntityBuildingException("unknown type '" + type + "'");
        }

        return kind;
    }

    private Kind getKind(URI identifier) throws EntityBuildingException {
        Kind kind = model.findKind(identifier);
        if (kind == null) {
            throw new EntityBuildingException("unknown identifier '" + identifier + "'");
        }

        return kind;
    }

    private Kind getKind(Class resourceClass) throws EntityBuildingException {
        URI uri = null;
        Kind defaultKind = null;

        if (resourceClass.equals(Compute.class)) {
            uri = URI.create(Compute.KIND_IDENTIFIER_DEFAULT);
            defaultKind = Compute.getDefaultKind();
        } else if (resourceClass.equals(Network.class)) {
            uri = URI.create(Network.KIND_IDENTIFIER_DEFAULT);
            defaultKind = Network.getDefaultKind();
        } else if (resourceClass.equals(Storage.class)) {
            uri = URI.create(Storage.KIND_IDENTIFIER_DEFAULT);
            defaultKind = Storage.getDefaultKind();
        } else if (resourceClass.equals(StorageLink.class)) {
            uri = URI.create(StorageLink.KIND_IDENTIFIER_DEFAULT);
            defaultKind = StorageLink.getDefaultKind();
        } else if (resourceClass.equals(NetworkInterface.class)) {
            uri = URI.create(NetworkInterface.KIND_IDENTIFIER_DEFAULT);
            defaultKind = NetworkInterface.getDefaultKind();
        } else {
            throw new EntityBuildingException("unknown class '" + resourceClass.getName() + "'");
        }

        Kind kind;
        try {
            kind = getKind(uri);
        } catch (EntityBuildingException ex) {
            kind = defaultKind;
        }

        return kind;
    }

    private Mixin getMixin(URI identifier) throws EntityBuildingException {
        Mixin mixin = model.findMixin(identifier);
        if (mixin == null) {
            throw new EntityBuildingException("unknown identifier '" + identifier + "'");
        }

        return mixin;
    }

    private Action getAction(String type) throws EntityBuildingException, AmbiguousIdentifierException {
        Action action = model.findAction(type);
        if (action == null) {
            throw new EntityBuildingException("unknown type '" + type + "'");
        }

        return action;
    }

    private Action getAction(URI identifier) throws EntityBuildingException {
        Action action = model.findAction(identifier);
        if (action == null) {
            throw new EntityBuildingException("unknown identifier '" + identifier + "'");
        }

        return action;
    }

    /**
     * Creates a link of given linkType (kind's term).
     *
     * @param linkType
     * @return new Link instance of given linkType
     * @throws EntityBuildingException if link type is ambiguous
     */
    public Link getLink(String linkType) throws EntityBuildingException {
        try {
            Kind kind = getKind(linkType);
            return createLink(kind);
        } catch (AmbiguousIdentifierException ex) {
            throw new EntityBuildingException(ex);
        }
    }

    /**
     * Creates a link identified by linkIdentifier (kind's scheme+term).
     *
     * @param linkIdentifier
     * @return new Link instance identified by linkIdentifier
     * @throws EntityBuildingException if kind with specified identifier is not
     * found in the model
     */
    public Link getLink(URI linkIdentifier) throws EntityBuildingException {
        return createLink(getKind(linkIdentifier));
    }

    /**
     * Creates a resource of given resourceType (kind's term).
     *
     * @param resourceType
     * @return new Resource instance of given resourceType
     * @throws EntityBuildingException if resource type is ambiguous
     */
    public Resource getResource(String resourceType) throws EntityBuildingException {
        try {
            Kind kind = getKind(resourceType);
            return createResource(kind);
        } catch (AmbiguousIdentifierException ex) {
            throw new EntityBuildingException(ex);
        }
    }

    /**
     * Creates a resource identified by resourceIdentifier (kind's scheme+term).
     *
     * @param resourceIdentifier
     * @return new Resource instance identified by resourceIdentifier
     * @throws EntityBuildingException if kind with specified identifier is not
     * found in the model
     */
    public Resource getResource(URI resourceIdentifier) throws EntityBuildingException {
        return createResource(getKind(resourceIdentifier));
    }

    /**
     * Creates an action instance of given actionType (action's term).
     *
     * @param actionType
     * @return new ActionInstance instance of given actionType
     * @throws EntityBuildingException if action type is ambiguous
     */
    public ActionInstance getActionInstance(String actionType) throws EntityBuildingException {
        try {
            Action action = getAction(actionType);
            return createActionInstance(action);
        } catch (AmbiguousIdentifierException ex) {
            throw new EntityBuildingException(ex);
        }
    }

    /**
     * Creates an action instance identified by actionIdentifier (action's
     * scheme+term).
     *
     * @param actionIdentifier
     * @return new ActionInstance instance identified by actionIdentifier
     * @throws EntityBuildingException if action with specified identifier is
     * not found in the model
     */
    public ActionInstance getActionInstance(URI actionIdentifier) throws EntityBuildingException {
        return createActionInstance(getAction(actionIdentifier));
    }

    /**
     * Creates an compute instance identified by resourceIdentifier (compute's
     * scheme+term).
     *
     * @param resourceIdentifier
     * @return new Compute instance identified by resourceIdentifier
     * @throws EntityBuildingException if compute with specified identifier is
     * not found in the model
     */
    public Compute getCompute(URI resourceIdentifier) throws EntityBuildingException {
        return createCompute(getKind(resourceIdentifier));
    }

    /**
     * Creates a default compute instance.
     *
     * @return new default Compute instance
     * @throws EntityBuildingException
     */
    public Compute getCompute() throws EntityBuildingException {
        return createCompute(getKind(Compute.class));
    }

    /**
     * Creates an network instance identified by resourceIdentifier (network's
     * scheme+term).
     *
     * @param resourceIdentifier
     * @return new Network instance identified by resourceIdentifier
     * @throws EntityBuildingException
     */
    public Network getNetwork(URI resourceIdentifier) throws EntityBuildingException {
        return createNetwork(getKind(resourceIdentifier));
    }

    /**
     * Creates a default network instance.
     *
     * @return new default Network instance
     * @throws EntityBuildingException
     */
    public Network getNetwork() throws EntityBuildingException {
        return createNetwork(getKind(Network.class));
    }

    /**
     * Creates an storage instance identified by resourceIdentifier (storage's
     * scheme+term).
     *
     * @param resourceIdentifier
     * @return new Storage instance identified by resourceIdentifier
     * @throws EntityBuildingException
     */
    public Storage getStorage(URI resourceIdentifier) throws EntityBuildingException {
        return createStorage(getKind(resourceIdentifier));
    }

    /**
     * Creates a default storage instance.
     *
     * @return new default Storage instance
     * @throws EntityBuildingException
     */
    public Storage getStorage() throws EntityBuildingException {
        return createStorage(getKind(Storage.class));
    }

    /**
     * Creates an storage link instance identified by resourceIdentifier (link's
     * scheme+term).
     *
     * @param resourceIdentifier
     * @return new StorageLink instance identified by resourceIdentifier
     * @throws EntityBuildingException
     */
    public StorageLink getStorageLink(URI resourceIdentifier) throws EntityBuildingException {
        return createStorageLink(getKind(resourceIdentifier));
    }

    /**
     * Creates a default storage link instance.
     *
     * @return new default StorageLink instance
     * @throws EntityBuildingException
     */
    public StorageLink getStorageLink() throws EntityBuildingException {
        return createStorageLink(getKind(StorageLink.class));
    }

    /**
     * Creates an network interface instance identified by resourceIdentifier
     * (interface's scheme+term).
     *
     * @param resourceIdentifier
     * @return new NetworkInterface instance identified by resourceIdentifier
     * @throws EntityBuildingException
     */
    public NetworkInterface getNetworkInterface(URI resourceIdentifier) throws EntityBuildingException {
        return createNetworkInterface(getKind(resourceIdentifier));
    }

    /**
     * Creates a default network interface instance.
     *
     * @return new default NetworkInterface instance
     * @throws EntityBuildingException
     */
    public NetworkInterface getNetworkInterface() throws EntityBuildingException {
        return createNetworkInterface(getKind(NetworkInterface.class));
    }

    /**
     * Creates an ip network instance identified by kind and mixin identifier
     * (scheme+term).
     *
     * @param kindIdentifier
     * @param mixinIdentifier
     * @return new IPNetwork instance identified by its kind and mixin
     * identifiers
     * @throws EntityBuildingException
     */
    public IPNetwork getIPNetwork(URI kindIdentifier, URI mixinIdentifier) throws EntityBuildingException {
        return createIPNetwork(getKind(kindIdentifier), getMixin(mixinIdentifier));
    }

    /**
     * Creates a default ip network instance.
     *
     * @return new default IPNetwork instance
     * @throws EntityBuildingException
     */
    public IPNetwork getIPNetwork() throws EntityBuildingException {
        Kind kind = getKind(Network.class);

        Mixin mixin;
        try {
            mixin = getMixin(URI.create(IPNetwork.MIXIN_IDENTIFIER_DEFAULT));
        } catch (EntityBuildingException ex) {
            mixin = IPNetwork.getDefaultMixin();
        }

        return createIPNetwork(kind, mixin);
    }

    /**
     * Creates an ip network interface instance identified by kind and mixin
     * identifier (scheme+term).
     *
     * @param kindIdentifier
     * @param mixinIdentifier
     * @return new IPNetworkInterface instance identified by its kind and mixin
     * identifiers
     * @throws EntityBuildingException
     */
    public IPNetworkInterface getIPNetworkInterface(URI kindIdentifier, URI mixinIdentifier) throws EntityBuildingException {
        return createIPNetworkInterface(getKind(kindIdentifier), getMixin(mixinIdentifier));
    }

    /**
     * Creates a default ip network interface instance.
     *
     * @return new default IPNetworkInterface instance
     * @throws EntityBuildingException
     */
    public IPNetworkInterface getIPNetworkInterface() throws EntityBuildingException {
        Kind kind = getKind(NetworkInterface.class);

        Mixin mixin;
        try {
            mixin = getMixin(URI.create(IPNetworkInterface.MIXIN_IDENTIFIER_DEFAULT));
        } catch (EntityBuildingException ex) {
            mixin = IPNetworkInterface.getDefaultMixin();
        }

        return createIPNetworkInterface(kind, mixin);
    }

    private Resource createResource(Kind kind) {
        try {
            Resource resource = new Resource(UUID.randomUUID().toString(), kind);
            resource.setModel(model);
            return resource;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    private Link createLink(Kind kind) {
        try {
            Link link = new Link(UUID.randomUUID().toString(), kind);
            link.setModel(model);
            return link;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    private ActionInstance createActionInstance(Action action) {
        ActionInstance ai = new ActionInstance(action);
        ai.setModel(model);
        return ai;
    }

    private Compute createCompute(Kind kind) {
        try {
            Compute compute = new Compute(UUID.randomUUID().toString(), kind);
            compute.setModel(model);
            return compute;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    private Network createNetwork(Kind kind) {
        try {
            Network network = new Network(UUID.randomUUID().toString(), kind);
            network.setModel(model);
            return network;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    private Storage createStorage(Kind kind) {
        try {
            Storage storage = new Storage(UUID.randomUUID().toString(), kind);
            storage.setModel(model);
            return storage;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    private StorageLink createStorageLink(Kind kind) {
        try {
            StorageLink storageLink = new StorageLink(UUID.randomUUID().toString(), kind);
            storageLink.setModel(model);
            return storageLink;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    private NetworkInterface createNetworkInterface(Kind kind) {
        try {
            NetworkInterface networkInterface = new NetworkInterface(UUID.randomUUID().toString(), kind);
            networkInterface.setModel(model);
            return networkInterface;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    private IPNetwork createIPNetwork(Kind kind, Mixin mixin) {
        try {
            IPNetwork ipnetwork = new IPNetwork(UUID.randomUUID().toString(), kind);
            ipnetwork.setModel(model);
            ipnetwork.addMixin(mixin);
            return ipnetwork;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    private IPNetworkInterface createIPNetworkInterface(Kind kind, Mixin mixin) {
        try {
            IPNetworkInterface ipnetworkInterface = new IPNetworkInterface(UUID.randomUUID().toString(), kind);
            ipnetworkInterface.setModel(model);
            ipnetworkInterface.addMixin(mixin);
            return ipnetworkInterface;
        } catch (InvalidAttributeValueException ex) {
            throw new RuntimeException("Invalid ID attribute value. This should not happen!", ex);
        }
    }

    /**
     * Returns model.
     *
     * @return model
     */
    public Model getModel() {
        return model;
    }

    /**
     * Sets model.
     *
     * @param model model
     */
    public void setModel(Model model) {
        this.model = model;
    }
}
