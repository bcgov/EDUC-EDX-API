package ca.bc.gov.educ.api.edx.exception;

public class APIServiceException extends Exception {

    public APIServiceException() {
        super();
    }

    public APIServiceException(String message) {
        super(message);
    }

    public APIServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public APIServiceException(Throwable cause) {
        super(cause);
    }

    protected APIServiceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
