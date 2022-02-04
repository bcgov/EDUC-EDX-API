package ca.bc.gov.educ.api.edx.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import org.apache.commons.lang3.StringUtils;
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

  private final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Autowired
  public SecureExchangePayloadValidator(SecureExchangeService secureExchangeService, ApplicationProperties applicationProperties, MinistryOwnershipTeamRepository ministryOwnershipTeamRepository) {
    this.secureExchangeService = secureExchangeService;
    this.applicationProperties = applicationProperties;
    this.ministryOwnershipTeamRepository = ministryOwnershipTeamRepository;
  }

  public List<FieldError> validatePayload(SecureExchange secureExchange, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && secureExchange.getSecureExchangeID() != null) {
      apiValidationErrors.add(createFieldError("secureExchangeID", secureExchange.getSecureExchangeID(), "secureExchangeID should be null for post operation."));
    }

    if (isCreateOperation && secureExchange.getSecureExchangeStatusCode() != null) {
      apiValidationErrors.add(createFieldError("secureExchangeStatusCode", secureExchange.getSecureExchangeStatusCode(), "secureExchangeStatusCode should be null for post operation."));
    }

    if (ministryOwnershipTeamRepository.findById(UUID.fromString(secureExchange.getMinistryOwnershipTeamID())).isEmpty()) {
      apiValidationErrors.add(createFieldError("ministryOwnershipTeamID", secureExchange.getMinistryOwnershipTeamID(), "ministryOwnershipTeamID value was not found as a valid team."));
    }

    if (StringUtils.isNotEmpty(secureExchange.getMinistryContactTeamID()) && ministryOwnershipTeamRepository.findById(UUID.fromString(secureExchange.getMinistryContactTeamID())).isEmpty()) {
      apiValidationErrors.add(createFieldError("ministryContactTeamID", secureExchange.getMinistryContactTeamID(), "ministryContactTeamID value was not found as a valid team."));
    }

    return apiValidationErrors;
  }


  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("secureExchange", fieldName, rejectedValue, false, null, null, message);
  }

}
