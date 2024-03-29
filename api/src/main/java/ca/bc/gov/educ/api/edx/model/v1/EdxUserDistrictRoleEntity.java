package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "EDX_USER_DISTRICT_ROLE", uniqueConstraints = {@UniqueConstraint(name = "EDX_USER_DISTRICT_ID_EDX_ROLE_UK", columnNames = {"EDX_USER_DISTRICT_ID", "EDX_ROLE_CODE"})})
@DynamicUpdate
public class EdxUserDistrictRoleEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_USER_DISTRICT_ROLE_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserDistrictRoleID;

  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @Column(name = "UPDATE_USER")
  String updateUser;

  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;

  @ManyToOne(optional = false, targetEntity = EdxUserDistrictEntity.class)
  @JoinColumn(name = "EDX_USER_DISTRICT_ID", referencedColumnName = "EDX_USER_DISTRICT_ID", updatable = false)
  private EdxUserDistrictEntity edxUserDistrictEntity;

  @Column(name = "EDX_ROLE_CODE")
  private String edxRoleCode;

  @PreRemove
  public void preRemove() {
    if(this.edxUserDistrictEntity != null) {
      this.edxUserDistrictEntity.getEdxUserDistrictRoleEntities().remove(this);
      this.edxUserDistrictEntity = null;
    }
  }
}
