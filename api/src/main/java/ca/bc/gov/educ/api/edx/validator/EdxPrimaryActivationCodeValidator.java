package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxPrimaryActivationCode;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class EdxPrimaryActivationCodeValidator {

    private static final String MINCODE_FIELD = "mincode";
    private static final String DISTRICT_CODE_FIELD = "districtCode";
    public List<FieldError> validateEdxPrimaryActivationCode(InstituteTypeCode instituteType, String instituteIdentifier, EdxPrimaryActivationCode toValidate) {
        List<FieldError> toReturn = new ArrayList<>();
        toReturn.addAll(this.validateMincodeAndDistrictCodeFields(toValidate));
        if (instituteType == InstituteTypeCode.SCHOOL) {
            toReturn.addAll(this.validateEdxPrimaryActivationCodeForSchool(instituteIdentifier, toValidate));
        }
        if (instituteType == InstituteTypeCode.DISTRICT) {
            toReturn.addAll(this.validateEdxPrimaryActivationCodeForDistrict(instituteIdentifier, toValidate));
        }
        return toReturn;
    }

    private List<FieldError> validateMincodeAndDistrictCodeFields(EdxPrimaryActivationCode toValidate) {
        List<FieldError> toReturn = new ArrayList<>();
        if (toValidate.getMincode() == null && toValidate.getDistrictId() == null) {
            toReturn.add(createFieldError(MINCODE_FIELD, toValidate.getMincode(), "Either mincode or districtCode should have a value specified."));
            toReturn.add(createFieldError(DISTRICT_CODE_FIELD, toValidate.getDistrictId(), "Either mincode or districtCode should have a value specified."));
        }
        if (toValidate.getMincode() != null && toValidate.getDistrictId() != null) {
            toReturn.add(createFieldError(MINCODE_FIELD, toValidate.getMincode(), "The mincode field shouldn't have a value specified when the districtCode has a value."));
            toReturn.add(createFieldError(DISTRICT_CODE_FIELD, toValidate.getDistrictId(), "The districtIdField shouldn't have a value specified when the mincode has a value."));
        }
        return toReturn;
    }

    private List<FieldError> validateEdxPrimaryActivationCodeForSchool(String instituteIdentifier, EdxPrimaryActivationCode toValidate) {
        List<FieldError> toReturn = new ArrayList<>();
        if (toValidate.getMincode() == null) {
            toReturn.add(createFieldError(MINCODE_FIELD, toValidate.getMincode(), "The mincode field is expected to be not null for an EdxPrimaryActivationCode meant for a school."));
        }
        if (!instituteIdentifier.equals(toValidate.getMincode())) {
            toReturn.add(createFieldError(MINCODE_FIELD, toValidate.getMincode(), "The mincode value is expected to match the value specified from the instituteIdentifier parameter."));
        }
        if (toValidate.getDistrictId() != null) {
            toReturn.add(createFieldError(DISTRICT_CODE_FIELD, toValidate.getDistrictId(), "The districtId field is expected to be null for an EdxPrimaryActivationCode meant for a school."));
        }
        return toReturn;
    }
    private List<FieldError> validateEdxPrimaryActivationCodeForDistrict(String instituteIdentifier, EdxPrimaryActivationCode toValidate) {
        List<FieldError> toReturn = new ArrayList<>();
        if (toValidate.getDistrictId() == null) {
            toReturn.add(createFieldError(DISTRICT_CODE_FIELD, toValidate.getMincode(), "The districtId field is expected to be not null for an EdxPrimaryActivationCode meant for a district."));
        }
        if (!instituteIdentifier.equals(toValidate.getDistrictId())) {
            toReturn.add(createFieldError(DISTRICT_CODE_FIELD, toValidate.getMincode(), "The districtId value is expected to match the value specified from the instituteIdentifier parameter."));
        }
        if (toValidate.getMincode() != null) {
            toReturn.add(createFieldError(MINCODE_FIELD, toValidate.getDistrictId(), "The mincode field is expected to be null for an EdxPrimaryActivationCode meant for a district."));
        }
        return toReturn;
    }
    private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
        return new FieldError("EdxPrimaryActivationCode", fieldName, rejectedValue, false, null, null, message);
    }
}
