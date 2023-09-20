package ca.bc.gov.educ.api.edx.orchestrator;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.CREATED_SCHOOL_HAS_ADMIN_USER;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.INITIAL_USER_INVITED;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.PRIMARY_ACTIVATION_CODE_SENT;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SCHOOL_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SCHOOL_PRIMARY_CODE_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_SCHOOL_PRIMARY_CODE;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_SCHOOL_WITH_ADMIN;
import static ca.bc.gov.educ.api.edx.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.INVITE_INITIAL_USER;
import static ca.bc.gov.educ.api.edx.constants.EventType.SEND_PRIMARY_ACTIVATION_CODE;
import static ca.bc.gov.educ.api.edx.constants.InstituteTypeCode.SCHOOL;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.edx.controller.BaseSagaControllerTest;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.repository.EdxActivationCodeRepository;
import ca.bc.gov.educ.api.edx.repository.EdxPermissionRepository;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.repository.SagaEventStateRepository;
import ca.bc.gov.educ.api.edx.repository.SagaRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;

public class CreateSchoolOrchestratorTest extends BaseSagaControllerTest {

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
  private EdxUsersService usersService;

  CreateSchoolSagaData sagaData;

  String sagaPayload;

  private SagaEntity saga;

  @Autowired
  private MessagePublisher messagePublisher;

  @Autowired
  RestUtils restUtils;

  @Autowired
  CreateSchoolOrchestrator orchestrator;

  @Autowired
  EdxActivationCodeRepository edxActivationCodeRepository;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;

  @Nested
  class InitializedTests {
    @BeforeEach
    public void setUp() throws JsonProcessingException {
      CreateSchoolSagaData mockData = createMockCreateSchoolSagaData();
      mockData.setInitialEdxUser(createMockInitialUser());
      setUpSagas(mockData);
    }

    @AfterEach
    public void after() {
      tearDown();
    }

    @Test
    void testCreateSchool_GivenEventAndSagaData_shouldPostEventToInstituteApi() throws JsonProcessingException {
      final int invocations = mockingDetails(messagePublisher).getInvocations().size();
      final Event event = Event.builder()
        .eventType(INITIATED)
        .eventOutcome(EventOutcome.INITIATE_SUCCESS)
        .sagaId(saga.getSagaId())
        .eventPayload(sagaPayload)
        .build();

      orchestrator.createSchool(event, saga, sagaData);

      verify(messagePublisher, atMost(invocations + 1))
        .dispatchMessage(eq(INSTITUTE_API_TOPIC.toString()), eventCaptor.capture());

      final Optional<SagaEntity> sagaFromDB = sagaService.findSagaById(saga.getSagaId());
      assertThat(sagaFromDB).isPresent();
      assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_SCHOOL_WITH_ADMIN.toString());

      final List<SagaEventStatesEntity> sagaStates = sagaService.findAllSagaStates(saga);
      assertThat(sagaStates).hasSize(1);

      assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
      assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
    }

