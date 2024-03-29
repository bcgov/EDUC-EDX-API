package ca.bc.gov.educ.api.edx.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxDistrictSchoolUserTombstone implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String edxUserID;

  String digitalIdentityID;

  String fullName;

  private List<String> schoolRoles;

  String email;
}





