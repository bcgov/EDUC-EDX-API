package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxUser extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String edxUserID;
  String digitalIdentityID;
  @Size(max = 255)
  String firstName;
  @Size(max = 255)
  String lastName;

  private List<EdxUserSchool> edxUserSchools;
  private List<EdxUserDistrict> edxUserDistricts;
}





