package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "SECURE_EXCHANGE_USER_ROLE")
@DynamicUpdate
public class SecureExchangeUserRoleEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_USER_ROLE_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserDistrictID;

  @Column(name = "EDX_USER_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserID;

  @Column(name = "EDX_ROLE_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxRoleID;

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

  @ManyToOne(cascade = CascadeType.ALL, optional = false, targetEntity = SecureExchangeEntity.class)
  @JoinColumn(name = "EDX_USER_ID", referencedColumnName = "EDX_USER_ID", updatable = false, insertable = false)
  private SecureExchangeUserEntity secureExchangeUserEntity;
}