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
  public void attachSchoolAndUserInviteToSaga(String schoolId, CreateSchoolSagaData createSchoolSagaData, SagaEntity saga)
  throws JsonProcessingException {
    List<School> result = this.restUtils.getSchoolById(saga.getSagaId(), schoolId);

    if (result.isEmpty()) {
      log.error("Could find School in Institute API :: {}", saga.getSagaId());
      throw new EntityNotFoundException("School entity not found");
    }

    School school = result.get(0);
    saga.setSchoolID(UUID.fromString(school.getSchoolId()));
    createSchoolSagaData.setSchool(school);
    EdxUser user = createSchoolSagaData.getInitialEdxUser();
    List<String> roles = List.of("EDX_SCHOOL_ADMIN");
    createSchoolSagaData.setSchoolID(UUID.fromString(school.getSchoolId()));
    createSchoolSagaData.setSchoolName(school.getDisplayName());
    createSchoolSagaData.setFirstName(user.getFirstName());
    createSchoolSagaData.setLastName(user.getLastName());
    createSchoolSagaData.setEmail(user.getEmail());
    createSchoolSagaData.setEdxActivationRoleCodes(roles);

    saga.setPayload(JsonUtil.getJsonStringFromObject(createSchoolSagaData));
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
    EdxUser user = sagaData.getInitialEdxUser();
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

}
