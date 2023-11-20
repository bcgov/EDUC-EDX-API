package ca.bc.gov.educ.api.edx.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSchoolSagaData extends EdxUserSchoolActivationInviteSagaData {
  private School school;
  private EdxUser initialEdxUser;
}
