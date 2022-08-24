package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchool;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolRole;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EdxUserPayLoadValidator {

    public List<FieldError> validateEdxUserPayload(EdxUser edxUser, boolean isCreateOperation) {
        final List<FieldError> apiValidationErrors = new ArrayList<>();
        if (isCreateOperation && edxUser.getEdxUserID() != null) {
            apiValidationErrors.add(createFieldError("edxUserID", edxUser.getEdxUserID(), "edxUserID should be null for post operation."));
        }
        return apiValidationErrors;
    }

    public List<FieldError> validateCreateEdxUserPayload(EdxUser edxUser) {
        return validateEdxUserPayload(edxUser, true);
    }

    private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
        return new FieldError("EdxUser", fieldName, rejectedValue, false, null, null, message);
    }

    public List<FieldError> validateCreateEdxUserSchoolPayload(UUID edxUserId, EdxUserSchool edxUserSchool) {
        return validateEdxUserSchoolPayload(edxUserId,edxUserSchool, true);
    }

    public List<FieldError> validateEdxUserSchoolPayload(UUID edxUserId, EdxUserSchool edxUserSchool, boolean isCreateOperation) {
        final List<FieldError> apiValidationErrors = new ArrayList<>();
        if (isCreateOperation && edxUserSchool.getEdxUserSchoolID() != null) {
            apiValidationErrors.add(createFieldError("edxUserSchoolID", edxUserSchool.getEdxUserSchoolID(), "edxUserSchoolID should be null for post operation."));
        }
        if (isCreateOperation && edxUserSchool.getEdxUserID() == null) {
            apiValidationErrors.add(createFieldError("edxUserID", edxUserSchool.getEdxUserID(), "edxUserID should not be null for post operation."));
        }
        if (!edxUserId.toString().equals(edxUserSchool.getEdxUserID())) {
            apiValidationErrors.add(createFieldError("edxUserId", edxUserSchool.getEdxUserSchoolID(), "edxUserID in path and payload edxUserId mismatch."));
        }
        return apiValidationErrors;
    }

    public List<FieldError> validateCreateEdxUserSchoolRolePayload(UUID edxUserSchoolId, EdxUserSchoolRole edxUserSchoolRole) {
        return validateEdxUserSchoolRolePayload(edxUserSchoolId,edxUserSchoolRole, true);
    }

    public List<FieldError> validateEdxUserSchoolRolePayload(UUID edxUserSchoolId, EdxUserSchoolRole edxUserSchoolRole, boolean isCreateOperation) {
        final List<FieldError> apiValidationErrors = new ArrayList<>();
        if (isCreateOperation && edxUserSchoolRole.getEdxUserSchoolRoleID() != null) {
            apiValidationErrors.add(createFieldError("edxUserSchoolRoleID", edxUserSchoolRole.getEdxUserSchoolRoleID(), "edxUserSchoolRoleID should be null for post operation."));
        }
        if(!edxUserSchoolId.toString().equals(edxUserSchoolRole.getEdxUserSchoolID())) {
            apiValidationErrors.add(createFieldError("edxUserSchoolId",edxUserSchoolId, "edxUserSchoolId in path and payload mismatch."));
        }
        return apiValidationErrors;
    }


}
