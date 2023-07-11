package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Student.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class District extends BaseRequest implements Serializable {
  /**
   * The constant serialVersionUID.
   */
  private static final long serialVersionUID = 1L;

  private String districtId;

  @Size(max = 3)
  @NotNull(message = "districtNumber can not be null.")
  private String districtNumber;

  @Size(max = 10)
  @Pattern(regexp = "^$|\\d{10}", message = "Invalid phone number format")
  private String faxNumber;

  @Size(max = 10)
  @Pattern(regexp = "^$|\\d{10}", message = "Invalid phone number format")
  private String phoneNumber;

  @Size(max = 255)
  @Email(message = "Email address should be a valid email address")
  private String email;

  @Size(max = 255)
  private String website;

  @Size(max = 255)
  @NotNull(message = "displayName cannot be null")
  private String displayName;

  @Size(max = 10)
  @NotNull(message = "districtRegionCode cannot be null")
  private String districtRegionCode;

  @Size(max = 10)
  @NotNull(message = "districtStatusCode cannot be null")
  private String districtStatusCode;

  @Valid
  private List<DistrictContact> contacts;

  @Valid
  private List<DistrictAddress> addresses;

  @Valid
  private List<Note> notes;
}
