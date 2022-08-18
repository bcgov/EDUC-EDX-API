package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxDistrictUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationRelinkSagaData;
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

  public EdxActivationCodeSagaDataPayLoadValidator(EdxRoleRepository edxRoleRepository) {
    this.edxRoleRepository = edxRoleRepository;
  }

  public List<FieldError> validateEdxActivationCodeSagaDataPayload(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    int rolesListInDBSize = getEdxRoleRepository().findAllById(edxUserActivationInviteSagaData.getEdxActivationRoleCodes()).size();
    if(rolesListInDBSize!= edxUserActivationInviteSagaData.getEdxActivationRoleCodes().size()){
      apiValidationErrors.add(createFieldError("edxActivationRoleIds", edxUserActivationInviteSagaData.getEdxActivationRoleCodes(), "Invalid Edx Roles in the payload"));
    }
    return apiValidationErrors;
  }

  public List<FieldError> validateDistrictUserEdxActivationCodeSagaDataPayload(EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    int rolesListInDBSize = getEdxRoleRepository().findAllById(edxDistrictUserActivationInviteSagaData.getEdxActivationRoleCodes()).size();
    if(rolesListInDBSize!= edxDistrictUserActivationInviteSagaData.getEdxActivationRoleCodes().size()){
      apiValidationErrors.add(createFieldError("edxActivationRoleIds", edxDistrictUserActivationInviteSagaData.getEdxActivationRoleCodes(), "Invalid Edx Roles in the payload"));
    }
    return apiValidationErrors;
  }

  public List<FieldError> validateEdxActivationCodeRelinkSagaDataPayload(EdxUserActivationRelinkSagaData edxUserActivationRelinkSagaData) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if(StringUtils.isBlank(edxUserActivationRelinkSagaData.getEdxUserId())){
      apiValidationErrors.add(createFieldError("edxUserID", edxUserActivationRelinkSagaData.getEdxUserId(), "EDX User ID must be provided for re-link"));
    }
    apiValidationErrors.addAll(validateEdxActivationCodeSagaDataPayload(edxUserActivationRelinkSagaData));
    return apiValidationErrors;
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("EdxActivationCodeSagaData", fieldName, rejectedValue, false, null, null, message);
  }
}
