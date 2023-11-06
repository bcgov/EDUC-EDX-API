package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.School;

import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;

@Component
public class CreateSchoolSagaPayloadValidator {
    public List<FieldError> validateCreateSchoolSagaPayload(CreateSchoolSagaData sagaData) {
        List<FieldError> apiValidationErrors = new ArrayList<>();
        List<String> categoriesRequiringAdmin = List.of("INDEPEND", "INDP_FNS");

        School school = sagaData.getSchool();
        EdxUser user = sagaData.getInitialEdxUser();

        if (user == null && categoriesRequiringAdmin.contains(school.getSchoolCategoryCode())) {
            String message = "Independent and offshore schools must be created with an initial user.";
            apiValidationErrors.add(createFieldError("initialEdxUser", null, message));
        }

        return apiValidationErrors;
    }

    private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
        return new FieldError("EdxUser", fieldName, rejectedValue, false, null, null, message);
    }
}
