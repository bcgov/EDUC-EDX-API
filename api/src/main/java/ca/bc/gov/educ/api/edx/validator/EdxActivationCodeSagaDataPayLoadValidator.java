package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import lombok.AccessLevel;
import lombok.Getter;
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
    int rolesListInDBSize = getEdxRoleRepository().findAllById(edxUserActivationInviteSagaData.getEdxActivationRoleIds()).size();
    if(rolesListInDBSize!= edxUserActivationInviteSagaData.getEdxActivationRoleIds().size()){
      apiValidationErrors.add(createFieldError("edxActivationRoleIds", edxUserActivationInviteSagaData.getEdxActivationRoleIds(), "Invalid Edx Roles in the payload"));
    }
    return apiValidationErrors;
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("EdxActivationCodeSagaData", fieldName, rejectedValue, false, null, null, message);
  }
}
