package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.constants.SecureExchangeContactTypeCode;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreate;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateSecureExchangeSagaPayloadValidator {

  private final SecureExchangePayloadValidator payloadValidator;
  private final SecureExchangeDocumentsValidator secureExchangeDocumentsValidator;
  private final SecureExchangeStudentValidator secureExchangeStudentValidator;

  public static final String DISTRICT_ID = "districtID";
  public static final String DISTRICT_NAME = "districtName";
  public static final String SCHOOL_ID = "schoolID";
  public static final String SCHOOL_NAME = "schoolName";

  @Autowired
  public CreateSecureExchangeSagaPayloadValidator(SecureExchangePayloadValidator payloadValidator, SecureExchangeDocumentsValidator secureExchangeDocumentsValidator, SecureExchangeStudentValidator secureExchangeStudentValidator) {
    this.payloadValidator = payloadValidator;
    this.secureExchangeDocumentsValidator = secureExchangeDocumentsValidator;
    this.secureExchangeStudentValidator = secureExchangeStudentValidator;
  }

  public List<FieldError> validatePayload(SecureExchangeCreateSagaData secureExchangeCreateSagaData, boolean isCreateOperation) {
    SecureExchangeCreate secureExchange = secureExchangeCreateSagaData.getSecureExchangeCreate();

    final List<FieldError> apiValidationErrors = new ArrayList<>(payloadValidator.validatePayload(secureExchange, isCreateOperation));
    if(isCreateOperation && secureExchange.getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.SCHOOL.toString())) {
      if(secureExchangeCreateSagaData.getSchoolID() == null) {
        apiValidationErrors.add(createFieldError(SCHOOL_ID, secureExchangeCreateSagaData.getSchoolID(), "School ID cannot be null"));
      }

      if(secureExchangeCreateSagaData.getSchoolName() == null) {
        apiValidationErrors.add(createFieldError(SCHOOL_NAME, secureExchangeCreateSagaData.getSchoolName(), "School Name cannot be null"));
      }

    } else if(isCreateOperation && secureExchange.getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.DISTRICT.toString())) {
      if(secureExchangeCreateSagaData.getDistrictID() == null) {
        apiValidationErrors.add(createFieldError(DISTRICT_ID, secureExchangeCreateSagaData.getDistrictID(), "District ID cannot be null"));
      }

      if(secureExchangeCreateSagaData.getDistrictName() == null) {
        apiValidationErrors.add(createFieldError(DISTRICT_NAME, secureExchangeCreateSagaData.getDistrictName(), "District Name cannot be null"));
      }
    }

    if(secureExchange.getDocumentList() != null){
      secureExchange.getDocumentList().forEach(document -> apiValidationErrors.addAll(secureExchangeDocumentsValidator.validateDocumentPayload(document, true)));
    }

    if(secureExchange.getStudentList() != null){
      secureExchange.getStudentList().forEach(student -> apiValidationErrors.addAll(secureExchangeStudentValidator.validatePayload(student)));
    }

    return apiValidationErrors;
  }


  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("secureExchange", fieldName, rejectedValue, false, null, null, message);
  }

}
