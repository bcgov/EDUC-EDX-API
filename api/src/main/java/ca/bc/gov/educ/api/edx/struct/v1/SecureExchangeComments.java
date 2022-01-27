package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SecureExchangeComments extends BaseRequest implements Serializable {
  private static final long serialVersionUID = -6904836038828419985L;

  String secureExchangeCommentID;
  String secureExchangeID;
  @Size(max = 50)
  String commentUserGUID;
  @Size(max = 255)
  String commentUserName;
  @NotNull(message = "Comment content can not be null")
  String content;

}
