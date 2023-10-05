package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.utils.UpperCase;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
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

  @Column(name = "SCHOOL_ID", columnDefinition = "BINARY(16)")
  UUID schoolID;

  @Column(name = "DISTRICT_ID", columnDefinition = "BINARY(16)")
  UUID districtID;

  @NotNull(message = "activationCode cannot be null")
  @Column(name = "ACTIVATION_CODE")
  String activationCode;

  @Column(name = "EDX_USER_ID")
  UUID edxUserId;

  @NotNull(message = "isPrimary cannot be null")
  @Column(name = "IS_PRIMARY")
  Boolean isPrimary;

  @Column(name = "EXPIRY_DATE")
  LocalDateTime expiryDate;

  @Column(name = "EDX_USER_EXPIRY_DATE")
  LocalDateTime edxUserExpiryDate;

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
  @UpperCase
  String firstName;

  @Column(name = "LAST_NAME")
  @UpperCase
  String lastName;

  @Column(name = "EMAIL")
  @UpperCase
  String email;

  @Column(name = "VALIDATION_CODE", columnDefinition = "BINARY(16)")
  UUID validationCode;

  @Column(name = "NUMBER_OF_LINK_CLICKS")
  Integer numberOfClicks;


}
