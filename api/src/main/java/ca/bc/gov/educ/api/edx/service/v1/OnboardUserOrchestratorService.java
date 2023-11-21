package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.repository.EdxActivationCodeRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.InstituteTypeCode.SCHOOL;

@Service
@Slf4j
public class OnboardUserOrchestratorService {

  protected final SagaService sagaService;

  private final EdxActivationCodeRepository edxActivationCodeRepository;

  private final EdxUsersService service;

  private final EmailProperties emailProperties;
  private final EmailNotificationService emailNotificationService;
  private final RestUtils restUtils;

  public OnboardUserOrchestratorService(
    SagaService sagaService,
    EdxActivationCodeRepository edxActivationCodeRepository,
    EdxUsersService service,
    EmailProperties emailProperties,
    EmailNotificationService emailNotificationService,
    RestUtils restUtils
  ) {
    this.sagaService = sagaService;
    this.edxActivationCodeRepository = edxActivationCodeRepository;
    this.service = service;
    this.emailProperties = emailProperties;
    this.emailNotificationService = emailNotificationService;
    this.restUtils = restUtils;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createPrimaryActivationCode(OnboardUserSagaData sagaData) {
    EdxPrimaryActivationCode edxPrimaryActivationCode = new EdxPrimaryActivationCode();
    UUID schoolId = sagaData.getSchoolID();
    edxPrimaryActivationCode.setSchoolID(schoolId);
    RequestUtil.setAuditColumnsForCreate(edxPrimaryActivationCode);

    this.service.generateOrRegeneratePrimaryEdxActivationCode(SCHOOL, schoolId.toString(), edxPrimaryActivationCode);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendPrimaryActivationCodeNotification(OnboardUserSagaData sagaData) {
    UUID schoolId = sagaData.getSchoolID();

    Optional<EdxActivationCodeEntity> edxActivationCodeEntity =
      edxActivationCodeRepository.findEdxActivationCodeEntitiesBySchoolIDAndIsPrimaryTrueAndDistrictIDIsNull(schoolId);
    final String recipient = (sagaData.getFirstName()
      + " " + sagaData.getLastName()).trim();

    EmailNotification emailNotification = EmailNotification.builder()
      .fromEmail(this.emailProperties.getEdxSchoolUserActivationInviteEmailFrom())
      .toEmail(sagaData.getEmail())
      .subject(this.emailProperties.getEdxSecureExchangePrimaryCodeNotificationEmailSubject())
      .templateName("edx.school.primary-code.notification")
      .emailFields(Map.of(
        "recipient", recipient,
        "minCode", sagaData.getMincode(),
        "instituteName", sagaData.getSchoolName(),
        "primaryCode", edxActivationCodeEntity.orElseThrow().getActivationCode()
      ))
      .build();

    this.emailNotificationService.sendEmail(emailNotification);
  }

}
