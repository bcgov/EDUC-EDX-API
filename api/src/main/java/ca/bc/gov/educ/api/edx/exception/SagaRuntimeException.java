package ca.bc.gov.educ.api.edx.exception;

/**
 * The type Saga runtime exception.
 */
public class SagaRuntimeException extends RuntimeException {

  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 5241655513745148898L;

  /**
   * Instantiates a new Saga runtime exception.
   *
   * @param message the message
   */
  public SagaRuntimeException(final String message) {
    super(message);
  }

  /**
   * Instantiates a new Saga runtime exception.
   *
   * @param exception the exception
   */
  public SagaRuntimeException(final Throwable exception) {
    super(exception);
  }

}
