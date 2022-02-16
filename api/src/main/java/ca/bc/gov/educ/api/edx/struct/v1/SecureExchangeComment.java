package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SecureExchangeComment extends BaseRequest implements Serializable {
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
}
