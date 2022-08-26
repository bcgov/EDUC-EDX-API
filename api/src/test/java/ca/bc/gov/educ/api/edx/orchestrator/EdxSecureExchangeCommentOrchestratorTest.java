package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.controller.BaseSagaControllerTest;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComment;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCommentSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
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
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.SECURE_EXCHANGE_COMMENT_SAGA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
public class EdxSecureExchangeCommentOrchestratorTest extends BaseSagaControllerTest {

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

  SecureExchangeCommentSagaData sagaData;

  @Autowired
  EdxSecureExchangeCommentOrchestrator orchestrator;

  @Autowired
  MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Autowired
  SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Autowired
  SecureExchangeRequestCommentRepository secureExchangeRequestCommentRepository;

  SecureExchangeEntity secureExchangeEntity;
  private static final SecureExchangeEntityMapper SECURE_EXCHANGE_ENTITY_MAPPER = SecureExchangeEntityMapper.mapper;

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;

  @After
  public void after() {
    sagaEventStateRepository.deleteAll();
    sagaRepository.deleteAll();
    edxRoleRepository.deleteAll();
    edxPermissionRepository.deleteAll();
    secureExchangeRequestCommentRepository.deleteAll();
    secureExchangeRequestRepository.deleteAll();
    ministryOwnershipTeamRepository.deleteAll();
  }

  @Before
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    sagaData = createCommentSagaData();
    sagaPayload = getJsonString(sagaData);
    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.SECURE_EXCHANGE_COMMENT_SAGA), sagaData);
      saga = this.sagaService.createSagaRecordInDB(sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }

  }

  private SecureExchangeCommentSagaData createCommentSagaData() throws JsonProcessingException {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    MinistryOwnershipTeamEntity ministryEntity = ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.secureExchangeEntity = this.secureExchangeRequestRepository.save(SECURE_EXCHANGE_ENTITY_MAPPER.toModel(this.getSecureExchangeEntityFromJsonString(ministryEntity.getMinistryOwnershipTeamId().toString())));
    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(secureExchangeEntity.getSecureExchangeID().toString()), SecureExchangeComment.class);
    return createSecureExchangeCommentSagaData(secureExchangeComment, UUID.randomUUID(), "WildFlower", "ABC Team", secureExchangeEntity.getSecureExchangeID(), "10");

  }

  @Test
  public void testCreateSecureExchangeCommentEvent_GivenEventAndSagaData_ShouldCreateRecordInDBAndPostMessageToNats() throws IOException, InterruptedException, TimeoutException {
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(this.saga.getSagaId())
      .eventPayload(sagaPayload)
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_SECURE_EXCHANGE_COMMENT);
    assertThat(newEvent.getEventOutcome()).isEqualTo(SECURE_EXCHANGE_COMMENT_CREATED);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_SECURE_EXCHANGE_COMMENT.toString());
    var payload = JsonUtil.getJsonObjectFromString(SecureExchangeCreateSagaData.class, newEvent.getEventPayload());
    assertThat(payload.getSchoolID()).isNotNull();
    assertThat(payload.getSchoolName()).isNotBlank();
    List<SecureExchangeEntity> secureExchangeEntities = secureExchangeRequestRepository.findSecureExchange(secureExchangeEntity.getContactIdentifier(), secureExchangeEntity.getSecureExchangeContactTypeCode());
    assertThat(secureExchangeEntities).hasSize(1);
    assertThat(secureExchangeRequestCommentRepository.findSecureExchangeCommentEntitiesBySecureExchangeEntitySecureExchangeID(secureExchangeEntities.get(0).getSecureExchangeID())).hasSize(2);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates).hasSize(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());

  }

  @Test
  public void testCreateSecureExchangeCommentEvent_GivenEventAndSagaDataWithRepeatScenarioAndCommentAlreadyExistInDB_ShouldSkipAddingTheSameCommentAndPostMessageToNats() throws IOException, InterruptedException, TimeoutException {
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(this.saga.getSagaId())
      .eventPayload(sagaPayload)
      .build();
    this.orchestrator.handleEvent(event);

    this.orchestrator.replaySaga(this.saga);

    verify(this.messagePublisher, atMost(invocations + 3)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_SECURE_EXCHANGE_COMMENT);
    assertThat(newEvent.getEventOutcome()).isEqualTo(SECURE_EXCHANGE_COMMENT_CREATED);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_SECURE_EXCHANGE_COMMENT.toString());
    var payload = JsonUtil.getJsonObjectFromString(SecureExchangeCreateSagaData.class, newEvent.getEventPayload());
    assertThat(payload.getSchoolID()).isNotNull();
    assertThat(payload.getSchoolName()).isNotBlank();
    List<SecureExchangeEntity> secureExchangeEntities = secureExchangeRequestRepository.findSecureExchange(secureExchangeEntity.getContactIdentifier(), secureExchangeEntity.getSecureExchangeContactTypeCode());
    assertThat(secureExchangeEntities).hasSize(1);
    assertThat(secureExchangeRequestCommentRepository.findSecureExchangeCommentEntitiesBySecureExchangeEntitySecureExchangeID(secureExchangeEntities.get(0).getSecureExchangeID())).hasSize(2);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates).hasSize(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
  }

  @Test
  public void testSendEmailEventForCreateSecureExchangeComment_GivenEventAndSagaData_ShouldCreateEmail() throws IOException, InterruptedException, TimeoutException {
    //to create the test data/
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(INITIATED)
      .eventOutcome(EventOutcome.INITIATE_SUCCESS)
      .sagaId(this.saga.getSagaId())
      .eventPayload(sagaPayload)
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 2)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_SECURE_EXCHANGE_COMMENT);
    assertThat(newEvent.getEventOutcome()).isEqualTo(SECURE_EXCHANGE_COMMENT_CREATED);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_SECURE_EXCHANGE_COMMENT.toString());

    final var nextEvent = Event.builder()
      .eventType(CREATE_SECURE_EXCHANGE_COMMENT)
      .eventOutcome(EventOutcome.SECURE_EXCHANGE_COMMENT_CREATED)
      .sagaId(this.saga.getSagaId())
      .eventPayload(newEvent.getEventPayload())
      .build();
    this.orchestrator.handleEvent(nextEvent);

    verify(this.messagePublisher, atMost(invocations + 3)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var nextNewEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(nextNewEvent.getEventType()).isEqualTo(SEND_EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT);
    assertThat(nextNewEvent.getEventOutcome()).isEqualTo(EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT_SENT);

  }

  @Test
  public void testMarkSagaCompleteEventForCreateSecureExchangeComment_GivenEventAndSagaData_ShouldMarkSagaCompleted() throws IOException, InterruptedException, TimeoutException {
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(SEND_EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT)
      .eventOutcome(EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT_SENT)
      .sagaId(this.saga.getSagaId())
      .eventPayload(sagaPayload)
      .build();
    this.orchestrator.handleEvent(event);

    verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(newEvent.getEventType()).isEqualTo(MARK_SAGA_COMPLETE);
    assertThat(newEvent.getEventOutcome()).isEqualTo(SAGA_COMPLETED);

  }


}
