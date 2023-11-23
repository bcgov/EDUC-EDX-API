package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.repository.EdxActivationCodeRepository;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.InstituteTypeCode.SCHOOL;
import static ca.bc.gov.educ.api.edx.constants.InstituteTypeCode.DISTRICT;

@Service
public class OnboardUserOrchestratorService {

  protected final SagaService sagaService;

  private final EdxActivationCodeRepository edxActivationCodeRepository;

  private final EdxUsersService service;

  private final EmailProperties emailProperties;
  private final EmailNotificationService emailNotificationService;

  public OnboardUserOrchestratorService(
    SagaService sagaService,
    EdxActivationCodeRepository edxActivationCodeRepository,
    EdxUsersService service,
    EmailProperties emailProperties,
    EmailNotificationService emailNotificationService
  ) {
    this.sagaService = sagaService;
    this.edxActivationCodeRepository = edxActivationCodeRepository;
    this.service = service;
    this.emailProperties = emailProperties;
    this.emailNotificationService = emailNotificationService;
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createPrimaryActivationCode(OnboardSchoolUserSagaData sagaData) {
    EdxPrimaryActivationCode edxPrimaryActivationCode = new EdxPrimaryActivationCode();
    UUID schoolId = sagaData.getSchoolID();
    edxPrimaryActivationCode.setSchoolID(schoolId);
    RequestUtil.setAuditColumnsForCreate(edxPrimaryActivationCode);

    Optional<EdxActivationCodeEntity> edxActivationCodeEntity =
      edxActivationCodeRepository.findEdxActivationCodeEntitiesBySchoolIDAndIsPrimaryTrueAndDistrictIDIsNull(schoolId);

    if (edxActivationCodeEntity.isEmpty()) {
      this.service.generateOrRegeneratePrimaryEdxActivationCode(SCHOOL, schoolId.toString(), edxPrimaryActivationCode);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void createPrimaryActivationCode(OnboardDistrictUserSagaData sagaData) {
    EdxPrimaryActivationCode edxPrimaryActivationCode = new EdxPrimaryActivationCode();
    UUID districtId = sagaData.getDistrictID();
    edxPrimaryActivationCode.setDistrictID(districtId);
    RequestUtil.setAuditColumnsForCreate(edxPrimaryActivationCode);

    Optional<EdxActivationCodeEntity> edxActivationCodeEntity =
      edxActivationCodeRepository.findEdxActivationCodeEntitiesByDistrictIDAndIsPrimaryTrueAndSchoolIDIsNull(districtId);

    if (edxActivationCodeEntity.isEmpty()) {
      this.service.generateOrRegeneratePrimaryEdxActivationCode(DISTRICT, districtId.toString(), edxPrimaryActivationCode);
    }
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public void sendPrimaryActivationCodeNotification(OnboardSchoolUserSagaData sagaData) {
    UUID schoolId = sagaData.getSchoolID();

    Optional<EdxActivationCodeEntity> edxActivationCodeEntity =
      edxActivationCodeRepository.findEdxActivationCodeEntitiesBySchoolIDAndIsPrimaryTrueAndDistrictIDIsNull(schoolId);
    final String recipient = (sagaData.getFirstName() + " " + sagaData.getLastName()).trim();

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

  @Transactional(propagation = Propagation.REQUIRED)
  public void sendPrimaryActivationCodeNotification(OnboardDistrictUserSagaData sagaData) {
    UUID districtId = sagaData.getDistrictID();

    Optional<EdxActivationCodeEntity> edxActivationCodeEntity =
      edxActivationCodeRepository.findEdxActivationCodeEntitiesByDistrictIDAndIsPrimaryTrueAndSchoolIDIsNull(districtId);
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
        "instituteName", sagaData.getDistrictName(),
        "primaryCode", edxActivationCodeEntity.orElseThrow().getActivationCode()
      ))
      .build();

    this.emailNotificationService.sendEmail(emailNotification);
  }

}
