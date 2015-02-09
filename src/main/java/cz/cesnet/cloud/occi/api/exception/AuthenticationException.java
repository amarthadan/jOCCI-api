package cz.cesnet.cloud.occi.api.exception;

/**
 * @author Michal Kimle <kimle.michal@gmail.com>
 */
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
