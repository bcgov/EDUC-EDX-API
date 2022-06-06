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
import java.util.HashSet;
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
  Boolean isPrimary;

  @NotNull(message = "expiryDate cannot be null")
  @Column(name = "EXPIRY_DATE")
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

  public Set<EdxActivationRoleEntity> getEdxActivationRoleEntities() {
    if (this.edxActivationRoleEntities == null) {
      this.edxActivationRoleEntities = new HashSet<>();
    }
    return this.edxActivationRoleEntities;
  }

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "edxActivationCodeEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxActivationRoleEntity.class)
  private Set<EdxActivationRoleEntity> edxActivationRoleEntities;

  @Column(name = "FIRST_NAME")
  String firstName;

  @Column(name = "LAST_NAME")
  String lastName;

  @Column(name = "EMAIL")
  String email;

  @NotNull
  @Column(name = "VALIDATION_CODE", columnDefinition = "BINARY(16)")
  UUID validationCode;

  @NotNull
  @Column(name = "IS_URL_CLICKED")
  Boolean isUrlClicked;


}
