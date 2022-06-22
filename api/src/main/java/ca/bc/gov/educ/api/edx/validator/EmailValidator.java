package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Component
@SuppressWarnings("java:S5998")
public class EmailValidator {
  private static final String REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
  private static final Pattern PATTERN = Pattern.compile(REGEX);

  @Getter(AccessLevel.PRIVATE)
  private final Map<String, String> templateConfig;

  public EmailValidator(final Map<String, String> templateConfig) {
    this.templateConfig = templateConfig;
  }


  public List<FieldError> validateEmail(final EmailNotification email) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (!this.templateConfig.containsKey(email.getTemplateName())) {
      apiValidationErrors.add(this.createTemplateFieldError(email.getTemplateName(), "templateName"));
    }
    if (isInvalidEmailAddress(email.getFromEmail())) {
      apiValidationErrors.add(this.createEmailFieldError(email.getFromEmail(), "fromEmail"));
    }
    if (isInvalidEmailAddress(email.getToEmail())) {
      apiValidationErrors.add(this.createEmailFieldError(email.getToEmail(), "toEmail"));
    }
    return apiValidationErrors;
  }

  public boolean isInvalidEmailAddress(String email) {
    return !PATTERN.matcher(email).matches();
  }

  private FieldError createEmailFieldError(final Object rejectedValue, final String fieldName) {
    return new FieldError("email", fieldName, rejectedValue, false, null, null, fieldName + " should be a valid email address.");
  }

  private FieldError createTemplateFieldError(final Object rejectedValue, final String fieldName) {
    return new FieldError("email", fieldName, rejectedValue, false, null, null, "Not found the email template.");
  }
}
