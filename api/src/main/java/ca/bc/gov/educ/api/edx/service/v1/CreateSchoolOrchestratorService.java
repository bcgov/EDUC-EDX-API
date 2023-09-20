package ca.bc.gov.educ.api.edx.service.v1;

import static ca.bc.gov.educ.api.edx.constants.InstituteTypeCode.SCHOOL;

import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.nimbusds.jose.Payload;

import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxPrimaryActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import ca.bc.gov.educ.api.edx.props.EmailProperties;

@Service
@Slf4j
public class CreateSchoolOrchestratorService {

  protected final SagaService sagaService;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService service;

  private final EdxUserMapper userMapper = EdxUserMapper.mapper;

  private final EmailProperties emailProperties;
  private final EmailNotificationService emailNotificationService;

  private final EdxUserSchoolRepository edxUserSchoolsRepository;


  public CreateSchoolOrchestratorService(
    SagaService sagaService,
    EdxUsersService service,
    EdxUserSchoolRepository edxUserSchoolRepository,
    EmailProperties emailProperties,
    EmailNotificationService emailNotificationService
  ) {
    this.sagaService = sagaService;
    this.service = service;
    this.edxUserSchoolsRepository = edxUserSchoolRepository;
    this.emailProperties = emailProperties;
    this.emailNotificationService = emailNotificationService;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createPrimaryActivationCode(CreateSchoolSagaData sagaData) {
    EdxPrimaryActivationCode edxPrimaryActivationCode = new EdxPrimaryActivationCode();
    School school = sagaData.getSchool();
    edxPrimaryActivationCode.setSchoolID(UUID.fromString(school.getSchoolId()));
    edxPrimaryActivationCode.setDistrictID(UUID.fromString(school.getDistrictId()));
    RequestUtil.setAuditColumnsForCreate(edxPrimaryActivationCode);

    service.generateOrRegeneratePrimaryEdxActivationCode(SCHOOL, school.getSchoolId(), edxPrimaryActivationCode);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendPrimaryActivationCodeNotification(CreateSchoolSagaData sagaData) {
    EdxUser user = sagaData.getInitialEdxUser().get();
    School school = sagaData.getSchool();

    EdxActivationCodeEntity edxActivationCodeEntity
      = service.findPrimaryEdxActivationCode(SCHOOL, school.getSchoolId());

    EmailNotification emailNotification = EmailNotification.builder()
      .fromEmail(emailProperties.getEdxSchoolUserActivationInviteEmailFrom())
      .toEmail(user.getEmail())
      .subject(emailProperties.getEdxSecureExchangePrimaryCodeNotificationEmailSubject())
      .templateName("edx.school.primary-code.notification")
      .emailFields(Map.of(
        "firstName", user.getFirstName(),
        "lastName", user.getLastName(),
        "minCode", school.getMincode(),
        "instituteName", school.getDisplayName(),
        "primaryCode", edxActivationCodeEntity.getActivationCode()
      ))
      .build();

    emailNotificationService.sendEmail(emailNotification);
  }

  public EdxActivationCodeEntity findPrimaryCode(String schoolId) {
    return service.findPrimaryEdxActivationCode(SCHOOL, schoolId);
  }

}
