package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

@Setter
@Getter
@ToString
public abstract class BaseEmail {

  @NotNull(message = "Email address can not be null.")
  @Email(message = "Email address should be a valid email address")
  private String emailAddress;
  /**
   * This holds the identity type code of the student , BASIC or BCSC or PERSONAL
   */
  private String identityType;
}
