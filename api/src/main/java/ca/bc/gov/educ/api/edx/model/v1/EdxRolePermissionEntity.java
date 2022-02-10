package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.struct.v1.EdxRole;
import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "EDX_ROLE_PERMISSION")
@DynamicUpdate
public class EdxRolePermissionEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_ROLE_PERMISSION_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxRolePermissionId;

  @NotNull(message = "edxPermissionID cannot be null")
  @Column(name = "EDX_PERMISSION_ID")
  UUID edxPermissionID;

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

  @ManyToOne(cascade = CascadeType.ALL, optional = false, targetEntity = EdxRoleEntity.class)
  @JoinColumn(name = "EDX_ROLE_ID", referencedColumnName = "EDX_ROLE_ID", updatable = false)
  private EdxRole edxRole;

}
