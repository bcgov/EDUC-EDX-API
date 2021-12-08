package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.utils.UpperCase;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Data
@Entity
@Table(name = "pen_retrieval_request")
@DynamicUpdate
public class PenRequestEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "pen_retrieval_request_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID penRequestID;

  @NotNull(message = "digitalID cannot be null")
  @Column(name = "digital_identity_id", updatable = false, columnDefinition = "BINARY(16)")
  UUID digitalID;

  @Column(name = "pen_retrieval_request_status_code")
  String penRequestStatusCode;

  @UpperCase
  @Column(name = "legal_first_name")
  String legalFirstName;

  @UpperCase
  @Column(name = "legal_middle_names")
  String legalMiddleNames;

  @UpperCase
  @NotNull(message = "legalLastName cannot be null")
  @Column(name = "legal_last_name")
  String legalLastName;

  @Column(name = "dob")
  LocalDate dob;

  @UpperCase
  @Column(name = "gender_code")
  String genderCode;

  @UpperCase
  @Column(name = "usual_first_name")
  String usualFirstName;

  @UpperCase
  @Column(name = "usual_middle_names")
  String usualMiddleName;

  @UpperCase
  @Column(name = "usual_last_name")
  String usualLastName;

  @UpperCase
  @Column(name = "email")
  String email;

  @UpperCase
  @Column(name = "email_verified")
  String emailVerified;

  @UpperCase
  @Column(name = "maiden_name")
  String maidenName;

  @UpperCase
  @Column(name = "past_names")
  String pastNames;

  @UpperCase
  @Column(name = "last_bc_school")
  String lastBCSchool;

  @UpperCase
  @Column(name = "last_bc_school_student_number")
  String lastBCSchoolStudentNumber;

  @UpperCase
  @Column(name = "current_school")
  String currentSchool;

  @UpperCase
  @Column(name = "reviewer")
  String reviewer;

  @Column(name = "failure_reason")
  String failureReason;

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

  @Column(name = "BCSC_AUTO_MATCH_OUTCOME")
  String bcscAutoMatchOutcome;

  @Column(name = "BCSC_AUTO_MATCH_DETAIL")
  String bcscAutoMatchDetails;

  @Column(name = "PEN")
  String pen;

  @UpperCase
  @Column(name = "demog_changed")
  String demogChanged;

  @Column(name = "complete_comment")
  String completeComment;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "penRequestEntity", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = PenRequestCommentsEntity.class)
  private Set<PenRequestCommentsEntity> penRequestComments;

}
