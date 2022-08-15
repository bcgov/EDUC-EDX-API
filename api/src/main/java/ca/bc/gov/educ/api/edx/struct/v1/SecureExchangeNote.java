package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class SecureExchangeNote extends BaseRequest implements Serializable {

  private static final long serialVersionUID = -4520013126634534781L;

  String secureExchangeNoteID;
  @NotNull
  String secureExchangeID;
  @NotNull
  @Size(max = 255)
  String staffUserIdentifier;
  @Size(max = 255)
  @NotNull(message = "Comment content can not be null")
  String content;
  @Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}T\\d{1,2}:\\d{1,2}:\\d{1,2}", message = "Expected pattern is yyyy-mm-ddTHH:MM:SS")
  String noteTimestamp;
}
