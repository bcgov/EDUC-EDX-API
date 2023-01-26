package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
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
  UUID schoolID;

  /**
   * The School name.
   */
  String schoolName;

  /**
   * The district ID.
   */
  UUID districtID;

  /**
   * The district name.
   */
  String districtName;

  /**
   * Contact Type
   * Acceptable Values: SCHOOL, DISTRICT
   */
  @NotNull(message = "secureExchangeContactTypeCode cannot be null")
  @Size(max = 10)
  String secureExchangeContactTypeCode;

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
