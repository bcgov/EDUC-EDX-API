package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxActivateUser extends BaseRequest implements Serializable {

  @NotNull
  String primaryEdxCode;

  @NotNull
  String personalActivationCode;

  UUID schoolID;

  UUID districtID;

  @NotNull
  String digitalId;

  String edxUserId;
}
