package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateSecureExchangePayloadValidator {

  private final SecureExchangePayloadValidator payloadValidator;
  private final SecureExchangeDocumentsValidator secureExchangeDocumentsValidator;
  private final SecureExchangeStudentValidator secureExchangeStudentValidator;

  @Autowired
  public CreateSecureExchangePayloadValidator(SecureExchangePayloadValidator payloadValidator, SecureExchangeDocumentsValidator secureExchangeDocumentsValidator, SecureExchangeStudentValidator secureExchangeStudentValidator) {
    this.payloadValidator = payloadValidator;
    this.secureExchangeDocumentsValidator = secureExchangeDocumentsValidator;
    this.secureExchangeStudentValidator = secureExchangeStudentValidator;
  }

  public List<FieldError> validatePayload(SecureExchangeCreate secureExchange) {

    final List<FieldError> apiValidationErrors = new ArrayList<>(payloadValidator.validatePayload(secureExchange, true));
    if(secureExchange.getDocumentList() != null){
      secureExchange.getDocumentList().forEach(document -> apiValidationErrors.addAll(secureExchangeDocumentsValidator.validateDocumentPayload(document, true)));
    }
    if(secureExchange.getStudentList() != null){
      secureExchange.getStudentList().forEach(student -> apiValidationErrors.addAll(secureExchangeStudentValidator.validatePayload(student)));
    }
    return apiValidationErrors;
  }

}
