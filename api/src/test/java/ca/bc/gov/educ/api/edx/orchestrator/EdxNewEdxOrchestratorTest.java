package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.controller.BaseSagaControllerTest;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreate;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EdxNewEdxOrchestratorTest extends BaseSagaControllerTest {

  /**
   * The Repository.
   */
  @Autowired
  SagaRepository sagaRepository;
  /**
   * The Saga event repository.
   */
  @Autowired
  SagaEventStateRepository sagaEventStateRepository;

  @Autowired
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;
  /**
   * The Saga service.
   */
  @Autowired
  private SagaService sagaService;

  /**
   * The Message publisher.
   */
  @Autowired
  private MessagePublisher messagePublisher;

  private SagaEntity saga;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  String sagaPayload;

  SecureExchangeCreateSagaData sagaData;

  @Autowired
  EdxNewSecureExchangeOrchestrator orchestrator;

  @Autowired
  MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Autowired
  SecureExchangeRequestRepository secureExchangeRequestRepository;

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;


  @AfterEach
  public void after() {
    sagaEventStateRepository.deleteAll();
    sagaRepository.deleteAll();
    edxRoleRepository.deleteAll();
    edxPermissionRepository.deleteAll();
    ministryOwnershipTeamRepository.deleteAll();
  }

  @BeforeEach
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    sagaData = createNewSecureExchangeSagaData();
    sagaPayload = getJsonString(sagaData);
    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.NEW_SECURE_EXCHANGE_SAGA), sagaData);
      saga = this.sagaService.createSagaRecordInDB(sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }

  }

  private SecureExchangeCreateSagaData createNewSecureExchangeSagaData() throws JsonProcessingException {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    SecureExchangeCreateSagaData sagaData = new SecureExchangeCreateSagaData();
    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJson(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString(), "SCHOOL"), SecureExchangeCreate.class);
    sagaData.setSecureExchangeCreate(secureExchangeCreate);
    sagaData.setSchoolID(UUID.randomUUID());
    sagaData.setSchoolName("ABC School");
    sagaData.setMinistryTeamName("Min Team");
    sagaData.setCreateUser("Test");
    sagaData.setUpdateUser("Test");
    return sagaData;
  }

  @Test
  void testCreateNewSecureExchangeEvent_GivenEventAndSagaData_ShouldCreateRecordInDBAndPostMessageToNats() throws IOException, InterruptedException, TimeoutException {
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(INITIATED.toString())
      .eventOutcome(EventOutcome.INITIATE_SUCCESS.toString())
      .sagaId(this.saga.getSagaId())
      .eventPayload(sagaPayload)
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_NEW_SECURE_EXCHANGE.toString());
    assertThat(newEvent.getEventOutcome()).isEqualTo(NEW_SECURE_EXCHANGE_CREATED.toString());

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_NEW_SECURE_EXCHANGE.toString());
    var payload = JsonUtil.getJsonObjectFromString(SecureExchangeCreateSagaData.class, newEvent.getEventPayload());
    assertThat(payload.getSchoolID()).isNotNull();
    assertThat(payload.getSchoolName()).isNotBlank();
    List<SecureExchangeEntity> secureExchangeEntities = secureExchangeRequestRepository.findSecureExchange(payload.getSecureExchangeCreate().getContactIdentifier(), payload.getSecureExchangeCreate().getSecureExchangeContactTypeCode());
    assertThat(secureExchangeEntities).hasSize(1);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates).hasSize(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());

  }

  @Test
  void testCreateNewSecureExchangeEvent_GivenEventAndSagaDataWithRepeatScenarioAndSecureExchangeExistsInDB_ShouldSkipAddingTheSameSecureExchangeAndPostMessageToNats() throws IOException, InterruptedException, TimeoutException {
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(INITIATED.toString())
      .eventOutcome(EventOutcome.INITIATE_SUCCESS.toString())
      .sagaId(this.saga.getSagaId())
      .eventPayload(sagaPayload)
      .build();
    this.orchestrator.handleEvent(event);
    this.orchestrator.replaySaga(this.sagaRepository.findById(this.saga.getSagaId()).orElseThrow());
    verify(this.messagePublisher, atMost(invocations + 3)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_NEW_SECURE_EXCHANGE.toString());
    assertThat(newEvent.getEventOutcome()).isEqualTo(NEW_SECURE_EXCHANGE_CREATED.toString());

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_NEW_SECURE_EXCHANGE.toString());
    var payload = JsonUtil.getJsonObjectFromString(SecureExchangeCreateSagaData.class, newEvent.getEventPayload());
    assertThat(payload.getSchoolID()).isNotNull();
    assertThat(payload.getSchoolName()).isNotBlank();
    List<SecureExchangeEntity> secureExchangeEntities = secureExchangeRequestRepository.findSecureExchange(payload.getSecureExchangeCreate().getContactIdentifier(), payload.getSecureExchangeCreate().getSecureExchangeContactTypeCode());
    assertThat(secureExchangeEntities).hasSize(1);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates).hasSize(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());

  }

  @Test
  void testSendEmailEvent_GivenEventAndSagaData_ShouldCreateEmail() throws IOException, InterruptedException, TimeoutException {
    //to create the test data/
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(INITIATED.toString())
      .eventOutcome(EventOutcome.INITIATE_SUCCESS.toString())
      .sagaId(this.saga.getSagaId())
      .eventPayload(sagaPayload)
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_NEW_SECURE_EXCHANGE.toString());
    assertThat(newEvent.getEventOutcome()).isEqualTo(NEW_SECURE_EXCHANGE_CREATED.toString());

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_NEW_SECURE_EXCHANGE.toString());

    final var nextEvent = Event.builder()
      .eventType(CREATE_NEW_SECURE_EXCHANGE.toString())
      .eventOutcome(EventOutcome.NEW_SECURE_EXCHANGE_CREATED.toString())
      .sagaId(this.saga.getSagaId())
      .eventPayload(newEvent.getEventPayload())
      .build();
    this.orchestrator.handleEvent(nextEvent);

    verify(this.messagePublisher, atMost(invocations + 3)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var nextNewEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(nextNewEvent.getEventType()).isEqualTo(SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE.toString());
    assertThat(nextNewEvent.getEventOutcome()).isEqualTo(EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE_SENT.toString());

  }

  @Test
  void testMarkSagaCompleteEvent_GivenEventAndSagaData_ShouldMarkSagaCompleted() throws IOException, InterruptedException, TimeoutException {
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE.toString())
      .eventOutcome(EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE_SENT.toString())
      .sagaId(this.saga.getSagaId())
      .eventPayload(sagaPayload)
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE.toString());
    assertThat(newEvent.getEventOutcome()).isEqualTo(SAGA_COMPLETED.toString());

  }
}
