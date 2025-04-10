package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * The type Event.
 */
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Event {
  /**
   * The Event type.
   */
  private String eventType;
  /**
   * The Event outcome.
   */
  private String eventOutcome;
  /**
   * The Saga id.
   */
  private UUID sagaId;
  /**
   * The Reply to.
   */
  private String replyTo;
  /**
   * The Event payload.
   */
  private String eventPayload; // json string
}
