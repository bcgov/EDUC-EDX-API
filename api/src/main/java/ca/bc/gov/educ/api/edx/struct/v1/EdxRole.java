package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxRole extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  @Size(max = 30)
  String edxRoleCode;
  @Size(max = 30)
  String label;
  @Size(max = 255)
  String roleDescription;
  @Pattern(regexp = "[YN]")
  String isDistrictRole;

  private List<EdxRolePermission> edxRolePermissions;
}





