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
@Table(name = "SECURE_EXCHANGE")
@DynamicUpdate
public class SecureExchangeEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "SECURE_EXCHANGE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID secureExchangeID;

  @Column(name = "EDX_USER_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserID;

  @Column(name = "EDX_USER_SCHOOL_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserSchoolID;

  @Column(name = "EDX_USER_DISTRICT_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserDistrictID;

  @NotNull(message = "ministryOwnershipTeamID cannot be null")
  @Column(name = "EDX_MINISTRY_OWNERSHIP_TEAM_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID ministryOwnershipTeamID;

  @Column(name = "EDX_MINISTRY_CONTACT_TEAM_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID ministryContactTeamID;

  @UpperCase
  @Column(name = "SECURE_EXCHANGE_STATUS_CODE")
  String secureExchangeStatusCode;

  @UpperCase
  @Column(name = "REVIEWER")
  String reviewer;

  @NotNull(message = "subject cannot be null")
  @Column(name = "SUBJECT")
  String subject;

  @UpperCase
  @Column(name = "IS_READ_BY_MINISTRY")
  String isReadByMinistry;

  @UpperCase
  @Column(name = "IS_READ_BY_CONTACT")
  String isReadByExchangeContact;

  @PastOrPresent
  @Column(name = "SUBMISSION_TIMESTAMP")
  LocalDateTime initialSubmitDate;

  @PastOrPresent
  @Column(name = "STATUS_UPDATE_TIMESTAMP")
  LocalDateTime statusUpdateDate;

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
  @OneToMany(mappedBy = "secureExchangeEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = SecureExchangeCommentEntity.class)
  private Set<SecureExchangeCommentEntity> secureExchangeComment;
}
