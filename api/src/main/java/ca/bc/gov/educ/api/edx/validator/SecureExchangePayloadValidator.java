package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.constants.SecureExchangeStatusCode;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeContactTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeStatusCodeTableRepository;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class SecureExchangePayloadValidator {

  public static final String GENDER_CODE = "genderCode";
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeService secureExchangeService;
  @Getter
  private final ApplicationProperties applicationProperties;

  private final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  private final SecureExchangeContactTypeCodeTableRepository secureExchangeContactTypeCodeTableRepository;

  private final SecureExchangeStatusCodeTableRepository secureExchangeStatusCodeTableRepository;

  @Autowired
  public SecureExchangePayloadValidator(SecureExchangeService secureExchangeService, ApplicationProperties applicationProperties, MinistryOwnershipTeamRepository ministryOwnershipTeamRepository, SecureExchangeContactTypeCodeTableRepository secureExchangeContactTypeCodeTableRepository, SecureExchangeStatusCodeTableRepository secureExchangeStatusCodeTableRepository) {
    this.secureExchangeService = secureExchangeService;
    this.applicationProperties = applicationProperties;
    this.ministryOwnershipTeamRepository = ministryOwnershipTeamRepository;
    this.secureExchangeContactTypeCodeTableRepository = secureExchangeContactTypeCodeTableRepository;
    this.secureExchangeStatusCodeTableRepository = secureExchangeStatusCodeTableRepository;
  }

  public List<FieldError> validatePayload(SecureExchange secureExchange, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && secureExchange.getSecureExchangeID() != null) {
      apiValidationErrors.add(createFieldError("secureExchangeID", secureExchange.getSecureExchangeID(), "secureExchangeID should be null for post operation."));
    }

    if (isCreateOperation && secureExchange.getSecureExchangeStatusCode() != null &&
      !secureExchange.getSecureExchangeStatusCode().equals(SecureExchangeStatusCode.NEW.toString()) && !secureExchange.getSecureExchangeStatusCode().equals(SecureExchangeStatusCode.INPROGRESS.toString())) {
      apiValidationErrors.add(createFieldError("secureExchangeStatusCode", secureExchange.getSecureExchangeStatusCode(), "secureExchangeStatusCode should be NEW or INPROGRESS for post operation."));
    }

    if (isCreateOperation && secureExchange.getSequenceNumber() != null){
      apiValidationErrors.add(createFieldError("sequenceNumber", secureExchange.getSequenceNumber(), "sequenceNumber should be null for post operation."));
    }

    if (!isCreateOperation && secureExchange.getSecureExchangeStatusCode() != null && secureExchangeStatusCodeTableRepository.findById(secureExchange.getSecureExchangeStatusCode()).isEmpty()) {
      apiValidationErrors.add(createFieldError("secureExchangeStatusCode", secureExchange.getSecureExchangeStatusCode(), "secureExchangeStatusCode value was not found as a valid status code."));
    }

    if (ministryOwnershipTeamRepository.findById(UUID.fromString(secureExchange.getMinistryOwnershipTeamID())).isEmpty()) {
      apiValidationErrors.add(createFieldError("ministryOwnershipTeamID", secureExchange.getMinistryOwnershipTeamID(), "ministryOwnershipTeamID value was not found as a valid team."));
    }

    if (secureExchangeContactTypeCodeTableRepository.findById(secureExchange.getSecureExchangeContactTypeCode()).isEmpty()) {
      apiValidationErrors.add(createFieldError("secureExchangeContactTypeCode", secureExchange.getSecureExchangeContactTypeCode(), "secureExchangeContactTypeCode value was not found as a valid contact type."));
    }

    return apiValidationErrors;
  }


  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("secureExchange", fieldName, rejectedValue, false, null, null, message);
  }

}
