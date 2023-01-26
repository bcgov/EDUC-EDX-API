package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.SecureExchangeContactTypeCode;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeCommentMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCommentSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

/**
 * The type Edx secure exchange comment orchestrator service.
 */
@Service
@Slf4j
public class EdxSecureExchangeCommentOrchestratorService {

  /**
   * The Email properties.
   */
  private final EmailProperties emailProperties;

  /**
   * The Props.
   */
  private final ApplicationProperties props;

  /**
   * The Email notification service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EmailNotificationService emailNotificationService;

  /**
   * The Saga service.
   */
  protected final SagaService sagaService;

  /**
   * The Comment service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeCommentService commentService;

  /**
   * The Edx users service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService edxUsersService;


  /**
   * The constant SECURE_EXCHANGE_COMMENT_MAPPER.
   */
  private static final SecureExchangeCommentMapper SECURE_EXCHANGE_COMMENT_MAPPER = SecureExchangeCommentMapper.mapper;

  /**
   * Instantiates a new Edx secure exchange comment orchestrator service.
   *
   * @param emailProperties          the email properties
   * @param props                    the props
   * @param emailNotificationService the email notification service
   * @param sagaService              the saga service
   * @param commentService           the comment service
   * @param edxUsersService          the edx users service
   */
  public EdxSecureExchangeCommentOrchestratorService(EmailProperties emailProperties, ApplicationProperties props, EmailNotificationService emailNotificationService, SagaService sagaService, SecureExchangeCommentService commentService, EdxUsersService edxUsersService) {
    this.emailProperties = emailProperties;
    this.props = props;
    this.emailNotificationService = emailNotificationService;
    this.sagaService = sagaService;
    this.commentService = commentService;
    this.edxUsersService = edxUsersService;
  }

  /**
   * Create secure exchange comment.
   *
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @param saga                          the saga
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createSecureExchangeComment(SecureExchangeCommentSagaData secureExchangeCommentSagaData, SagaEntity saga) {
    RequestUtil.setAuditColumnsForCreate(secureExchangeCommentSagaData.getSecureExchangeComment());
    SecureExchangeCommentEntity secureExchangeCommentEntity = getCommentService().save(secureExchangeCommentSagaData.getSecureExchangeId(), SECURE_EXCHANGE_COMMENT_MAPPER.toModel(secureExchangeCommentSagaData.getSecureExchangeComment()));
    try {
      updateSagaDataInternal(secureExchangeCommentEntity, secureExchangeCommentSagaData, saga);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }

  }

  /**
   * Send email.
   *
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendEmail(SecureExchangeCommentSagaData secureExchangeCommentSagaData) throws JsonProcessingException {
    Set<String> emailIds = null;
    // query to find all the users to whom it should be sent
    if(secureExchangeCommentSagaData.getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.SCHOOL.toString())) {
      emailIds = getEdxUsersService().findEdxUserEmailBySchoolIDAndPermissionCode(secureExchangeCommentSagaData.getSchoolID(), "SECURE_EXCHANGE");
    } else if(secureExchangeCommentSagaData.getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.DISTRICT.toString())) {
      emailIds = getEdxUsersService().findEdxUserEmailByDistrictIDAndPermissionCode(secureExchangeCommentSagaData.getDistrictID(), "SECURE_EXCHANGE");
    }

    final var subject = emailProperties.getEdxSecureExchangeCommentNotificationEmailSubject();
    final var from = emailProperties.getEdxSchoolUserActivationInviteEmailFrom();
    for (String emailId : emailIds) {
      final var emailNotification = EmailNotification.builder()
        .fromEmail(from)
        .toEmail(emailId)
        .subject(subject)
        .templateName("edx.secure.exchange.comment.notification")
        .emailFields(secureExchangeCommentSagaData.getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.SCHOOL.toString()) ? Map.of("schoolName", secureExchangeCommentSagaData.getSchoolName(), "ministryTeamName", secureExchangeCommentSagaData.getMinistryTeamName(), "linkToEDX", props.getEdxApplicationBaseUrl(),"messageSequenceNumber",secureExchangeCommentSagaData.getSequenceNumber()) : Map.of("districtName", secureExchangeCommentSagaData.getDistrictName(), "ministryTeamName", secureExchangeCommentSagaData.getMinistryTeamName(), "linkToEDX", props.getEdxApplicationBaseUrl(),"messageSequenceNumber",secureExchangeCommentSagaData.getSequenceNumber()))
        .build();

      this.getEmailNotificationService().sendEmail(emailNotification);
    }
  }

  /**
   * Update saga data.
   *
   * @param secureExchangeCommentEntity   the secure exchange comment entity
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @param saga                          the saga
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateSagaData(SecureExchangeCommentEntity secureExchangeCommentEntity, SecureExchangeCommentSagaData secureExchangeCommentSagaData, SagaEntity saga) throws JsonProcessingException {
    updateSagaDataInternal(secureExchangeCommentEntity, secureExchangeCommentSagaData, saga);
  }

  /**
   * Update saga data internal.
   *
   * @param secureExchangeCommentEntity   the secure exchange comment entity
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @param sagaEntity                    the saga entity
   * @throws JsonProcessingException the json processing exception
   */
  private void updateSagaDataInternal(SecureExchangeCommentEntity secureExchangeCommentEntity, SecureExchangeCommentSagaData secureExchangeCommentSagaData, SagaEntity sagaEntity) throws JsonProcessingException {
    secureExchangeCommentSagaData.setSecureExchangeComment(SECURE_EXCHANGE_COMMENT_MAPPER.toStructure(secureExchangeCommentEntity));
    sagaEntity.setPayload(JsonUtil.getJsonStringFromObject(secureExchangeCommentSagaData)); // update the payload which will be updated in DB.
    this.sagaService.updateSagaRecord(sagaEntity); // save updated payload to DB again.
  }

  public Optional<SecureExchangeCommentEntity> findCommentForSecureExchange(SecureExchangeCommentSagaData secureExchangeCommentSagaData) {
   return commentService.findCommentForSecureExchange(LocalDateTime.parse(secureExchangeCommentSagaData.getSecureExchangeComment().getCommentTimestamp()), secureExchangeCommentSagaData.getSecureExchangeComment().getCommentUserName(), UUID.fromString(secureExchangeCommentSagaData.getSecureExchangeComment().getSecureExchangeID()), secureExchangeCommentSagaData.getSecureExchangeComment().getContent());
  }
}
