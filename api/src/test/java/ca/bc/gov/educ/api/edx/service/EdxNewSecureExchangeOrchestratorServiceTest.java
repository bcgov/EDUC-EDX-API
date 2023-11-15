package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import ca.bc.gov.educ.api.edx.constants.SecureExchangeContactTypeCode;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.repository.EdxPermissionRepository;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserDistrictRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRoleRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.EdxNewSecureExchangeOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EdxSecureExchangeCommentOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EmailNotificationService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreate;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;


@TestInstance(Lifecycle.PER_CLASS)
class EdxNewSecureExchangeOrchestratorServiceTest extends BaseEdxAPITest {

  @Autowired
  ApplicationProperties props;

  @Autowired
  EmailProperties emailProps;

  @Autowired
  EmailNotificationService emailNotificationService;

  @Autowired
  EdxNewSecureExchangeOrchestratorService newSecureExchangeOrchestratorService;

  @Autowired
  EdxSecureExchangeCommentOrchestratorService secureExchangeCommentOrchestratorService;

  @Autowired
  EdxUserSchoolRepository edxUserSchoolRepository;

  @Autowired
  EdxUserDistrictRepository edxUserDistrictRepository;

  @Autowired
  EdxRoleRepository edxRoleRepository;

  @Autowired
  EdxUserSchoolRoleRepository edxUserSchoolRoleRepository;

  @Autowired
  EdxPermissionRepository edxPermissionRepository;

  @Autowired
  EdxUserRepository edxUserRepository;

  @Autowired
  private RestUtils restUtils;

  @Captor
  private ArgumentCaptor<String> emailBodyCaptor;

  @BeforeAll
  public void beforeAll() {
    this.createEdxRoleForSchoolAndDistrict(this.edxRoleRepository, this.edxPermissionRepository);
  }

  @AfterAll
  public void afterAll() {
    this.edxRoleRepository.deleteAll();
    this.edxPermissionRepository.deleteAll();
  }

  @BeforeEach
  public void setUp() {
    openMocks(this);
    doNothing().when(this.restUtils).sendEmail(any(), any(), any(), any());
  }

  @AfterEach
  public void tearDown() {
    this.edxUserSchoolRepository.deleteAll();
    this.edxUserDistrictRepository.deleteAll();
  }

  @Test
  void sendEmail_givenNewSecureExchangeTemplate_and_School_Contact_shouldSendCorrectEmail() {
    EdxUserEntity edxUserEntity = this.createUserEntity(
      this.edxUserRepository,
      this.edxPermissionRepository,
      this.edxRoleRepository,
      this.edxUserSchoolRepository,
      this.edxUserDistrictRepository
    );

    EdxUserSchoolEntity schoolEntity = edxUserEntity.getEdxUserSchoolEntities().stream().findFirst().orElseThrow();
    SecureExchangeCreateSagaData sagaData = createNewExchangeSagaData(SecureExchangeContactTypeCode.SCHOOL);
    sagaData.setSchoolID(schoolEntity.getSchoolID());

    newSecureExchangeOrchestratorService.sendEmail(sagaData);

    verify(this.restUtils, atLeastOnce())
      .sendEmail(
        eq(emailProps.getEdxSchoolUserActivationInviteEmailFrom()),
        eq("test@email.com"),
        this.emailBodyCaptor.capture(),
        eq(emailProps.getEdxNewSecureExchangeNotificationEmailSubject())
      );

    final String emailBody = this.emailBodyCaptor.getValue();
    assertThat(emailBody).contains("Test Ministry");
    assertThat(emailBody).contains(sagaData.getSchoolName());
    assertThat(emailBody).contains(props.getEdxApplicationBaseUrl());
    assertThat(this.emailBodyCaptor.getValue()).doesNotContainPattern("\\{\\d\\}");
  }

  @Test
  void sendEmail_givenNewSecureExchangeTemplate_and_District_Contact_shouldSendCorrectEmail() {
    EdxUserEntity edxUserEntity = this.createUserEntity(
      this.edxUserRepository,
      this.edxPermissionRepository,
      this.edxRoleRepository,
      this.edxUserSchoolRepository,
      this.edxUserDistrictRepository
    );

    EdxUserDistrictEntity districtEntity = edxUserEntity.getEdxUserDistrictEntities().stream().findFirst().orElseThrow();
    SecureExchangeCreateSagaData sagaData = createNewExchangeSagaData(SecureExchangeContactTypeCode.DISTRICT);
    sagaData.setDistrictID(districtEntity.getDistrictID());

    newSecureExchangeOrchestratorService.sendEmail(sagaData);

    verify(this.restUtils, atLeastOnce())
      .sendEmail(
        eq(emailProps.getEdxSchoolUserActivationInviteEmailFrom()),
        eq("test@email.com"),
        this.emailBodyCaptor.capture(),
        eq(emailProps.getEdxNewSecureExchangeNotificationEmailSubject())
      );

    final String emailBody = this.emailBodyCaptor.getValue();
    assertThat(emailBody).contains("Test Ministry");
    assertThat(emailBody).contains(sagaData.getDistrictName());
    assertThat(emailBody).contains(props.getEdxApplicationBaseUrl());
    assertThat(this.emailBodyCaptor.getValue()).doesNotContainPattern("\\{\\d\\}");
  }

  SecureExchangeCreateSagaData createNewExchangeSagaData(SecureExchangeContactTypeCode contactType) {
    SecureExchangeCreateSagaData data = new SecureExchangeCreateSagaData();
    SecureExchangeCreate exchangeCreate = new SecureExchangeCreate();

    exchangeCreate.setSecureExchangeContactTypeCode(contactType.toString());
    data.setSchoolName("Test School");
    data.setDistrictName("Test District");
    data.setSecureExchangeCreate(exchangeCreate);
    data.setMinistryTeamName("Test Ministry");
    return data;
 }
}
