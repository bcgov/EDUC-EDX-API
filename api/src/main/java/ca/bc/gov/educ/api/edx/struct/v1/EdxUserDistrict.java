package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxUserDistrict extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String edxUserDistrictID;
  String edxUserID;
  String districtId;

  private List<EdxUserDistrictRole> edxUserDistrictRoles;
}





