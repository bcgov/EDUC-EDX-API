package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.controller.BaseSagaControllerTest;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.OnboardSchoolUserSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OnboardDistrictUserOrchestratorTest extends BaseSagaControllerTest {

  /**
   * The Repository.
   */
  @Autowired
  SagaRepository sagaRepository;

  @Autowired
  private EdxUserSchoolRepository edxUserSchoolRepository;

  @Autowired
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;
  /**
   * The Saga event repository.
   */
  @Autowired
  SagaEventStateRepository sagaEventStateRepository;

  @Autowired
  private SagaService sagaService;

  @Autowired
  private MessagePublisher messagePublisher;

  @Autowired
  RestUtils restUtils;

  @Autowired
  OnboardSchoolUserOrchestrator orchestrator;

  @Autowired
  EdxActivationCodeRepository edxActivationCodeRepository;

  private School mockInstituteSchool;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;

  @BeforeEach
  public void before() {
    this.mockInstituteSchool = this.createMockSchoolFromInstitute();
  }

  @AfterEach
  public void after() {
    tearDown();
  }

  @Test
  void testSendPrimaryCode_GivenEventAndSaga_sagaShouldSendAPrimaryCodeToUser()
  throws TimeoutException, IOException, InterruptedException {
    final OnboardSchoolUserSagaData mockData = createMockOnboardUserSagaData(this.mockInstituteSchool);
    SagaEntity saga = this.saveMockSaga(mockData);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(INITIATED)
      .eventOutcome(INITIATE_SUCCESS)
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    final Event createCodeEvent = JsonUtil.getJsonObjectFromBytes(Event.class, this.eventCaptor.getValue());
    this.orchestrator.handleEvent(createCodeEvent);

    verify(this.messagePublisher, atMost(invocations + 3))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    final Event sendCodeEvent = JsonUtil.getJsonObjectFromBytes(Event.class, this.eventCaptor.getValue());

    assertThat(sendCodeEvent.getEventType()).isEqualTo(SEND_PRIMARY_ACTIVATION_CODE);
    assertThat(sendCodeEvent.getEventOutcome()).isEqualTo(PRIMARY_ACTIVATION_CODE_SENT);
  }

  @Test
  void testCreatePersonalCodeForUser_GivenEventAndSaga_sagaShouldGenerateCode() throws IOException, InterruptedException, TimeoutException {
    final OnboardSchoolUserSagaData mockData = createMockOnboardUserSagaData(this.mockInstituteSchool);
    SagaEntity saga = this.saveMockSaga(mockData);
    createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(SEND_PRIMARY_ACTIVATION_CODE)
      .eventOutcome(PRIMARY_ACTIVATION_CODE_SENT)
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    Event currentEventState = JsonUtil.getJsonObjectFromBytes(Event.class, this.eventCaptor.getValue());
    assertThat(currentEventState.getEventType()).isEqualTo(CREATE_PERSONAL_ACTIVATION_CODE);
    assertThat(currentEventState.getEventOutcome()).isEqualTo(PERSONAL_ACTIVATION_CODE_CREATED);
  }

  @Test
  void testSendEdxUserActivationEmail_GivenEventAndSaga_sagaShouldSendEmail() throws IOException, InterruptedException, TimeoutException {
    final OnboardSchoolUserSagaData mockData = createMockOnboardUserSagaData(this.mockInstituteSchool);
    List<String> roles = List.of("EDX_DISTRICT_ADMIN");
    mockData.setSchoolID(UUID.fromString(this.mockInstituteSchool.getSchoolId()));
    mockData.setSchoolName(this.mockInstituteSchool.getDisplayName());
    mockData.setEdxActivationRoleCodes(roles);
    mockData.setEdxActivationCodeId(UUID.randomUUID().toString());
    mockData.setValidationCode("FEDCBA");
    mockData.setExpiryDate(LocalDateTime.now().plusDays(2));
    mockData.setPersonalActivationCode("ABCDEF");
    SagaEntity saga = this.saveMockSaga(mockData);
    createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
            .eventType(CREATE_PERSONAL_ACTIVATION_CODE)
            .eventOutcome(PERSONAL_ACTIVATION_CODE_CREATED)
            .sagaId(saga.getSagaId())
            .eventPayload(getJsonString(mockData))
            .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2))
            .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    Event currentEventState = JsonUtil.getJsonObjectFromBytes(Event.class, this.eventCaptor.getValue());
    assertThat(currentEventState.getEventType()).isEqualTo(SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL);
    assertThat(currentEventState.getEventOutcome()).isEqualTo(EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT);
  }

  private OnboardSchoolUserSagaData createMockOnboardUserSagaData(School school) {
    OnboardSchoolUserSagaData sagaData = new OnboardSchoolUserSagaData();
    RequestUtil.setAuditColumnsForCreate(sagaData);
    sagaData.setSchoolID(UUID.fromString(school.getSchoolId()));
    sagaData.setMincode(school.getMincode());
    sagaData.setFirstName("First Name");
    sagaData.setLastName("Last Name");
    sagaData.setEmail("test@test.com");
    sagaData.setSchoolName(school.getDisplayName());
    return sagaData;
  }

  private School createMockSchoolFromInstitute() {
    School school = new School();
    school.setSchoolNumber("12345");
    school.setMincode("12312345");
    school.setDisplayName("School Name");
    school.setOpenedDate(LocalDateTime.now().minusDays(1).withNano(0).toString());
    school.setSchoolCategoryCode("PUBLIC");
    school.setSchoolOrganizationCode("TWO_SEM");
    school.setSchoolReportingRequirementCode("REGULAR");
    school.setFacilityTypeCode("DISTONLINE");
    school.setWebsite("abc@sd99.edu");
    school.setCreateUser("TEST");
    school.setUpdateUser("TEST");
    school.setDistrictId(UUID.randomUUID().toString());
    school.setSchoolId(UUID.randomUUID().toString());
    return school;
  }

  private void tearDown() {
    this.sagaEventStateRepository.deleteAll();
    this.sagaRepository.deleteAll();
    this.edxUserSchoolRepository.deleteAll();
    this.edxActivationCodeRepository.deleteAll();
    this.edxRoleRepository.deleteAll();
    this.edxPermissionRepository.deleteAll();
  }

  private SagaEntity saveMockSaga(OnboardSchoolUserSagaData mockSaga) {
    MockitoAnnotations.openMocks(this);
    try {
      SagaEntity sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.ONBOARD_SCHOOL_USER_SAGA), mockSaga);
      return this.sagaService.createSagaRecordInDB(sagaEntity);
    } catch (Exception e) {
      throw new SagaRuntimeException(e);
    }
  }

}
