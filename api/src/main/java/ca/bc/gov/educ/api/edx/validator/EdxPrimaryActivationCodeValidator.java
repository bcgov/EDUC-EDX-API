package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxPrimaryActivationCode;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class EdxPrimaryActivationCodeValidator {

    private static final String SCHOOL_ID_FIELD = "schoolID";
    private static final String DISTRICT_ID_FIELD = "districtID";
    public List<FieldError> validateEdxPrimaryActivationCode(InstituteTypeCode instituteType, String instituteIdentifier, EdxPrimaryActivationCode toValidate) {
        List<FieldError> toReturn = new ArrayList<>(this.validateSchoolIDAndDistrictIDFields(toValidate));
        if (instituteType == InstituteTypeCode.SCHOOL) {
            toReturn.addAll(this.validateEdxPrimaryActivationCodeForSchool(instituteIdentifier, toValidate));
        }
        if (instituteType == InstituteTypeCode.DISTRICT) {
            toReturn.addAll(this.validateEdxPrimaryActivationCodeForDistrict(instituteIdentifier, toValidate));
        }
        return toReturn;
    }

    private List<FieldError> validateSchoolIDAndDistrictIDFields(EdxPrimaryActivationCode toValidate) {
        List<FieldError> toReturn = new ArrayList<>();
        if (toValidate.getSchoolID() == null && toValidate.getDistrictID() == null) {
            toReturn.add(createFieldError(SCHOOL_ID_FIELD, toValidate.getSchoolID(), "Either schoolID or districtID should have a value specified."));
            toReturn.add(createFieldError(DISTRICT_ID_FIELD, toValidate.getDistrictID(), "Either schoolID or districtID should have a value specified."));
        }
        if (toValidate.getSchoolID() != null && toValidate.getDistrictID() != null) {
            toReturn.add(createFieldError(SCHOOL_ID_FIELD, toValidate.getSchoolID(), "The schoolID field shouldn't have a value specified when the districtID has a value."));
            toReturn.add(createFieldError(DISTRICT_ID_FIELD, toValidate.getDistrictID(), "The districtID field shouldn't have a value specified when the schoolID has a value."));
        }
        return toReturn;
    }

    private List<FieldError> validateEdxPrimaryActivationCodeForSchool(String instituteIdentifier, EdxPrimaryActivationCode toValidate) {
        List<FieldError> toReturn = new ArrayList<>();
        if (toValidate.getSchoolID() == null) {
            toReturn.add(createFieldError(SCHOOL_ID_FIELD, toValidate.getSchoolID(), "The schoolID field is expected to be not null for an EdxPrimaryActivationCode meant for a school."));
        }
        if (toValidate.getSchoolID() == null || !instituteIdentifier.equals(toValidate.getSchoolID().toString())) {
            toReturn.add(createFieldError(SCHOOL_ID_FIELD, toValidate.getSchoolID(), "The schoolID value is expected to match the value specified from the instituteIdentifier parameter."));
        }
        if (toValidate.getDistrictID() != null) {
            toReturn.add(createFieldError(DISTRICT_ID_FIELD, toValidate.getDistrictID(), "The districtID field is expected to be null for an EdxPrimaryActivationCode meant for a school."));
        }

        return toReturn;
    }

    private List<FieldError> validateEdxPrimaryActivationCodeForDistrict(String instituteIdentifier, EdxPrimaryActivationCode toValidate) {
        List<FieldError> toReturn = new ArrayList<>();
        if (toValidate.getDistrictID() == null) {
            toReturn.add(createFieldError(DISTRICT_ID_FIELD, toValidate.getSchoolID(), "The districtID field is expected to be not null for an EdxPrimaryActivationCode meant for a district."));
        }
        if (toValidate.getDistrictID() == null || !instituteIdentifier.equals(toValidate.getDistrictID().toString())) {
            toReturn.add(createFieldError(DISTRICT_ID_FIELD, toValidate.getSchoolID(), "The districtID value is expected to match the value specified from the instituteIdentifier parameter."));
        }
        if (toValidate.getSchoolID() != null) {
            toReturn.add(createFieldError(SCHOOL_ID_FIELD, toValidate.getDistrictID(), "The schoolID field is expected to be null for an EdxPrimaryActivationCode meant for a district."));
        }
        return toReturn;
    }
    private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
        return new FieldError("EdxPrimaryActivationCode", fieldName, rejectedValue, false, null, null, message);
    }
}
