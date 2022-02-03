package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecureExchange extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String secureExchangeID;
  String edxUserID;
  String edxUserSchoolID;
  String edxUserDistrictID;
  @NotNull(message = "ministryOwnershipTeamID cannot be null")
  String ministryOwnershipTeamID;
  String ministryContactTeamID;
  @Size(max = 10)
  String secureExchangeStatusCode;

  @Size(max = 255)
  private String reviewer;

  @Size(max = 4000)
  private String subject;

  @Size(max = 1)
  @Pattern(regexp = "[YN]")
  String isReadByMinistry;

  @Size(max = 1)
  @Pattern(regexp = "[YN]")
  String isReadByExchangeContact;

  String initialSubmitDate;

  String statusUpdateDate;
}





