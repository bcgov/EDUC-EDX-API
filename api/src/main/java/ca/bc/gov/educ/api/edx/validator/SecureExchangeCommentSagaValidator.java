package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.constants.SecureExchangeContactTypeCode;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCommentSagaData;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class SecureExchangeCommentSagaValidator {

    public static final String DISTRICT_ID = "districtID";
    public static final String DISTRICT_NAME = "districtName";
    public static final String SCHOOL_ID = "schoolID";
    public static final String SCHOOL_NAME = "schoolName";

    public List<FieldError> validateSecureExchangeCommentSagaPayload(SecureExchangeCommentSagaData secureExchangeCommentSagaData) {
        final List<FieldError> apiPayloadValidationErrors = new ArrayList<>();

        if(secureExchangeCommentSagaData.getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.SCHOOL.toString())) {
           if(secureExchangeCommentSagaData.getSchoolID() == null) {
               apiPayloadValidationErrors.add(createFieldError(SCHOOL_ID, secureExchangeCommentSagaData.getSchoolID(), "School ID cannot be null"));
           }

           if(secureExchangeCommentSagaData.getSchoolName() == null) {
               apiPayloadValidationErrors.add(createFieldError(SCHOOL_NAME, secureExchangeCommentSagaData.getSchoolName(), "School Name cannot be null"));
           }

        } else if(secureExchangeCommentSagaData.getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.DISTRICT.toString())) {
            if(secureExchangeCommentSagaData.getDistrictID() == null) {
                apiPayloadValidationErrors.add(createFieldError(DISTRICT_ID, secureExchangeCommentSagaData.getDistrictID(), "District ID cannot be null"));
            }

            if(secureExchangeCommentSagaData.getDistrictName() == null) {
                apiPayloadValidationErrors.add(createFieldError(DISTRICT_NAME, secureExchangeCommentSagaData.getDistrictName(), "District Name cannot be null"));
            }
        }
       return apiPayloadValidationErrors;
    }

    private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
        return new FieldError("secureExchangeCommentSagaData", fieldName, rejectedValue, false, null, null, message);
    }
}
