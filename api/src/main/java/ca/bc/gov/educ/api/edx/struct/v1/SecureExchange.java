package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecureExchange extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String secureExchangeID;
  @NotNull(message = "contactIdentifier cannot be null")
  String contactIdentifier;
  @NotNull(message = "ministryOwnershipTeamID cannot be null")
  String ministryOwnershipTeamID;
  @NotNull(message = "secureExchangeContactTypeCode cannot be null")
  @Size(max = 10)
  String secureExchangeContactTypeCode;
  @Size(max = 10)
  String secureExchangeStatusCode;

  @Size(max = 255)
  private String reviewer;

  @Size(max = 4000)
  private String subject;

  Boolean isReadByMinistry;

  Boolean isReadByExchangeContact;

  String statusUpdateDate;

  String sequenceNumber;

  private List<SecureExchangeComment> commentsList;
}





