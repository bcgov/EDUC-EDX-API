package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxUserSchoolRole extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String edxUserSchoolRoleID;

  @NotNull(message = "edxRoleCode cannot be null.")
  @Size(max = 32, message = "edxRoleCode should be no longer than 32 characters.")
  String edxRoleCode;

  @NotNull(message = "edxUserSchoolID cannot be null.")
  String edxUserSchoolID;
}





