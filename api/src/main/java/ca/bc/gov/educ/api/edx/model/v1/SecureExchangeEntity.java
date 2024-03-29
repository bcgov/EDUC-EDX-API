package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.utils.UpperCase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Data
@Entity
@Table(name = "SECURE_EXCHANGE")
@SuperBuilder
@NoArgsConstructor
@DynamicUpdate
public class SecureExchangeEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "SECURE_EXCHANGE_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID secureExchangeID;

  @NotNull(message = "ministryOwnershipTeamID cannot be null")
  @Column(name = "EDX_MINISTRY_OWNERSHIP_TEAM_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID ministryOwnershipTeamID;

  @NotNull(message = "secureExchangeContactTypeCode cannot be null")
  @UpperCase
  @Column(name = "SECURE_EXCHANGE_CONTACT_TYPE_CODE")
  String secureExchangeContactTypeCode;

  @NotNull(message = "contactIdentifier cannot be null")
  @Column(name = "CONTACT_IDENTIFIER")
  String contactIdentifier;

  @NotNull(message = "secureExchangeStatusCode cannot be null")
  @UpperCase
  @Column(name = "SECURE_EXCHANGE_STATUS_CODE")
  String secureExchangeStatusCode;

  @UpperCase
  @Column(name = "REVIEWER")
  String reviewer;

  @NotNull(message = "subject cannot be null")
  @Column(name = "SUBJECT")
  String subject;

  @NotNull(message = "isReadByMinistry cannot be null")
  @UpperCase
  @Column(name = "IS_READ_BY_MINISTRY")
  Boolean isReadByMinistry;

  @NotNull(message = "isReadByExchangeContact cannot be null")
  @UpperCase
  @Column(name = "IS_READ_BY_CONTACT")
  Boolean isReadByExchangeContact;

  @NotNull(message = "statusUpdateDate cannot be null")
  @PastOrPresent
  @Column(name = "STATUS_UPDATE_TIMESTAMP")
  LocalDateTime statusUpdateDate;

  @NotNull(message = "createUser cannot be null")
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  @NotNull(message = "createDate cannot be null")
  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @NotNull(message = "updateUser cannot be null")
  @Column(name = "update_user")
  String updateUser;

  @NotNull(message = "updateDate cannot be null")
  @PastOrPresent
  @Column(name = "update_date")
  LocalDateTime updateDate;

  @Column(
    name="SEQUENCE_NUMBER",
    insertable = false,
    updatable=false
  )
  Integer sequenceNumber;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "secureExchangeEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = SecureExchangeCommentEntity.class)
  private Set<SecureExchangeCommentEntity> secureExchangeComment;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "secureExchangeEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = SecureExchangeStudentEntity.class)
  private Set<SecureExchangeStudentEntity> secureExchangeStudents;

  @OneToMany(mappedBy = "secureExchangeEntity", cascade = CascadeType.ALL, fetch = FetchType.LAZY, targetEntity = SecureExchangeDocumentEntity.class)
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private Set<SecureExchangeDocumentEntity> secureExchangeDocument;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "secureExchangeEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, targetEntity = SecureExchangeNoteEntity.class)
  private Set<SecureExchangeNoteEntity> secureExchangeNotes;

}
