package ca.bc.gov.educ.api.edx.validator;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(JUnitParamsRunner.class)
public class EmailValidatorTest {
  private final EmailValidator validator = new EmailValidator(Map.of("template1", "test template"));

  @Test
  @Parameters({
      "a@b.c, true",
      ".username@yahoo.com,true",
      "username@yahoo.com.,true",
      "username@yahoo..com,true",
      "1@2.3,true",
      "1@gov.bc.ca,false",
      "username@test.ca,false",
      "username@yahoo.com,false",
  })
  public void testValidateEmail_givenDifferentInputs_shouldReturnExpectedResult(final String email, final boolean expectedErrorSize) {
    val result = this.validator.isInvalidEmailAddress(email);
    assertThat (result).isEqualTo(expectedErrorSize);
  }
}
