package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationRole;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class EdxActivationCodePayLoadValidator {

  public List<FieldError> validateEdxActivationCodePayload(EdxActivationCode edxActivationCode) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (edxActivationCode.getEdxActivationCodeId() != null) {
      apiValidationErrors.add(createFieldError("edxActivationCodeId", edxActivationCode.getEdxActivationCodeId(), "edxActivationCodeId should be null for post operation."));
    }
    apiValidationErrors.addAll(validateEdxActivationRoles(edxActivationCode.getEdxActivationRoles()));
    return apiValidationErrors;
  }

  private List<FieldError> validateEdxActivationRoles(List<EdxActivationRole> edxActivationRoles) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if(CollectionUtils.isEmpty(edxActivationRoles)){
      apiValidationErrors.add(createFieldError("edxActivationRoles", edxActivationRoles, "edxActivationRoles should be null for post operation."));
    }else{
      edxActivationRoles.forEach(el->
        apiValidationErrors.addAll(validateEdxActivationRole(el)));
    }
    return apiValidationErrors;
  }

  private List<FieldError> validateEdxActivationRole(EdxActivationRole activationRole) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (activationRole.getEdxActivationCodeId() != null) {
      apiValidationErrors.add(createFieldError("edxActivationCodeId", activationRole.getEdxActivationCodeId(), "edxActivationCodeId should be null for post operation."));
    }
    if (activationRole.getEdxActivationRoleId() != null) {
      apiValidationErrors.add(createFieldError("edxActivationRoleId", activationRole.getEdxActivationRoleId(), "edxActivationRoleId should be null for post operation."));
    }
    if (activationRole.getEdxRoleId() == null) {
      apiValidationErrors.add(createFieldError("edxRoleId", activationRole.getEdxActivationRoleId(), "edxRoleId should not be null for post operation."));
    }
    return apiValidationErrors;
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("EdxActivationCode", fieldName, rejectedValue, false, null, null, message);
  }
}
