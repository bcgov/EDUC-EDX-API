package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class EdxNewSecureExchangeOrchestratorService {

  private final EmailProperties emailProperties;

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeService secureExchangeService;

  private final ApplicationProperties props;

  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;

  @Getter(AccessLevel.PRIVATE)
  private final EmailNotificationService emailNotificationService;

  protected final SagaService sagaService;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService edxUsersService;

  public EdxNewSecureExchangeOrchestratorService(EmailProperties emailProperties, SecureExchangeService secureExchangeService, ApplicationProperties props, EmailNotificationService emailNotificationService, SagaService sagaService, EdxUsersService edxUsersService) {
    this.emailProperties = emailProperties;
    this.secureExchangeService = secureExchangeService;
    this.props = props;
    this.emailNotificationService = emailNotificationService;
    this.sagaService = sagaService;
    this.edxUsersService = edxUsersService;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendEmail(SecureExchangeCreateSagaData secureExchangeCreateSagaData) {
    // query to find all the users to whom it should be sent
    Set<String> emailIds = getEdxUsersService().findEdxUserEmailByMincodeAndPermissionCode(secureExchangeCreateSagaData.getMincode(),"SECURE_EXCHANGE");
    final var subject = emailProperties.getEdxNewSecureExchangeNotificationEmailSubject();
    final var from = emailProperties.getEdxSchoolUserActivationInviteEmailFrom();
    for(String emailId : emailIds){
      final var emailNotification = EmailNotification.builder()
        .fromEmail(from)
        .toEmail(emailId)
        .subject(subject)
        .templateName("edx.school.user.activation.invite")
        .emailFields(Map.of("schoolName", secureExchangeCreateSagaData.getSchoolName(), "ministryTeamName", secureExchangeCreateSagaData.getSchoolName(), "linkToEDX", props.getEdxApplicationBaseUrl()))
        .build();

      this.getEmailNotificationService().sendEmail(emailNotification);
    }


  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createNewSecureExchange(SecureExchangeCreateSagaData secureExchangeCreateSagaData, SagaEntity saga) {
    RequestUtil.setAuditColumnsForCreate(secureExchangeCreateSagaData.getSecureExchangeCreate());
    SecureExchangeEntity secureExchangeEntity = getSecureExchangeService().createSecureExchange(mapper.toModel(secureExchangeCreateSagaData.getSecureExchangeCreate()));
    try {
      updateSagaData(secureExchangeEntity,secureExchangeCreateSagaData,saga);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }

  }

  private void updateSagaDataInternal(SecureExchangeEntity secureExchangeEntity, SecureExchangeCreateSagaData secureExchangeCreateSagaData, SagaEntity sagaEntity) throws JsonProcessingException {
    secureExchangeCreateSagaData.setSecureExchangeId(secureExchangeEntity.getSecureExchangeID());
    sagaEntity.setSecureExchangeId(secureExchangeEntity.getSecureExchangeID());
    sagaEntity.setPayload(JsonUtil.getJsonStringFromObject(secureExchangeCreateSagaData)); // update the payload which will be updated in DB.
    this.sagaService.updateSagaRecord(sagaEntity); // save updated payload to DB again.
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateSagaData(SecureExchangeEntity secureExchangeEntity, SecureExchangeCreateSagaData secureExchangeCreateSagaData, SagaEntity sagaEntity) throws JsonProcessingException {
   updateSagaDataInternal(secureExchangeEntity,secureExchangeCreateSagaData,sagaEntity);
  }

}
