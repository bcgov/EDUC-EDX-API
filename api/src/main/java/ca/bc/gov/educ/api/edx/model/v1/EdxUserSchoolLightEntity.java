package ca.bc.gov.educ.api.edx.model.v1;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Data
@Entity
@Table(name = "EDX_USER_SCHOOL", uniqueConstraints = {@UniqueConstraint(name = "EDX_USER_ID_SCHOOL_ID_UK", columnNames = {"EDX_USER_ID", "SCHOOL_ID"})})
@DynamicUpdate
public class EdxUserSchoolLightEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
    @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_USER_SCHOOL_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserSchoolID;

  @Column(name = "EDX_USER_ID", columnDefinition = "BINARY(16)")
  UUID edxUserID;

  @Column(name = "SCHOOL_ID", columnDefinition = "BINARY(16)")
  UUID schoolID;

  @Column(name = "EXPIRY_DATE")
  LocalDateTime expiryDate;

  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @Column(name = "update_user")
  String updateUser;

  @PastOrPresent
  @Column(name = "update_date")
  LocalDateTime updateDate;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "edxUserSchoolEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxUserSchoolRoleEntity.class)
  private Set<EdxUserSchoolRoleEntity> edxUserSchoolRoleEntities;

  public Set<EdxUserSchoolRoleEntity> getEdxUserSchoolRoleEntities() {
    if (this.edxUserSchoolRoleEntities == null) {
      this.edxUserSchoolRoleEntities = new HashSet<>();
    }
    return this.edxUserSchoolRoleEntities;
  }

}