    @Test
    void testCreateSchool_GivenAnInitialUser_sagaShouldOnboardInitialUser()
    throws JsonProcessingException, TimeoutException, IOException, InterruptedException {
      final int invocations = mockingDetails(messagePublisher).getInvocations().size();
      final Event event = Event.builder()
        .eventType(INITIATED)
        .eventOutcome(EventOutcome.INITIATE_SUCCESS)
        .sagaId(saga.getSagaId())
        .eventPayload(sagaPayload)
        .build();
      orchestrator.handleEvent(event);

      verify(messagePublisher, atMost(invocations + 1))
        .dispatchMessage(eq(INSTITUTE_API_TOPIC.toString()), eventCaptor.capture());

      final Event newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));

      assertThat(newEvent.getEventType()).isEqualTo(CREATE_SCHOOL_WITH_ADMIN);
      assertThat(newEvent.getEventPayload()).isEqualTo(sagaPayload);
      assertThat(newEvent.getEventOutcome()).isEqualTo(CREATED_SCHOOL_HAS_ADMIN_USER);
    }

    @Test
    void testCreateSchool_GivenAnInitialUser_sagaShouldGenerateSchoolPrimaryCode()
    throws JsonProcessingException, TimeoutException, IOException, InterruptedException {
      final int invocations = mockingDetails(messagePublisher).getInvocations().size();
      final Event event = Event.builder()
        .eventType(CREATE_SCHOOL_WITH_ADMIN)
        .eventOutcome(CREATED_SCHOOL_HAS_ADMIN_USER)
        .sagaId(saga.getSagaId())
        .eventPayload(sagaPayload)
        .build();
      orchestrator.handleEvent(event);

      verify(messagePublisher, atMost(invocations + 2))
        .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

      final Event newEvent = JsonUtil.getJsonObjectFromBytes(Event.class, eventCaptor.getValue());

      String schoolId = sagaData.getSchool().getSchoolId();
      assertDoesNotThrow(() -> {
        usersService.findPrimaryEdxActivationCode(SCHOOL, schoolId);
      });

      assertThat(newEvent.getEventType()).isEqualTo(CREATE_SCHOOL_PRIMARY_CODE);
      assertThat(newEvent.getEventOutcome()).isEqualTo(SCHOOL_PRIMARY_CODE_CREATED);
    }

    @Test
    void testSendEmailEvent_GivenEventAndSaga_ShouldSendPrimaryCode()
    throws IOException, InterruptedException, TimeoutException {
      final int invocations = mockingDetails(messagePublisher).getInvocations().size();
      final Event event = Event.builder()
        .eventType(CREATE_SCHOOL_WITH_ADMIN)
        .eventOutcome(CREATED_SCHOOL_HAS_ADMIN_USER)
        .sagaId(saga.getSagaId())
        .eventPayload(sagaPayload)
        .build();
      orchestrator.handleEvent(event);

      verify(messagePublisher, atMost(invocations + 2))
        .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

      Optional<SagaEntity> sagaFromDB = sagaService.findSagaById(saga.getSagaId());
      assertThat(sagaFromDB).isPresent();
      assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_SCHOOL_PRIMARY_CODE.toString());

      Event currentEventState = JsonUtil.getJsonObjectFromBytes(Event.class, eventCaptor.getValue());
      assertThat(currentEventState.getEventType()).isEqualTo(CREATE_SCHOOL_PRIMARY_CODE);
      assertThat(currentEventState.getEventOutcome()).isEqualTo(SCHOOL_PRIMARY_CODE_CREATED);

      Event sendMailEvent = Event.builder()
        .eventType(CREATE_SCHOOL_PRIMARY_CODE)
        .eventOutcome(SCHOOL_PRIMARY_CODE_CREATED)
        .sagaId(saga.getSagaId())
        .eventPayload(currentEventState.getEventPayload())
        .build();
      orchestrator.handleEvent(sendMailEvent);

      verify(messagePublisher, atMost(invocations + 2))
        .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

      Event mailSentEvent = JsonUtil.getJsonObjectFromBytes(Event.class, eventCaptor.getValue());

      assertThat(mailSentEvent.getEventType()).isEqualTo(SEND_PRIMARY_ACTIVATION_CODE);
      assertThat(mailSentEvent.getEventOutcome()).isEqualTo(PRIMARY_ACTIVATION_CODE_SENT);
      System.out.println(mailSentEvent);
    }

    @Test
    void testInviteInitialUser_GivenEventAndSaga_ShouldStartInviteSaga()
    throws IOException, InterruptedException, TimeoutException {
      createRoleAndPermissionData(edxPermissionRepository, edxRoleRepository);
      final int invocations = mockingDetails(messagePublisher).getInvocations().size();
      final Event event = Event.builder()
        .eventType(SEND_PRIMARY_ACTIVATION_CODE)
        .eventOutcome(PRIMARY_ACTIVATION_CODE_SENT)
        .sagaId(saga.getSagaId())
        .eventPayload(sagaPayload)
        .build();
      orchestrator.handleEvent(event);

      verify(messagePublisher, atMost(invocations + 2))
        .dispatchMessage(eq(orchestrator.getTopicToSubscribe()), eventCaptor.capture());

      Event currentEventState = JsonUtil.getJsonObjectFromBytes(Event.class, eventCaptor.getValue());
      assertThat(currentEventState.getEventType()).isEqualTo(INVITE_INITIAL_USER);
      assertThat(currentEventState.getEventOutcome()).isEqualTo(INITIAL_USER_INVITED);

      School school = sagaData.getSchool();
      EdxUser user = sagaData.getInitialEdxUser().get();

      final Optional<SagaEntity> sagaInProgress = sagaService
        .findAllActiveUserActivationInviteSagasBySchoolIDAndEmailId(
          UUID.fromString(school.getSchoolId()),
          user.getEmail(),
          EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA.toString(),
          List.of(SagaStatusEnum.IN_PROGRESS.toString(), SagaStatusEnum.STARTED.toString()));

      assertThat(sagaInProgress).isPresent();
    }
  }

  @Nested
  class StandaloneTests {
    @Test
    void testCreateSchool_GivenNoInitialUser_sagaShouldEndEarly() throws JsonProcessingException {
        setUpSagas(createMockCreateSchoolSagaData());

      final int invocations = mockingDetails(messagePublisher).getInvocations().size();
      final Event event = Event.builder()
        .eventType(INITIATED)
        .eventOutcome(EventOutcome.INITIATE_SUCCESS)
        .sagaId(saga.getSagaId())
        .eventPayload(sagaPayload)
        .build();

      orchestrator.createSchool(event, saga, sagaData);

      verify(messagePublisher, atMost(invocations + 1))
        .dispatchMessage(eq(INSTITUTE_API_TOPIC.toString()), eventCaptor.capture());

      final Event newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(eventCaptor.getValue()));

      assertThat(newEvent.getEventType()).isEqualTo(CREATE_SCHOOL_WITH_ADMIN);
      assertThat(newEvent.getEventPayload()).isEqualTo("");
      assertThat(newEvent.getEventOutcome()).isEqualTo(SCHOOL_CREATED);

      tearDown();
    }
  }

  private CreateSchoolSagaData createMockCreateSchoolSagaData() {
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
    school.setSchoolId(UUID.randomUUID().toString());
    school.setDistrictId(UUID.randomUUID().toString());

    CreateSchoolSagaData sagaData = new CreateSchoolSagaData();
    RequestUtil.setAuditColumnsForCreate(school);
    RequestUtil.setAuditColumnsForCreate(sagaData);
    sagaData.setSchool(school);
    sagaData.setInitialEdxUser(Optional.empty());
    return sagaData;
  }

  private Optional<EdxUser> createMockInitialUser() {
    EdxUser mockUser = new EdxUser();
    mockUser.setEmail("trevor.richards@gov.bc.ca");
    mockUser.setFirstName("Trevor");
    mockUser.setLastName("Richards");
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

  private void setUpSagas(CreateSchoolSagaData mockSaga) {
    MockitoAnnotations.openMocks(this);
    try {
      sagaData = mockSaga;
      sagaPayload = getJsonString(sagaData);
      SagaEntity sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.CREATE_NEW_SCHOOL_SAGA), sagaData);
      this.saga = sagaService.createSagaRecordInDB(sagaEntity);
    } catch (Exception e) {
      throw new SagaRuntimeException(e);
    }
  }

}
