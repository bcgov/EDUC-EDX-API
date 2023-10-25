package ca.bc.gov.educ.api.edx.struct.v1;

import java.io.Serializable;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSchoolSagaData extends BaseRequest implements Serializable {
  private School school;
  private Optional<EdxUser> initialEdxUser;
  private EdxUserSchoolActivationInviteSagaData inviteSagaData;
}
