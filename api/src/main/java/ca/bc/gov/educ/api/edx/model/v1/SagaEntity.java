package ca.bc.gov.educ.api.edx.model.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;


@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "EDX_SAGA")
@DynamicUpdate
@JsonIgnoreProperties(ignoreUnknown = true)
public class SagaEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
    @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "SAGA_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  private UUID sagaId;

  @NotNull(message = "saga name cannot be null")
  @Column(name = "SAGA_NAME")
  private String sagaName;

  @NotNull(message = "saga state cannot be null")
  @Column(name = "SAGA_STATE")
  private String sagaState;

  @NotNull(message = "payload cannot be null")
  @Column(name = "PAYLOAD",  length = 10485760)
  private String payload;

  @NotNull(message = "status cannot be null")
  @Column(name = "STATUS")
  private String status;

  @Column(name = "SAGA_COMPENSATED")
  private Boolean sagaCompensated;

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

  /**
   * Edx user Id
   */
  @Column(name = "EDX_USER_ID", columnDefinition = "BINARY(16)")
  UUID edxUserId;

  @Column(name = "SECURE_ECHANGE_ID", columnDefinition = "BINARY(16)")
  UUID secureExchangeId;

  @Column(name = "MINCODE")
  String mincode;

  @Column(name = "EMAIL_ID")
  String emailId;

  @Column(name = "RETRY_COUNT")
  private Integer retryCount;

  public Boolean getSagaCompensated() {
    return this.sagaCompensated == null ?Boolean.FALSE:this.sagaCompensated;
  }
}
