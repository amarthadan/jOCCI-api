package cz.cesnet.cloud.occi.api.exception;

public class AuthenticationException extends CommunicationException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable ex) {
        super(message, ex);
    }

    public AuthenticationException(Throwable ex) {
        super(ex);
    }

}
