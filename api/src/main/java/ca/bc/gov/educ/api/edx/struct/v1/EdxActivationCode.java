package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxActivationCode extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String edxActivationCodeId;

  UUID schoolID;

  UUID districtID;

  String activationCode;

  String isPrimary;

  String expiryDate;

  private List<EdxActivationRole> edxActivationRoles;

  @Size(max = 255)
  @NotNull(message = "First Name cannot be null")
  String firstName;

  @Size(max = 255)
  @NotNull(message = "Last Name cannot be null")
  String lastName;

  @Size(max = 255)
  @NotNull(message = "Email cannot be null")
  @Email(message = "Email address should be a valid email address")
  String email;

  String validationCode;

  String isUrlClicked;

  String edxUserId;
}
