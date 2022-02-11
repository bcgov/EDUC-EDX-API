package ca.bc.gov.educ.api.edx.model.v1;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "EDX_ACTIVATION_CODE")
@Getter
@Setter
public class EdxActivationCodeEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_ACTIVATION_CODE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID edxActivationCodeId;

  @Column(name = "MINCODE")
  String mincode;

  @Column(name = "DISTRICT_CODE")
  String districtCode;

  @NotNull(message = "activationCode cannot be null")
  @Column(name = "ACTIVATION_CODE")
  String activationCode;

  @NotNull(message = "isPrimary cannot be null")
  @Column(name = "IS_PRIMARY")
  String isPrimary;

  @NotNull(message = "expiryDate cannot be null")
  @PastOrPresent
  @Column(name = "EXPIRY_DATE", updatable = false)
  LocalDateTime expiryDate;

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

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "edxActivationCodeEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxActivationRoleEntity.class)
  private Set<EdxActivationRoleEntity> edxActivationRoleEntities;

}