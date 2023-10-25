package ca.bc.gov.educ.api.edx.service.v1;

import static ca.bc.gov.educ.api.edx.constants.InstituteTypeCode.SCHOOL;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.bc.gov.educ.api.edx.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.orchestrator.EdxSchoolUserActivationInviteOrchestrator;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.repository.EdxActivationCodeRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxPrimaryActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class CreateSchoolOrchestratorService {

  protected final SagaService sagaService;

  private final EdxActivationCodeRepository edxActivationCodeRepository;

  private final EdxUsersService service;

  private final EmailProperties emailProperties;
  private final EmailNotificationService emailNotificationService;
  private final RestUtils restUtils;

  public CreateSchoolOrchestratorService(
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
  public void attachInstituteSchoolToSaga(String schoolId, SagaEntity saga)
  throws JsonProcessingException {
    List<School> result = this.restUtils.getSchoolById(saga.getSagaId(), schoolId);

    if (result.isEmpty()) {
      log.error("Could find School in Institute API :: {}", saga.getSagaId());
      throw new EntityNotFoundException("School entity not found");
    }

    School school = result.get(0);
    saga.setSchoolID(UUID.fromString(school.getSchoolId()));
    CreateSchoolSagaData payload = JsonUtil.getJsonObjectFromString(CreateSchoolSagaData.class, saga.getPayload());
    payload.setSchool(school);
    saga.setPayload(JsonUtil.getJsonStringFromObject(payload));
    this.sagaService.updateAttachedEntityDuringSagaProcess(saga);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createPrimaryActivationCode(CreateSchoolSagaData sagaData) {
    EdxPrimaryActivationCode edxPrimaryActivationCode = new EdxPrimaryActivationCode();
    School school = sagaData.getSchool();
    edxPrimaryActivationCode.setSchoolID(UUID.fromString(school.getSchoolId()));
    RequestUtil.setAuditColumnsForCreate(edxPrimaryActivationCode);

    this.service.generateOrRegeneratePrimaryEdxActivationCode(SCHOOL, school.getSchoolId(), edxPrimaryActivationCode);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendPrimaryActivationCodeNotification(CreateSchoolSagaData sagaData) {
    EdxUser user = sagaData.getInitialEdxUser().orElseThrow();
    School school = sagaData.getSchool();
    UUID schoolId = UUID.fromString(school.getSchoolId());

    Optional<EdxActivationCodeEntity> edxActivationCodeEntity =
      edxActivationCodeRepository.findEdxActivationCodeEntitiesBySchoolIDAndIsPrimaryTrueAndDistrictIDIsNull(schoolId);

    EmailNotification emailNotification = EmailNotification.builder()
      .fromEmail(this.emailProperties.getEdxSchoolUserActivationInviteEmailFrom())
      .toEmail(user.getEmail())
      .subject(this.emailProperties.getEdxSecureExchangePrimaryCodeNotificationEmailSubject())
      .templateName("edx.school.primary-code.notification")
      .emailFields(Map.of(
        "firstName", user.getFirstName(),
        "lastName", user.getLastName(),
        "minCode", school.getMincode(),
        "instituteName", school.getDisplayName(),
        "primaryCode", edxActivationCodeEntity.orElseThrow().getActivationCode()
      ))
      .build();

    this.emailNotificationService.sendEmail(emailNotification);
  }

  public EdxActivationCodeEntity findPrimaryCode(String schoolId) {
    return this.service.findPrimaryEdxActivationCode(SCHOOL, schoolId);
  }

}
