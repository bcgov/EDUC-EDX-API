package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "secure_exchange_comment")
@Getter
@Setter
public class SecureExchangeCommentEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "secure_exchange_comment_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID secureExchangeCommentID;

  @Column(name = "secure_exchange_id")
  UUID secureExchangeID;

  @Column(name = "comment_user_GUID")
  String commentUserGUID;

  @Column(name = "comment_user_name")
  String commentUserName;

  @Column(name = "comment_user_type_code")
  String commentUserTypeCode;

  @Column(name = "content")
  String content;

  @Column(name = "create_user", updatable = false)
  String createUser;

  @PastOrPresent
  @Column(name = "create_date", updatable = false)
  LocalDateTime createDate;

  @Column(name = "update_user")
  String updateUser;

  @PastOrPresent
  @Column(name = "update_date")
  LocalDateTime updateDate;

  @ManyToOne(cascade = CascadeType.ALL, optional = false, targetEntity = SecureExchangeEntity.class)
  @JoinColumn(name = "secure_exchange_id", referencedColumnName = "secure_exchange_id", updatable = false, insertable = false)
  private SecureExchangeEntity secureExchangeEntity;

}
