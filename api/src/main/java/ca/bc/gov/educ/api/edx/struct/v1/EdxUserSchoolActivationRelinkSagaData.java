package ca.bc.gov.educ.api.edx.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxUserSchoolActivationRelinkSagaData extends EdxUserSchoolActivationInviteSagaData {
  private static final long serialVersionUID = -7847063658732692951L;

  @NotNull(message = "edxUserSchoolID cannot be null")
  String edxUserSchoolID;

}
