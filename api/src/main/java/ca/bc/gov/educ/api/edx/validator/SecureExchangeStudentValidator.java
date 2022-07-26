package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;
import org.thymeleaf.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class SecureExchangeStudentValidator {

  public List<FieldError> validatePayload(final SecureExchangeStudent student) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (StringUtils.isEmpty(student.getStaffUserIdentifier()) && student.getEdxUserID() == null) {
      apiValidationErrors.add(createFieldError("staffUserIdentifier", student.getStaffUserIdentifier(), "staffUserIdentifier and edxUserID are both null, one must be provided"));
    }

    if (!StringUtils.isEmpty(student.getStaffUserIdentifier()) && student.getEdxUserID() != null) {
      apiValidationErrors.add(createFieldError("staffUserIdentifier", student.getStaffUserIdentifier(), "staffUserIdentifier and edxUserID both have values, only one must be provided"));
    }

    return apiValidationErrors;
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("secureExchange", fieldName, rejectedValue, false, null, null, message);
  }
}
