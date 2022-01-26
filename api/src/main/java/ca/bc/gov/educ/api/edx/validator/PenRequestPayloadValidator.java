package ca.bc.gov.educ.api.edx.validator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeService;
import lombok.AccessLevel;
import lombok.Getter;

@Component
public class PenRequestPayloadValidator {

  public static final String GENDER_CODE = "genderCode";
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeService penRequestService;
  @Getter
  private final ApplicationProperties applicationProperties;

  @Autowired
  public PenRequestPayloadValidator(SecureExchangeService penRequestService, ApplicationProperties applicationProperties) {
    this.penRequestService = penRequestService;
    this.applicationProperties = applicationProperties;
  }

  public List<FieldError> validatePayload(SecureExchange secureExchange, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && secureExchange.getSecureExchangeID() != null) {
      apiValidationErrors.add(createFieldError("penRequestID", secureExchange.getSecureExchangeID(), "penRequestID should be null for post operation."));
    }

    if (isCreateOperation && secureExchange.getInitialSubmitDate() != null) {
      apiValidationErrors.add(createFieldError("initialSubmitDate", secureExchange.getSecureExchangeID(), "initialSubmitDate should be null for post operation."));
    }

    return apiValidationErrors;
  }


  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("penRequest", fieldName, rejectedValue, false, null, null, message);
  }

}
