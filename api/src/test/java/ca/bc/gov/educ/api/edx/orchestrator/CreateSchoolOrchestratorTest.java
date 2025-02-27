package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.controller.BaseSagaControllerTest;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class CreateSchoolOrchestratorTest extends BaseSagaControllerTest {

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
  CreateSchoolOrchestrator orchestrator;

  @Autowired
  EdxActivationCodeRepository edxActivationCodeRepository;

  private School mockSchool;
  private School mockInstituteSchool;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;

  @BeforeEach
  public void before() {
    this.mockSchool = createMockSchool();
    this.mockInstituteSchool = createMockSchoolFromInstitute(this.mockSchool);
    doReturn(List.of(this.mockInstituteSchool)).when(this.restUtils).getSchoolById(any(), any());
  }

  @AfterEach
  public void after() {
    tearDown();
  }

  @Test
  void testCreateSchool_GivenEventAndSagaData_shouldPostEventToInstituteApi() throws JsonProcessingException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockSchool);
    final SagaEntity saga = this.saveMockSaga(mockData);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(INITIATED.toString())
      .eventOutcome(INITIATE_SUCCESS.toString())
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();

    this.orchestrator.createSchool(event, saga, mockData);

    verify(this.messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(INSTITUTE_API_TOPIC.toString()), this.eventCaptor.capture());

    final Optional<SagaEntity> sagaFromDB = this.sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_SCHOOL.toString());

    final List<SagaEventStatesEntity> sagaStates = this.sagaService.findAllSagaStates(saga);
    assertThat(sagaStates).hasSize(1);

    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(INITIATE_SUCCESS.toString());
  }

  @Test
  void testCheckForIntitialUser_GivenAnInitialUser_sagaShouldOnboardUser()
  throws JsonProcessingException, IOException, TimeoutException, InterruptedException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    SagaEntity saga = this.saveMockSaga(mockData);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(CREATE_SCHOOL.toString())
      .eventOutcome(SCHOOL_CREATED.toString())
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(this.mockInstituteSchool))
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    final Event nextEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    CreateSchoolSagaData newData =
      JsonUtil.getJsonObjectFromString(CreateSchoolSagaData.class, nextEvent.getEventPayload());

    assertThat(newData.getInitialEdxUser()).isNotNull();
    assertThat(nextEvent.getEventType()).isEqualTo(ONBOARD_INITIAL_USER.toString());
    assertThat(nextEvent.getEventOutcome()).isEqualTo(INITIAL_USER_FOUND.toString());
  }

  @Test
  void testCheckForInitialUser_GivenNoInitialUser_sagaShouldBeCompleted() throws IOException, TimeoutException, InterruptedException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockSchool);
    SagaEntity saga = this.saveMockSaga(mockData);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(CREATE_SCHOOL.toString())
      .eventOutcome(SCHOOL_CREATED.toString())
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(this.mockInstituteSchool))
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    final Event onboardingEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    CreateSchoolSagaData onboardingSagaData =
      JsonUtil.getJsonObjectFromString(CreateSchoolSagaData.class, onboardingEvent.getEventPayload());

    assertThat(onboardingSagaData.getInitialEdxUser()).isNull();

    this.orchestrator.handleEvent(onboardingEvent);

    verify(this.messagePublisher, atMost(invocations + 2))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    final Event lastEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));

    assertThat(lastEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE.toString());
    assertThat(lastEvent.getEventOutcome()).isEqualTo(SAGA_COMPLETED.toString());
  }

  @Test
  void testCreatePrimaryCode_GivenAnInitialUserAndSchool_sagaShouldCreatePrimarySchoolCode() throws TimeoutException, IOException, InterruptedException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockInstituteSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    SagaEntity saga = this.saveMockSaga(mockData);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(ONBOARD_INITIAL_USER.toString())
      .eventOutcome(INITIAL_USER_FOUND.toString())
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    final Event newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    CreateSchoolSagaData newData =
      JsonUtil.getJsonObjectFromString(CreateSchoolSagaData.class, newEvent.getEventPayload());

    UUID schooId = UUID.fromString(newData.getSchool().getSchoolId());
    Optional<EdxActivationCodeEntity> codeOptional =
      edxActivationCodeRepository.findEdxActivationCodeEntitiesBySchoolIDAndIsPrimaryTrueAndDistrictIDIsNull(schooId);

    assertThat(codeOptional).isPresent();
    assertThat(newData.getInitialEdxUser()).isNotNull();
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_SCHOOL_PRIMARY_CODE.toString());
    assertThat(newEvent.getEventOutcome()).isEqualTo(SCHOOL_PRIMARY_CODE_CREATED.toString());
  }

  @Test
  void testSendPrimaryCode_GivenAnInitialUser_School_AndPrimaryCode_sagaShouldSendAPrimaryCodeToUser()
  throws TimeoutException, IOException, InterruptedException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockInstituteSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    SagaEntity saga = this.saveMockSaga(mockData);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(ONBOARD_INITIAL_USER.toString())
      .eventOutcome(INITIAL_USER_FOUND.toString())
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    final Event createCodeEvent = JsonUtil.getJsonObjectFromBytes(Event.class, this.eventCaptor.getValue());
    this.orchestrator.handleEvent(createCodeEvent);

    verify(this.messagePublisher, atMost(invocations + 2))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    final Event sendCodeEvent = JsonUtil.getJsonObjectFromBytes(Event.class, this.eventCaptor.getValue());

    assertThat(sendCodeEvent.getEventType()).isEqualTo(SEND_PRIMARY_ACTIVATION_CODE.toString());
    assertThat(sendCodeEvent.getEventOutcome()).isEqualTo(PRIMARY_ACTIVATION_CODE_SENT.toString());
  }

  @Test
  void testCreatePersonalCodeForUser_GivenEventAndSaga_sagaShouldGenerateCode() throws IOException, InterruptedException, TimeoutException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockInstituteSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    SagaEntity saga = this.saveMockSaga(mockData);
    createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(SEND_PRIMARY_ACTIVATION_CODE.toString())
      .eventOutcome(PRIMARY_ACTIVATION_CODE_SENT.toString())
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2))
      .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    Event currentEventState = JsonUtil.getJsonObjectFromBytes(Event.class, this.eventCaptor.getValue());
    assertThat(currentEventState.getEventType()).isEqualTo(CREATE_PERSONAL_ACTIVATION_CODE.toString());
    assertThat(currentEventState.getEventOutcome()).isEqualTo(PERSONAL_ACTIVATION_CODE_CREATED.toString());
  }

  @Test
  void testSendEdxUserActivationEmail_GivenEventAndSaga_sagaShouldSendEmail() throws IOException, InterruptedException, TimeoutException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockInstituteSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    EdxUser user = mockData.getInitialEdxUser();
    List<String> roles = List.of("EDX_SCHOOL_ADMIN");
    mockData.setSchoolID(UUID.fromString(mockData.getSchool().getSchoolId()));
    mockData.setSchoolName(mockData.getSchool().getDisplayName());
    mockData.setFirstName(user.getFirstName());
    mockData.setLastName(user.getLastName());
    mockData.setEmail(user.getEmail());
    mockData.setEdxActivationRoleCodes(roles);
    mockData.setEdxActivationCodeId(UUID.randomUUID().toString());
    mockData.setValidationCode("FEDCBA");
    mockData.setExpiryDate(LocalDateTime.now().plusDays(2));
    mockData.setPersonalActivationCode("ABCDEF");
    SagaEntity saga = this.saveMockSaga(mockData);
    createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);

    final int invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final Event event = Event.builder()
            .eventType(CREATE_PERSONAL_ACTIVATION_CODE.toString())
            .eventOutcome(PERSONAL_ACTIVATION_CODE_CREATED.toString())
            .sagaId(saga.getSagaId())
            .eventPayload(getJsonString(mockData))
            .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2))
            .dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());

    Event currentEventState = JsonUtil.getJsonObjectFromBytes(Event.class, this.eventCaptor.getValue());
    assertThat(currentEventState.getEventType()).isEqualTo(SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL.toString());
    assertThat(currentEventState.getEventOutcome()).isEqualTo(EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT.toString());
  }

  private CreateSchoolSagaData createMockCreateSchoolSagaData(School school) {
    CreateSchoolSagaData sagaData = new CreateSchoolSagaData();
    RequestUtil.setAuditColumnsForCreate(school);
    RequestUtil.setAuditColumnsForCreate(sagaData);
    sagaData.setSchool(school);
    sagaData.setInitialEdxUser(null);
    return sagaData;
  }

  private School createMockSchool() {
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

    return school;
  }

  private School createMockSchoolFromInstitute(School school) {
    School updatedSchool = this.createMockSchool();
    updatedSchool.setSchoolId(UUID.randomUUID().toString());
    updatedSchool.setDistrictId(school.getDistrictId());
    return updatedSchool;
  }

  private EdxUser createMockInitialUser() {
    EdxUser mockUser = new EdxUser();
    mockUser.setEmail("test@gov.bc.ca");
    mockUser.setFirstName("TestFirst");
    mockUser.setLastName("TestLast");
    mockUser.setDigitalIdentityID(UUID.randomUUID().toString());
    return mockUser;
  }

  private void tearDown() {
    this.sagaEventStateRepository.deleteAll();
    this.sagaRepository.deleteAll();
    this.edxUserSchoolRepository.deleteAll();
    this.edxActivationCodeRepository.deleteAll();
    this.edxRoleRepository.deleteAll();
    this.edxPermissionRepository.deleteAll();
  }

  private SagaEntity saveMockSaga(CreateSchoolSagaData mockSaga) {
    MockitoAnnotations.openMocks(this);
    try {
      SagaEntity sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.CREATE_NEW_SCHOOL_SAGA), mockSaga);
      return this.sagaService.createSagaRecordInDB(sagaEntity);
    } catch (Exception e) {
      throw new SagaRuntimeException(e);
    }
  }

}
