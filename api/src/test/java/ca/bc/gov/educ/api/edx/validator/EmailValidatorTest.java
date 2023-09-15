package ca.bc.gov.educ.api.edx.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import lombok.val;

class EmailValidatorTest {
  private final EmailValidator validator = new EmailValidator(Map.of("template1", "test template"));

  @ParameterizedTest
  @CsvSource({
    "a@b.c,true",
    ".username@yahoo.com,true",
    "username@yahoo.com.,true",
    "username@yahoo..com,true",
    "1@2.3,true",
    "1@gov.bc.ca,false",
    "username@test.ca,false",
    "username@yahoo.com,false",
  })
  void testValidateEmail_givenDifferentInputs_shouldReturnExpectedResult(final String email, final boolean expectedErrorSize) {
    val result = this.validator.isInvalidEmailAddress(email);
    assertThat (result).isEqualTo(expectedErrorSize);
  }
}
