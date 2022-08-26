package ca.bc.gov.educ.api.edx.struct.v1;


import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecureExchangeCreateSagaData extends BaseRequest implements Serializable {

  @NotNull(message = "SecureExchange cannot be null")
  SecureExchangeCreate secureExchangeCreate;

  @NotNull(message = "School ID cannot be null")
  UUID schoolID;

  @NotNull(message = "School Name cannot be null")
  String schoolName;

  @NotNull(message = "Ministry Team Name cannot be null")
  String ministryTeamName;

  @Null
  UUID secureExchangeId;


}
