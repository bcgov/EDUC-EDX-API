package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EDX_ACTIVATION_ROLE", uniqueConstraints = {@UniqueConstraint(name = "EDX_ACTIVATION_CODE_ID_EDX_ROLE_CODE_UK", columnNames = {"EDX_ACTIVATION_CODE_ID", "EDX_ROLE_CODE"})})
@Getter
@Setter
public class EdxActivationRoleEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_ACTIVATION_ROLE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID edxActivationRoleId;

  @Column(name = "EDX_ROLE_CODE")
  String edxRoleCode;

  @NotNull(message = "createUser cannot be null")
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  @NotNull(message = "createDate cannot be null")
  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @NotNull(message = "updateUser cannot be null")
  @Column(name = "UPDATE_USER")
  String updateUser;

  @NotNull(message = "updateDate cannot be null")
  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;

  @ManyToOne(cascade = CascadeType.ALL, optional = false, targetEntity = EdxActivationCodeEntity.class)
  @JoinColumn(name = "EDX_ACTIVATION_CODE_ID", referencedColumnName = "EDX_ACTIVATION_CODE_ID", updatable = false)
  private EdxActivationCodeEntity edxActivationCodeEntity;

}
