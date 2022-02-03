package ca.bc.gov.educ.api.edx.validator;

import java.util.ArrayList;
import java.util.List;

import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeService;
import lombok.AccessLevel;
import lombok.Getter;

@Component
public class SecureExchangePayloadValidator {

  public static final String GENDER_CODE = "genderCode";
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeService secureExchangeService;
  @Getter
  private final ApplicationProperties applicationProperties;

  @Autowired
  public SecureExchangePayloadValidator(SecureExchangeService secureExchangeService, ApplicationProperties applicationProperties) {
    this.secureExchangeService = secureExchangeService;
    this.applicationProperties = applicationProperties;
  }

  public List<FieldError> validatePayload(SecureExchange secureExchange, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && secureExchange.getSecureExchangeID() != null) {
      apiValidationErrors.add(createFieldError("secureExchangeID", secureExchange.getSecureExchangeID(), "secureExchangeID should be null for post operation."));
    }

    return apiValidationErrors;
  }


  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("secureExchange", fieldName, rejectedValue, false, null, null, message);
  }

}
