package ca.bc.gov.educ.api.edx.orchestrator;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.CREATED_SCHOOL_HAS_ADMIN_USER;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SCHOOL_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_SCHOOL;
import static ca.bc.gov.educ.api.edx.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mockingDetails;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import ca.bc.gov.educ.api.edx.controller.BaseSagaControllerTest;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.repository.EdxPermissionRepository;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.repository.SagaEventStateRepository;
import ca.bc.gov.educ.api.edx.repository.SagaRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;

public class CreateSchoolOrchestratorTest extends BaseSagaControllerTest {

  /**
   * The Repository.
   */
  @Autowired
  SagaRepository sagaRepository;

  @Autowired
  private EdxUserRepository edxUserRepository;

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

  CreateSchoolSagaData sagaData;

  String sagaPayload;

  private SagaEntity saga;

  @Autowired
  private MessagePublisher messagePublisher;

  @Autowired
  RestUtils restUtils;

  @Autowired
  CreateSchoolOrchestrator orchestrator;

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
      tearDownSagas();
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
      assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_SCHOOL.toString());

      final List<SagaEventStatesEntity> sagaStates = sagaService.findAllSagaStates(saga);
      assertThat(sagaStates).hasSize(1);

      assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
      assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
    }

    @Test
    void testCreateSchool_GivenAnInitialUser_sagaShouldMoveOnToCreateUser() throws JsonProcessingException {
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

      assertThat(newEvent.getEventType()).isEqualTo(CREATE_SCHOOL);
      assertThat(newEvent.getEventPayload()).isEqualTo(sagaPayload);
      assertThat(newEvent.getEventOutcome()).isEqualTo(CREATED_SCHOOL_HAS_ADMIN_USER);
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

      assertThat(newEvent.getEventType()).isEqualTo(CREATE_SCHOOL);
      assertThat(newEvent.getEventPayload()).isEqualTo("");
      assertThat(newEvent.getEventOutcome()).isEqualTo(SCHOOL_CREATED);

      tearDownSagas();
    }
  }

  private CreateSchoolSagaData createMockCreateSchoolSagaData() {
    CreateSchoolSagaData sagaData = new CreateSchoolSagaData();
    sagaData.setSchoolNumber("12345");
    sagaData.setDisplayName("School Name");
    sagaData.setOpenedDate(LocalDateTime.now().minusDays(1).withNano(0).toString());
    sagaData.setSchoolCategoryCode("PUBLIC");
    sagaData.setSchoolOrganizationCode("TWO_SEM");
    sagaData.setSchoolReportingRequirementCode("REGULAR");
    sagaData.setFacilityTypeCode("DISTONLINE");
    sagaData.setWebsite("abc@sd99.edu");
    sagaData.setCreateDate(LocalDateTime.now().withNano(0).toString());
    sagaData.setUpdateDate(LocalDateTime.now().withNano(0).toString());
    sagaData.setCreateUser("TEST");
    sagaData.setUpdateUser("TEST");
    sagaData.setInitialEdxUser(Optional.empty());
    return sagaData;
  }

  private Optional<EdxUser> createMockInitialUser() {
    EdxUser mockUser = new EdxUser();
    mockUser.setEmail("test@test.xyz");
    mockUser.setFirstName("Test");
    mockUser.setLastName("Test");
    mockUser.setDigitalIdentityID(UUID.randomUUID().toString());
    return Optional.of(mockUser);
  }

  private void tearDownSagas() {
    sagaEventStateRepository.deleteAll();
    sagaRepository.deleteAll();
    edxUserSchoolRepository.deleteAll();
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
