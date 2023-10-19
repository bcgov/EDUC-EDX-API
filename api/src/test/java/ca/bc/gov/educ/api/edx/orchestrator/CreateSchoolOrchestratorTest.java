package ca.bc.gov.educ.api.edx.orchestrator;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.INITIAL_USER_FOUND;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.INITIAL_USER_INVITED;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.INITIATE_SUCCESS;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.PRIMARY_ACTIVATION_CODE_SENT;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SAGA_COMPLETED;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SCHOOL_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SCHOOL_PRIMARY_CODE_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_SCHOOL;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_SCHOOL_PRIMARY_CODE;
import static ca.bc.gov.educ.api.edx.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.INVITE_INITIAL_USER;
import static ca.bc.gov.educ.api.edx.constants.EventType.MARK_SAGA_COMPLETE;
import static ca.bc.gov.educ.api.edx.constants.EventType.ONBOARD_INITIAL_USER;
import static ca.bc.gov.educ.api.edx.constants.EventType.SEND_PRIMARY_ACTIVATION_CODE;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.edx.controller.BaseSagaControllerTest;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.repository.EdxActivationCodeRepository;
import ca.bc.gov.educ.api.edx.repository.EdxPermissionRepository;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.repository.SagaEventStateRepository;
import ca.bc.gov.educ.api.edx.repository.SagaRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;

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
    final SagaEntity saga = saveMockSaga(mockData);

    final int invocations = mockingDetails(messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(INITIATED)
      .eventOutcome(INITIATE_SUCCESS)
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();

    orchestrator.createSchool(event, saga, mockData);

    verify(messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(INSTITUTE_API_TOPIC.toString()), eventCaptor.capture());

    final Optional<SagaEntity> sagaFromDB = sagaService.findSagaById(saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_SCHOOL.toString());

    final List<SagaEventStatesEntity> sagaStates = sagaService.findAllSagaStates(saga);
    assertThat(sagaStates).hasSize(1);

    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(INITIATE_SUCCESS.toString());
  }

  @Test
  void testCheckForIntitialUser_GivenAnInitialUser_sagaShouldOnboardUser()
  throws JsonProcessingException, IOException, TimeoutException, InterruptedException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    SagaEntity saga = saveMockSaga(mockData);

    final int invocations = mockingDetails(messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(CREATE_SCHOOL)
      .eventOutcome(SCHOOL_CREATED)
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(this.mockInstituteSchool))
      .build();
    orchestrator.handleEvent(event);

    verify(messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

    final Event nextEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    CreateSchoolSagaData newData =
      JsonUtil.getJsonObjectFromString(CreateSchoolSagaData.class, nextEvent.getEventPayload());

    assertThat(newData.getInitialEdxUser()).isPresent();
    assertThat(nextEvent.getEventType()).isEqualTo(ONBOARD_INITIAL_USER);
    assertThat(nextEvent.getEventOutcome()).isEqualTo(INITIAL_USER_FOUND);
  }

  @Test
  void testCheckForIntitialUser_GivenNoIntialUser_sagaShouldBeCompleted()
  throws JsonProcessingException, IOException, TimeoutException, InterruptedException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockSchool);
    SagaEntity saga = saveMockSaga(mockData);

    final int invocations = mockingDetails(messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(CREATE_SCHOOL)
      .eventOutcome(SCHOOL_CREATED)
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(this.mockInstituteSchool))
      .build();
    orchestrator.handleEvent(event);

    verify(messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

    final Event onboardingEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    CreateSchoolSagaData onboardingSagaData =
      JsonUtil.getJsonObjectFromString(CreateSchoolSagaData.class, onboardingEvent.getEventPayload());

    assertThat(onboardingSagaData.getInitialEdxUser()).isNotPresent();

    orchestrator.handleEvent(onboardingEvent);

    verify(messagePublisher, atMost(invocations + 2))
      .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

    final Event lastEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));

    assertThat(lastEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE);
    assertThat(lastEvent.getEventOutcome()).isEqualTo(SAGA_COMPLETED);
  }

  @Test
  void testCreatePrimaryCode_GivenAnInitialUserAndSchool_sagaShouldCreatePrimarySchoolCode()
  throws TimeoutException, IOException, InterruptedException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockInstituteSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    SagaEntity saga = saveMockSaga(mockData);

    final int invocations = mockingDetails(messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(ONBOARD_INITIAL_USER)
      .eventOutcome(INITIAL_USER_FOUND)
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();
    orchestrator.handleEvent(event);

    verify(messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

    final Event newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));
    CreateSchoolSagaData newData =
      JsonUtil.getJsonObjectFromString(CreateSchoolSagaData.class, newEvent.getEventPayload());

    UUID schooId = UUID.fromString(newData.getSchool().getSchoolId());
    Optional<EdxActivationCodeEntity> codeOptional = edxActivationCodeRepository.findEdxActivationCodeEntitiesBySchoolIDAndIsPrimaryTrueAndDistrictIDIsNull(schooId);

    assertThat(codeOptional).isPresent();
    assertThat(newData.getInitialEdxUser()).isPresent();
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_SCHOOL_PRIMARY_CODE);
    assertThat(newEvent.getEventOutcome()).isEqualTo(SCHOOL_PRIMARY_CODE_CREATED);
  }

  @Test
  void testSendPrimaryCode_GivenAnInitialUser_School_AndPrimaryCode_sagaShouldSendAPrimaryCodeToUser()
  throws TimeoutException, IOException, InterruptedException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockInstituteSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    SagaEntity saga = saveMockSaga(mockData);

    final int invocations = mockingDetails(messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(ONBOARD_INITIAL_USER)
      .eventOutcome(INITIAL_USER_FOUND)
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();
    orchestrator.handleEvent(event);

    verify(messagePublisher, atMost(invocations + 1))
      .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

    final Event createCodeEvent = JsonUtil.getJsonObjectFromBytes(Event.class, eventCaptor.getValue());
    orchestrator.handleEvent(createCodeEvent);

    verify(messagePublisher, atMost(invocations + 2))
      .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

    final Event sendCodeEvent = JsonUtil.getJsonObjectFromBytes(Event.class, eventCaptor.getValue());

    assertThat(sendCodeEvent.getEventType()).isEqualTo(SEND_PRIMARY_ACTIVATION_CODE);
    assertThat(sendCodeEvent.getEventOutcome()).isEqualTo(PRIMARY_ACTIVATION_CODE_SENT);
  }

  @Test
  void testInviteInitialUser_GivenEventAndSaga_sagaShouldStartInviteSaga() throws IOException, InterruptedException, TimeoutException {
    final CreateSchoolSagaData mockData = createMockCreateSchoolSagaData(this.mockInstituteSchool);
    mockData.setInitialEdxUser(createMockInitialUser());
    SagaEntity saga = saveMockSaga(mockData);
    createRoleAndPermissionData(edxPermissionRepository, edxRoleRepository);

    final int invocations = mockingDetails(messagePublisher).getInvocations().size();
    final Event event = Event.builder()
      .eventType(SEND_PRIMARY_ACTIVATION_CODE)
      .eventOutcome(PRIMARY_ACTIVATION_CODE_SENT)
      .sagaId(saga.getSagaId())
      .eventPayload(getJsonString(mockData))
      .build();
    orchestrator.handleEvent(event);

    verify(messagePublisher, atMost(invocations + 2))
      .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

    Event currentEventState = JsonUtil.getJsonObjectFromBytes(Event.class, eventCaptor.getValue());
    assertThat(currentEventState.getEventType()).isEqualTo(INVITE_INITIAL_USER);
    assertThat(currentEventState.getEventOutcome()).isEqualTo(INITIAL_USER_INVITED);

    School school = mockData.getSchool();
    EdxUser user = mockData.getInitialEdxUser().orElseThrow();

    final Optional<SagaEntity> sagaInProgress = sagaService
      .findAllActiveUserActivationInviteSagasBySchoolIDAndEmailId(
        UUID.fromString(school.getSchoolId()),
        user.getEmail(),
        EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA.toString(),
        List.of(SagaStatusEnum.IN_PROGRESS.toString(), SagaStatusEnum.STARTED.toString()));

    assertThat(sagaInProgress).isPresent();
  }

  private CreateSchoolSagaData createMockCreateSchoolSagaData(School school) {
    CreateSchoolSagaData sagaData = new CreateSchoolSagaData();
    RequestUtil.setAuditColumnsForCreate(school);
    RequestUtil.setAuditColumnsForCreate(sagaData);
    sagaData.setSchool(school);
    sagaData.setInitialEdxUser(Optional.empty());
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

  private Optional<EdxUser> createMockInitialUser() {
    EdxUser mockUser = new EdxUser();
    mockUser.setEmail("test@gov.bc.ca");
    mockUser.setFirstName("TestFirst");
    mockUser.setLastName("TestLast");
    mockUser.setDigitalIdentityID(UUID.randomUUID().toString());
    return Optional.of(mockUser);
  }

  private void tearDown() {
    sagaEventStateRepository.deleteAll();
    sagaRepository.deleteAll();
    edxUserSchoolRepository.deleteAll();
    edxActivationCodeRepository.deleteAll();
    edxRoleRepository.deleteAll();
    edxPermissionRepository.deleteAll();
  }

  private SagaEntity saveMockSaga(CreateSchoolSagaData mockSaga) {
    MockitoAnnotations.openMocks(this);
    try {
      SagaEntity sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.CREATE_NEW_SCHOOL_SAGA), mockSaga);
      return sagaService.createSagaRecordInDB(sagaEntity);
    } catch (Exception e) {
      throw new SagaRuntimeException(e);
    }
  }

}
