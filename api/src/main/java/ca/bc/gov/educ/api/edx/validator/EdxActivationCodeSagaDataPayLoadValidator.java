package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserDistrictActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserDistrictActivationRelinkSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationRelinkSagaData;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class EdxActivationCodeSagaDataPayLoadValidator {
  @Getter(AccessLevel.PRIVATE)
  private final EdxRoleRepository edxRoleRepository;
  private static final String EDX_ACTIVATION_ROLE_CODE = "edxActivationRoleCode";
  private final ApplicationProperties props;

  public EdxActivationCodeSagaDataPayLoadValidator(EdxRoleRepository edxRoleRepository, ApplicationProperties props) {
    this.edxRoleRepository = edxRoleRepository;
    this.props = props;
  }

  public List<FieldError> validateEdxActivationCodeSagaDataPayload(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    apiValidationErrors.addAll(validateEdxActivationCodes(edxUserActivationInviteSagaData.getEdxActivationRoleCodes()));
    return apiValidationErrors;
  }

  public List<FieldError> validateDistrictUserEdxActivationCodeSagaDataPayload(EdxUserDistrictActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    apiValidationErrors.addAll(validateEdxActivationCodes(edxDistrictUserActivationInviteSagaData.getEdxActivationRoleCodes()));
    return apiValidationErrors;
  }

  public List<FieldError> validateEdxActivationCodes(List<String> roles) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    for(var role: roles) {
      if (!props.getAllowRolesList().contains(role)) {
        apiValidationErrors.add(createFieldError(EDX_ACTIVATION_ROLE_CODE, role, "edxActivationRoleCode is not valid according to the allow list."));
        break;
      }
    }
    return apiValidationErrors;
  }

  public List<FieldError> validateEdxActivationCodeRelinkSchoolSagaDataPayload(EdxUserSchoolActivationRelinkSagaData edxUserActivationRelinkSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if(StringUtils.isBlank(edxUserActivationRelinkSagaData.getEdxUserId())){
      apiValidationErrors.add(createFieldError("edxUserID", edxUserActivationRelinkSagaData.getEdxUserId(), "EDX User ID must be provided for re-link"));
    }
    apiValidationErrors.addAll(validateEdxActivationCodeSagaDataPayload(edxUserActivationRelinkSagaData));
    return apiValidationErrors;
  }

  public List<FieldError> validateEdxActivationCodeRelinkSchoolSagaDataPayload(EdxUserDistrictActivationRelinkSagaData edxUserActivationRelinkSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if(StringUtils.isBlank(edxUserActivationRelinkSagaData.getEdxUserId())){
      apiValidationErrors.add(createFieldError("edxUserID", edxUserActivationRelinkSagaData.getEdxUserId(), "EDX User ID must be provided for re-link"));
    }
    apiValidationErrors.addAll(validateDistrictUserEdxActivationCodeSagaDataPayload(edxUserActivationRelinkSagaData));
    return apiValidationErrors;
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("EdxActivationCodeSagaData", fieldName, rejectedValue, false, null, null, message);
  }
}
