package ca.bc.gov.educ.api.edx.exception;

import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import lombok.Getter;

@SuppressWarnings("squid:S1948")
public class InvalidPayloadException extends RuntimeException {

  @Getter
  private final ApiError error;

  public InvalidPayloadException(final ApiError error) {
    super(error.getMessage());
    this.error = error;
  }
}
