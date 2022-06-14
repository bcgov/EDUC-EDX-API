package ca.bc.gov.educ.api.edx.model.v1;

import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * The type Saga event.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "EDX_SERVICES_SAGA_EVENT_STATES")
@DynamicUpdate
public class SagaEventStatesEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
    @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "SAGA_EVENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  private UUID sagaEventId;

  @ManyToOne
  @JoinColumn(name = "SAGA_ID", updatable = false, columnDefinition = "BINARY(16)")
  private SagaEntity saga;

  @NotNull(message = "saga_event_state cannot be null")
  @Column(name = "SAGA_EVENT_STATE")
  private String sagaEventState;

  @NotNull(message = "saga_event_outcome cannot be null")
  @Column(name = "SAGA_EVENT_OUTCOME")
  private String sagaEventOutcome;

  @NotNull(message = "saga_step_number cannot be null")
  @Column(name = "SAGA_STEP_NUMBER")
  private Integer sagaStepNumber;

  @Column(name = "SAGA_EVENT_RESPONSE", length = 10485760)
  private String sagaEventResponse;

  @NotNull(message = "create user cannot be null")
  @Column(name = "CREATE_USER", updatable = false)
  private String createUser;

  @NotNull(message = "update user cannot be null")
  @Column(name = "UPDATE_USER")
  private String updateUser;

  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  private LocalDateTime createDate;

  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  private LocalDateTime updateDate;
}
