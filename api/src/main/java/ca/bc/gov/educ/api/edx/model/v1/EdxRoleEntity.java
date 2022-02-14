package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.utils.UpperCase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Data
@Entity
@Table(name = "EDX_ROLE")
@DynamicUpdate
public class EdxRoleEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_ROLE_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxRoleID;

  @NotNull(message = "roleName cannot be null")
  @Column(name = "NAME")
  String roleName;

  @Column(name = "DESCRIPTION")
  String roleDescription;

  @NotNull(message = "isDistrictRole cannot be null")
  @UpperCase
  @Column(name = "IS_DISTRICT_ROLE")
  String isDistrictRole;

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
  @OneToMany(mappedBy = "edxRoleEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxRolePermissionEntity.class)
  private Set<EdxRolePermissionEntity> edxRolePermissionEntities;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "edxRoleEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxUserSchoolRoleEntity.class)
  private Set<EdxUserSchoolRoleEntity> edxUserSchoolRoleEntities;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "edxRoleEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxUserDistrictRoleEntity.class)
  private Set<EdxUserDistrictRoleEntity> edxUserDistrictRoleEntities;

}
