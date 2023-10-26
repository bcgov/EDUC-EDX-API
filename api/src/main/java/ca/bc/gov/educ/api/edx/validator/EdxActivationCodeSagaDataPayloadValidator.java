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
public class EdxActivationCodeSagaDataPayloadValidator {
  @Getter(AccessLevel.PRIVATE)
  private final EdxRoleRepository edxRoleRepository;
  private static final String EDX_ACTIVATION_ROLE_CODE = "edxActivationRoleCode";
  private final ApplicationProperties props;

  public static final String DISTRICT_ID = "districtID";
  public static final String DISTRICT_NAME = "districtName";

  public static final String SCHOOL_ID = "schoolID";
  public static final String SCHOOL_NAME = "schoolName";
  public static final String FIRST_NAME = "firstName";
  public static final String LAST_NAME = "lastName";
  public static final String EMAIL = "email";

  public EdxActivationCodeSagaDataPayloadValidator(EdxRoleRepository edxRoleRepository, ApplicationProperties props) {
    this.edxRoleRepository = edxRoleRepository;
    this.props = props;
  }

  public List<FieldError> validateEdxSchoolUserActivationCodeSagaDataPayload(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    apiValidationErrors.addAll(validatePayload(edxUserActivationInviteSagaData));
    apiValidationErrors.addAll(validateEdxActivationCodes(edxUserActivationInviteSagaData.getEdxActivationRoleCodes()));
    return apiValidationErrors;
  }

  public List<FieldError> validateDistrictUserEdxActivationCodeSagaDataPayload(EdxUserDistrictActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    return new ArrayList<>(validateEdxActivationCodes(edxDistrictUserActivationInviteSagaData.getEdxActivationRoleCodes()));
  }

  public List<FieldError> validatePayload(EdxUserSchoolActivationInviteSagaData edxUserSchoolActivationInviteSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();

    if(edxUserSchoolActivationInviteSagaData.getSchoolID() == null) {
      apiValidationErrors.add(createFieldError(SCHOOL_ID, edxUserSchoolActivationInviteSagaData.getSchoolID(), "School ID cannot be null"));
    }

    if(edxUserSchoolActivationInviteSagaData.getSchoolName() == null) {
      apiValidationErrors.add(createFieldError(SCHOOL_NAME, edxUserSchoolActivationInviteSagaData.getSchoolName(), "School Name cannot be null"));
    }

    if(edxUserSchoolActivationInviteSagaData.getFirstName() == null) {
      apiValidationErrors.add(createFieldError(FIRST_NAME, edxUserSchoolActivationInviteSagaData.getFirstName(), "First Name cannot be null"));
    }

    if(edxUserSchoolActivationInviteSagaData.getLastName() == null) {
      apiValidationErrors.add(createFieldError(LAST_NAME, edxUserSchoolActivationInviteSagaData.getLastName(), "Last Name cannot be null"));
    }

    if(edxUserSchoolActivationInviteSagaData.getEmail() == null) {
      apiValidationErrors.add(createFieldError(EMAIL, edxUserSchoolActivationInviteSagaData.getEmail(), "Email cannot be null"));
    }

    return apiValidationErrors;
  }


  private List<FieldError> validateEdxActivationCodes(List<String> roles) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if(roles.isEmpty()){
      apiValidationErrors.add(createFieldError(EDX_ACTIVATION_ROLE_CODE, null, "Roles list cannot be empty."));
    }else {
      for (var role : roles) {
        if (!props.getAllowRolesList().contains(role)) {
          apiValidationErrors.add(createFieldError(EDX_ACTIVATION_ROLE_CODE, role, "edxActivationRoleCode is not valid according to the allow list."));
          break;
        }
      }
    }
    return apiValidationErrors;
  }

  public List<FieldError> validateEdxActivationCodeRelinkSchoolSagaDataPayload(EdxUserSchoolActivationRelinkSagaData edxUserActivationRelinkSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if(StringUtils.isBlank(edxUserActivationRelinkSagaData.getEdxUserId())){
      apiValidationErrors.add(createFieldError("edxUserID", edxUserActivationRelinkSagaData.getEdxUserId(), "EDX User ID must be provided for re-link"));
    }
    apiValidationErrors.addAll(validateEdxSchoolUserActivationCodeSagaDataPayload(edxUserActivationRelinkSagaData));
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
