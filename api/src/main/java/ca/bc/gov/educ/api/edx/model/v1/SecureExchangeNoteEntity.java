package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "SECURE_EXCHANGE_NOTE")
@Getter
@Setter
public class SecureExchangeNoteEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "SECURE_EXCHANGE_NOTE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID secureExchangeNoteID;

  @Column(name = "SECURE_EXCHANGE_ID")
  UUID secureExchangeID;

  @NotNull(message = "staffUserIdentifier cannot be null")
  @Column(name = "STAFF_USER_IDENTIFIER")
  String staffUserIdentifier;

  @NotNull(message = "staffUserName cannot be null")
  @Column(name = "STAFF_USER_NAME")
  String staffUserName;

  @NotNull(message = "content cannot be null")
  @Column(name = "CONTENT")
  String content;

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

  @ManyToOne(cascade = CascadeType.ALL, optional = false, targetEntity = SecureExchangeEntity.class)
  @JoinColumn(name = "SECURE_EXCHANGE_ID", referencedColumnName = "SECURE_EXCHANGE_ID", updatable = false, insertable = false)
  private SecureExchangeEntity secureExchangeEntity;

}