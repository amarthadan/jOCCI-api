package cz.cesnet.cloud.occi.api.exception;

public class CommunicationException extends Exception {

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, Throwable ex) {
        super(message, ex);
    }

    public CommunicationException(Throwable ex) {
        super(ex);
    }
}
