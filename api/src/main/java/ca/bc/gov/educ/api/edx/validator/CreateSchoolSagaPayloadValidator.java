package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateSchoolSagaPayloadValidator {
    public List<FieldError> validateCreateSchoolSagaPayload(CreateSchoolSagaData sagaData) {
        List<FieldError> apiValidationErrors = new ArrayList<>();

        School school = sagaData.getSchool();

        if(school.getSchoolCategoryCode().equalsIgnoreCase("OFFSHORE") && school.getAddresses().stream().anyMatch(schoolAddress -> schoolAddress.getAddressTypeCode().equalsIgnoreCase("PHYSICAL"))){
            String message = "Offshore schools cannot have a physical address.";
            apiValidationErrors.add(createFieldError("addresses", null, message));
        }

        return apiValidationErrors;
    }

    private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
        return new FieldError("EdxUser", fieldName, rejectedValue, false, null, null, message);
    }
}
