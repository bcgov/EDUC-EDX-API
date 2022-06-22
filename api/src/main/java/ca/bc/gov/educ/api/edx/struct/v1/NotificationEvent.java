package ca.bc.gov.educ.api.edx.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * The type Notification event.
 */
@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class NotificationEvent extends Event {
  /**
   * The Saga status.
   */
  private String sagaStatus;
  /**
   * The Saga name.
   */
  private String sagaName;
}
