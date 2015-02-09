package cz.cesnet.cloud.occi.api;

import cz.cesnet.cloud.occi.Model;
import cz.cesnet.cloud.occi.api.exception.EntityBuildingException;
import cz.cesnet.cloud.occi.core.Action;
import cz.cesnet.cloud.occi.core.ActionInstance;
import cz.cesnet.cloud.occi.core.Kind;
import cz.cesnet.cloud.occi.core.Link;
import cz.cesnet.cloud.occi.core.Resource;
import cz.cesnet.cloud.occi.exception.AmbiguousIdentifierException;
import cz.cesnet.cloud.occi.exception.InvalidAttributeValueException;
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
        Kind kind = getKind(resourceIdentifier);
        return createResource(kind);
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
        Kind kind = getKind(linkIdentifier);
        return createLink(kind);
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
        Action action = getAction(actionIdentifier);
        return createActionInstance(action);
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

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }
}
