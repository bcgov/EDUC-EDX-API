package ca.bc.gov.educ.api.edx.struct;

import lombok.Data;

import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

@Data
public abstract class BaseRequest {
  @Size(max = 100)
  protected String createUser;
  @Size(max = 100)
  protected String updateUser;
  @Null(message = "createDate should be null.")
  protected String createDate;
  @Null(message = "updateDate should be null.")
  protected String updateDate;
}
