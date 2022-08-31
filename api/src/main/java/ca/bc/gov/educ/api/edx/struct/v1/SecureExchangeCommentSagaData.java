package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.UUID;

/**
 * The type Secure exchange comment saga data.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecureExchangeCommentSagaData extends BaseRequest implements Serializable {

  /**
   * The Secure exchange comment.
   */
  @NotNull(message = "Secure Exchange Comment cannot be null")
  SecureExchangeComment secureExchangeComment;

  /**
   * The schoolID.
   */
  @NotNull(message = "schoolID cannot be null")
  UUID schoolID;

  /**
   * The School name.
   */
  @NotNull(message = "School Name cannot be null")
  String schoolName;

  /**
   * The Ministry team name.
   */
  @NotNull(message = "MinistryTeamName cannot be null")
  String ministryTeamName;

  /**
   * The Secure exchange id.
   */
  @NotNull(message = "SecureExchangeId cannot be null")
  UUID secureExchangeId;

  /**
   * The Sequence number.
   */
  @NotNull(message = "Sequence Number cannot be null")
  String sequenceNumber;

}
