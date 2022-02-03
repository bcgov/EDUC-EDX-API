package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SECURE_EXCHANGE_COMMENT")
@Getter
@Setter
public class SecureExchangeCommentEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "SECURE_EXCHANGE_COMMENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID secureExchangeCommentID;

  @Column(name = "SECURE_EXCHANGE_ID")
  UUID secureExchangeID;

  @Column(name = "COMMENT_USER_IDENTIFIER")
  String commentUserIdentifier;

  @Column(name = "COMMENT_USER_NAME")
  String commentUserName;

  @Column(name = "SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE")
  String commentUserTypeCode;

  @Column(name = "CONTENT")
  String content;

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

  @ManyToOne(cascade = CascadeType.ALL, optional = false, targetEntity = SecureExchangeEntity.class)
  @JoinColumn(name = "SECURE_EXCHANGE_ID", referencedColumnName = "SECURE_EXCHANGE_ID", updatable = false, insertable = false)
  private SecureExchangeEntity secureExchangeEntity;

}
