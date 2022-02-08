package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode
@Data
public class SecureExchangeComments implements Serializable {
  private static final long serialVersionUID = -6904836038828419985L;

  String secureExchangeCommentID;
  @NotNull
  String secureExchangeID;
  String edxUserID;
  @Size(max = 255)
  String staffUserIdentifier;
  @Size(max = 255)
  @NotNull
  String commentUserName;
  @NotNull(message = "Comment content can not be null")
  String content;
  String commentTimestamp;

  @Size(max = 32)
  @NotNull(message = "createUser can not be null")
  String createUser;
  @Size(max = 32)
  @NotNull(message = "updateUser can not be null")
  String updateUser;
  @Null(message = "createDate should be null.")
  protected String createDate;
  @Null(message = "updateDate should be null.")
  protected String updateDate;
}
