package cz.cesnet.cloud.occi.api.exception;

public class EntityBuildingException extends Exception {

    public EntityBuildingException(String message) {
        super(message);
    }

    public EntityBuildingException(String message, Throwable ex) {
        super(message, ex);
    }

    public EntityBuildingException(Throwable ex) {
        super(ex);
    }
}
