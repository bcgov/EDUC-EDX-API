package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxPermission extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String edxPermissionCode;
  @Size(max = 255)
  String permissionDescription;
  @Size(max = 30)
  String label;
}





