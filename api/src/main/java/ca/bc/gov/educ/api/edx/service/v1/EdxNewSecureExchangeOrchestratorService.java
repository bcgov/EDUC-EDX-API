package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.SecureExchangeContactTypeCode;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The type Edx new secure exchange orchestrator service.
 */
@Service
@Slf4j
public class EdxNewSecureExchangeOrchestratorService {

  /**
   * The Email properties.
   */
  private final EmailProperties emailProperties;

  /**
   * The Secure exchange service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeService secureExchangeService;

  /**
   * The Props.
   */
  private final ApplicationProperties props;

  /**
   * The constant mapper.
   */
  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;

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
   * The Edx users service.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService edxUsersService;

  /**
   * Instantiates a new Edx new secure exchange orchestrator service.
   *
   * @param emailProperties          the email properties
   * @param secureExchangeService    the secure exchange service
   * @param props                    the props
   * @param emailNotificationService the email notification service
   * @param sagaService              the saga service
   * @param edxUsersService          the edx users service
   */
  public EdxNewSecureExchangeOrchestratorService(EmailProperties emailProperties, SecureExchangeService secureExchangeService, ApplicationProperties props, EmailNotificationService emailNotificationService, SagaService sagaService, EdxUsersService edxUsersService) {
    this.emailProperties = emailProperties;
    this.secureExchangeService = secureExchangeService;
    this.props = props;
    this.emailNotificationService = emailNotificationService;
    this.sagaService = sagaService;
    this.edxUsersService = edxUsersService;
  }

  /**
   * Send email.
   *
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendEmail(SecureExchangeCreateSagaData secureExchangeCreateSagaData) {
    Set<String> emailIds = null;

    if(secureExchangeCreateSagaData.getSecureExchangeCreate().getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.SCHOOL.toString())) {
      emailIds = getEdxUsersService().findEdxUserEmailBySchoolIDAndPermissionCode(secureExchangeCreateSagaData.getSchoolID(), "SECURE_EXCHANGE");
    } else if(secureExchangeCreateSagaData.getSecureExchangeCreate().getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.DISTRICT.toString())) {
      emailIds = getEdxUsersService().findEdxUserEmailByDistrictIDAndPermissionCode(secureExchangeCreateSagaData.getDistrictID(), "SECURE_EXCHANGE");
    }

    final var subject = emailProperties.getEdxNewSecureExchangeNotificationEmailSubject();
    final var from = emailProperties.getEdxSchoolUserActivationInviteEmailFrom();
    for(String emailId : emailIds){
      final var emailNotification = EmailNotification.builder()
        .fromEmail(from)
        .toEmail(emailId)
        .subject(subject)
        .templateName("edx.new.secure.exchange.notification")
        .emailFields(secureExchangeCreateSagaData.getSecureExchangeCreate().getSecureExchangeContactTypeCode().equals(SecureExchangeContactTypeCode.SCHOOL.toString()) ? Map.of("schoolName", secureExchangeCreateSagaData.getSchoolName(), "ministryTeamName", secureExchangeCreateSagaData.getMinistryTeamName(), "linkToEDX", props.getEdxApplicationBaseUrl()): Map.of("districtName", secureExchangeCreateSagaData.getDistrictName(), "ministryTeamName", secureExchangeCreateSagaData.getMinistryTeamName(), "linkToEDX", props.getEdxApplicationBaseUrl()))
        .build();

      this.getEmailNotificationService().sendEmail(emailNotification);
    }
  }

  /**
   * Create new secure exchange.
   *
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   * @param saga                         the saga
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createNewSecureExchange(SecureExchangeCreateSagaData secureExchangeCreateSagaData, SagaEntity saga) {
    RequestUtil.setAuditColumnsForCreate(secureExchangeCreateSagaData.getSecureExchangeCreate());
    SecureExchangeEntity secureExchangeEntity = getSecureExchangeService().createSecureExchange(mapper.toModel(secureExchangeCreateSagaData.getSecureExchangeCreate()));
    try {
      updateSagaDataInternal(secureExchangeEntity,secureExchangeCreateSagaData,saga);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }
  public SecureExchangeEntity getSecureExchangeById(UUID secureExchangeID){
    return this.getSecureExchangeService().retrieveSecureExchange(secureExchangeID);
  }

  /**
   * Update saga data internal.
   *
   * @param secureExchangeEntity         the secure exchange entity
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   * @param sagaEntity                   the saga entity
   * @throws JsonProcessingException the json processing exception
   */
  private void updateSagaDataInternal(SecureExchangeEntity secureExchangeEntity, SecureExchangeCreateSagaData secureExchangeCreateSagaData, SagaEntity sagaEntity) throws JsonProcessingException {
    secureExchangeCreateSagaData.setSecureExchangeId(secureExchangeEntity.getSecureExchangeID());
    sagaEntity.setSecureExchangeId(secureExchangeEntity.getSecureExchangeID());
    sagaEntity.setPayload(JsonUtil.getJsonStringFromObject(secureExchangeCreateSagaData)); // update the payload which will be updated in DB.
    this.sagaService.updateSagaRecord(sagaEntity); // save updated payload to DB again.
  }

  /**
   * Update saga data.
   *
   * @param secureExchangeEntity         the secure exchange entity
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   * @param sagaEntity                   the saga entity
   * @throws JsonProcessingException the json processing exception
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateSagaData(SecureExchangeEntity secureExchangeEntity, SecureExchangeCreateSagaData secureExchangeCreateSagaData, SagaEntity sagaEntity) throws JsonProcessingException {
   updateSagaDataInternal(secureExchangeEntity,secureExchangeCreateSagaData,sagaEntity);
  }

}
