package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class EdxUserPayLoadValidator {

    private static final String EDX_USER_ID = "edxUserID";
    private static final String EDX_ROLE_CODE = "edxRoleCode";
    private final ApplicationProperties props;

    public EdxUserPayLoadValidator(ApplicationProperties props) {
        this.props = props;
    }

    public List<FieldError> validateEdxUserPayload(EdxUser edxUser, boolean isCreateOperation) {
        final List<FieldError> apiValidationErrors = new ArrayList<>();
        if (isCreateOperation && edxUser.getEdxUserID() != null) {
            apiValidationErrors.add(createFieldError(EDX_USER_ID, edxUser.getEdxUserID(), "edxUserID should be null for post operation."));
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
           apiValidationErrors.add(createFieldError(EDX_USER_ID, edxUserSchool.getEdxUserID(), "edxUserID should not be null for post operation."));
        }
        if (!edxUserId.toString().equals(edxUserSchool.getEdxUserID())) {
           apiValidationErrors.add(createFieldError(EDX_USER_ID, edxUserSchool.getEdxUserSchoolID(), "edxUserID in path and payload edxUserId mismatch."));
        }
        if(edxUserSchool.getEdxUserSchoolRoles() != null) {
           for (var role : edxUserSchool.getEdxUserSchoolRoles()) {
              if (!props.getAllowRolesList().contains(role.getEdxRoleCode())) {
                 apiValidationErrors.add(createFieldError(EDX_ROLE_CODE, role.getEdxRoleCode(), "edxRoleCode is not valid according to the allow list."));
                 break;
              }
           }
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

    public List<FieldError> validateCreateEdxUserDistrictPayload(UUID edxUserId, EdxUserDistrict edxUserDistrict) {
        return validateEdxUserDistrictPayload(edxUserId,edxUserDistrict, true);
    }

    public List<FieldError> validateEdxUserDistrictPayload(UUID edxUserId, EdxUserDistrict edxUserDistrict, boolean isCreateOperation) {
        final List<FieldError> apiValidationErrors = new ArrayList<>();
        if (isCreateOperation && edxUserDistrict.getEdxUserDistrictID() != null) {
            apiValidationErrors.add(createFieldError("edxUserDistrictID", edxUserDistrict.getEdxUserDistrictID(), "edxUserDistrictID should be null for post operation."));
        }
        if (isCreateOperation && edxUserDistrict.getEdxUserID() == null) {
            apiValidationErrors.add(createFieldError(EDX_USER_ID, edxUserDistrict.getEdxUserID(), "edxUserID should not be null for post operation."));
        }
        if (!edxUserId.toString().equals(edxUserDistrict.getEdxUserID())) {
            apiValidationErrors.add(createFieldError(EDX_USER_ID, edxUserDistrict.getEdxUserDistrictID(), "edxUserID in path and payload edxUserId mismatch."));
        }
        return apiValidationErrors;
    }

    public List<FieldError> validateCreateEdxUserDistrictRolePayload(UUID edxUserDistrictId, EdxUserDistrictRole edxUserDistrictRole) {
        return validateEdxUserDistrictRolePayload(edxUserDistrictId,edxUserDistrictRole, true);
    }

    public List<FieldError> validateEdxUserDistrictRolePayload(UUID edxUserDistrictID, EdxUserDistrictRole edxUserDistrictRole, boolean isCreateOperation) {
        final List<FieldError> apiValidationErrors = new ArrayList<>();
        if (isCreateOperation && edxUserDistrictRole.getEdxUserDistrictRoleID() != null) {
            apiValidationErrors.add(createFieldError("edxUserDistrictRoleID", edxUserDistrictRole.getEdxUserDistrictRoleID(), "edxUserDistrictRoleID should be null for post operation."));
        }
        if(!edxUserDistrictID.toString().equals(edxUserDistrictRole.getEdxUserDistrictID())) {
            apiValidationErrors.add(createFieldError("edxUserDistrictID",edxUserDistrictID, "edxUserDistrictId in path and payload mismatch."));
        }
        return apiValidationErrors;
    }

}
