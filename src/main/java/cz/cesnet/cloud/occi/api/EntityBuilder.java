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

public class EntityBuilder {

    private Model model;

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

    public Resource getResource(String resourceType) throws EntityBuildingException {
        try {
            Kind kind = getKind(resourceType);
            return createResource(kind);
        } catch (AmbiguousIdentifierException ex) {
            throw new EntityBuildingException(ex);
        }
    }

    public Resource getResource(URI resourceIdentifier) throws EntityBuildingException {
        Kind kind = getKind(resourceIdentifier);
        return createResource(kind);
    }

    public Link getLink(String linkType) throws EntityBuildingException {
        try {
            Kind kind = getKind(linkType);
            return createLink(kind);
        } catch (AmbiguousIdentifierException ex) {
            throw new EntityBuildingException(ex);
        }
    }

    public Link getLink(URI linkIdentifier) throws EntityBuildingException {
        Kind kind = getKind(linkIdentifier);
        return createLink(kind);
    }

    public ActionInstance getActionInstance(String actionType) throws EntityBuildingException {
        try {
            Action action = getAction(actionType);
            return createActionInstance(action);
        } catch (AmbiguousIdentifierException ex) {
            throw new EntityBuildingException(ex);
        }
    }

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
