package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxUserActivationInviteSagaData extends BaseRequest implements Serializable {
  private static final long serialVersionUID = -7847063658732692951L;
  @NotNull(message = "schoolID cannot be null")
  UUID schoolID;

  @NotNull(message = "School Name cannot be null")
  String schoolName;

  @NotEmpty(message = "Activation Roles cannot be null or empty")
  private List<String> edxActivationRoleCodes;

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

  //below fields are generated by the system should not be sent from the UI

  @Null
  String personalActivationCode;
  @Null
  String validationCode;

  @Null
  String edxActivationCodeId;

  @Null
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
  LocalDateTime expiryDate;

  String edxUserId;

}
