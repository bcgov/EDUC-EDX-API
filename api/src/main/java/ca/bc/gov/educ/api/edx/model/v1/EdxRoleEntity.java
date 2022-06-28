package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.utils.UpperCase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.Set;


@Data
@Entity
@Table(name = "EDX_ROLE")
@DynamicUpdate
public class EdxRoleEntity {
  @Id
  @Column(name = "EDX_ROLE_CODE", unique = true, updatable = false)
  String edxRoleCode;

  @Column(name = "LABEL")
  String label;

  @Column(name = "DESCRIPTION")
  String roleDescription;

  @NotNull(message = "isDistrictRole cannot be null")
  @UpperCase
  @Column(name = "IS_DISTRICT_ROLE")
  Boolean isDistrictRole;

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
  @OneToMany(mappedBy = "edxRoleCode", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxRolePermissionEntity.class)
  private Set<EdxRolePermissionEntity> edxRolePermissionEntities;

}
