package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxActivateUser extends BaseRequest implements Serializable {

  @NotNull
  String primaryEdxCode;

  @NotNull
  String personalActivationCode;

  String mincode;

  String districtId;

  @NotNull
  String digitalId;

  String edxUserId;
}
