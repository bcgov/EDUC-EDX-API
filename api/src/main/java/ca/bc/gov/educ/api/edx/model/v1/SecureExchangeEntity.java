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
@Table(name = "secure_exchange")
@DynamicUpdate
public class SecureExchangeEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "secure_exchange_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID secureExchangeID;

  @NotNull(message = "digitalID cannot be null")
  @Column(name = "digital_identity_id", updatable = false, columnDefinition = "BINARY(16)")
  UUID digitalID;

  @NotNull(message = "ownershipTeamID cannot be null")
  @Column(name = "ownership_team_id", updatable = false, columnDefinition = "BINARY(16)")
  UUID ownershipTeamID;

  @Column(name = "secure_exchange_status_code")
  String secureExchangeStatusCode;

  @UpperCase
  @Column(name = "exchange_contact")
  String exchangeContact;

  @UpperCase
  @Column(name = "exchange_contact_type_code")
  String exchangeContactTypeCode;

  @UpperCase
  @Column(name = "reviewer")
  String reviewer;

  @UpperCase
  @Column(name = "is_read_by_owner")
  String isReadByOwner;

  @UpperCase
  @Column(name = "is_read_by_exchange_contact")
  String isReadByExchangeContact;

  @PastOrPresent
  @Column(name = "INITIAL_SUBMIT_DATE")
  LocalDateTime initialSubmitDate;

  @PastOrPresent
  @Column(name = "STATUS_UPDATE_DATE")
  LocalDateTime statusUpdateDate;

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

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "secureExchangeEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = SecureExchangeCommentEntity.class)
  private Set<SecureExchangeCommentEntity> secureExchangeComment;
}
