package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;


@Data
@Entity
@Table(name = "EDX_PERMISSION")
@DynamicUpdate
public class EdxPermissionEntity {
  @Id
  @Column(name = "EDX_PERMISSION_CODE", unique = true, updatable = false)
  String edxPermissionCode;

  @Column(name = "DESCRIPTION")
  String permissionDescription;

  @Column(name = "LABEL")
  String label;

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

}
