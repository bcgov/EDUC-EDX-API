package ca.bc.gov.educ.api.edx.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxUserDistrictActivationRelinkSagaData extends EdxUserDistrictActivationInviteSagaData implements Serializable {
  private static final long serialVersionUID = -7847063658732692951L;

  @NotNull(message = "edxUserDistrictID cannot be null")
  String edxUserDistrictID;

}
