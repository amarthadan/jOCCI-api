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

    private Mixin getMixin(String type) throws EntityBuildingException, AmbiguousIdentifierException {
        Mixin mixin = model.findMixin(type);
        if (mixin == null) {
            throw new EntityBuildingException("unknown type '" + type + "'");
        }

        return mixin;
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

    public Compute getCompute(URI resourceIdentifier) throws EntityBuildingException {
        return createCompute(getKind(resourceIdentifier));
    }

    public Compute getCompute() throws EntityBuildingException {
        Kind kind = getKind(URI.create("http://schemas.ogf.org/occi/infrastructure#compute"));
        if (kind == null) {
            return createCompute(Compute.getDefaultKind());
        } else {
            return createCompute(kind);
        }
    }

    private Kind getNetworkKind() throws EntityBuildingException {
        Kind kind = getKind(URI.create("http://schemas.ogf.org/occi/infrastructure#network"));
        if (kind == null) {
            kind = Network.getDefaultKind();
        }

        return kind;
    }

    public Network getNetwork(URI resourceIdentifier) throws EntityBuildingException {
        return createNetwork(getKind(resourceIdentifier));
    }

    public Network getNetwork() throws EntityBuildingException {
        return createNetwork(getNetworkKind());
    }

    public Storage getStorage(URI resourceIdentifier) throws EntityBuildingException {
        return createStorage(getKind(resourceIdentifier));
    }

    public Storage getStorage() throws EntityBuildingException {
        Kind kind = getKind(URI.create("http://schemas.ogf.org/occi/infrastructure#storage"));
        if (kind == null) {
            return createStorage(Storage.getDefaultKind());
        } else {
            return createStorage(kind);
        }
    }

    public StorageLink getStorageLink(URI resourceIdentifier) throws EntityBuildingException {
        return createStorageLink(getKind(resourceIdentifier));
    }

    public StorageLink getStorageLink() throws EntityBuildingException {
        Kind kind = getKind(URI.create("http://schemas.ogf.org/occi/infrastructure#storagelink"));
        if (kind == null) {
            return createStorageLink(StorageLink.getDefaultKind());
        } else {
            return createStorageLink(kind);
        }
    }

    private Kind getNetworkInterfaceKind() throws EntityBuildingException {
        Kind kind = getKind(URI.create("http://schemas.ogf.org/occi/infrastructure#networkinterface"));
        if (kind == null) {
            kind = NetworkInterface.getDefaultKind();
        }

        return kind;
    }

    public NetworkInterface getNetworkInterface(URI resourceIdentifier) throws EntityBuildingException {
        return createNetworkInterface(getKind(resourceIdentifier));
    }

    public NetworkInterface getNetworkInterface() throws EntityBuildingException {
        return createNetworkInterface(getNetworkInterfaceKind());
    }

    public IPNetwork getIPNetwork(URI kindIdentifier, URI mixinIdentifier) throws EntityBuildingException {
        return createIPNetwork(getKind(kindIdentifier), getMixin(mixinIdentifier));
    }

    public IPNetwork getIPNetwork() throws EntityBuildingException {
        Kind kind = getNetworkKind();
        Mixin mixin = getMixin(URI.create("http://schemas.ogf.org/occi/infrastructure/network#ipnetwork"));
        if (mixin == null) {
            mixin = IPNetwork.getDefaultMixin();
        }

        return createIPNetwork(kind, mixin);
    }

    public IPNetworkInterface getIPNetworkInterface(URI kindIdentifier, URI mixinIdentifier) throws EntityBuildingException {
        return createIPNetworkInterface(getKind(kindIdentifier), getMixin(mixinIdentifier));
    }

    public IPNetworkInterface getIPNetworkInterface() throws EntityBuildingException {
        Kind kind = getNetworkInterfaceKind();
        Mixin mixin = getMixin(URI.create("http://schemas.ogf.org/occi/infrastructure/networkinterface#ipnetwork"));
        if (mixin == null) {
            mixin = IPNetworkInterface.getDefaultMixin();
        }

        return createIPNetworkInterface(kind, mixin);
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
