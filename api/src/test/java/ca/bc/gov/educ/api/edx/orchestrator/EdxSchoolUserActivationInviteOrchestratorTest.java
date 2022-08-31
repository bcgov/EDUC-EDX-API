package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
public class EdxSchoolUserActivationInviteOrchestratorTest extends BaseSecureExchangeAPITest {

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

  @Autowired
  EdxActivationCodeRepository edxActivationCodeRepository;

  private SagaEntity saga;
  EdxUserActivationInviteSagaData sagaData;

  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  String sagaPayload;
  @Autowired
  EdxSchoolUserActivationInviteOrchestrator orchestrator;

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;

  @Before
  public void setUp() throws JsonProcessingException {
    MockitoAnnotations.openMocks(this);
    sagaData = createUserActivationInviteData("Test", "User", "testuser@bcgov.ca");
    sagaPayload = getJsonString(sagaData);

    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA), sagaData);
      saga = this.sagaService.createSagaRecordInDB(sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }

  }

  /**
   * After.
   */
  @After
  public void after() {
    sagaEventStateRepository.deleteAll();
    sagaRepository.deleteAll();
    edxActivationCodeRepository.deleteAll();
    edxRoleRepository.deleteAll();
    edxPermissionRepository.deleteAll();
  }

  private EdxUserActivationInviteSagaData createUserActivationInviteData(String firstName, String lastName, String email) {

    EdxUserActivationInviteSagaData sagaData = new EdxUserActivationInviteSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName(firstName);
    sagaData.setLastName(lastName);
    sagaData.setEmail(email);
    sagaData.setSchoolName("Test School");
    sagaData.setSchoolID(UUID.randomUUID());
    List<String> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleCode());
    sagaData.setEdxActivationRoleCodes(rolesList);
    sagaData.setUpdateUser("Test");
    sagaData.setCreateUser("Test");
    return sagaData;
  }

  @Test
  public void testCreatePersonalActivationCodeEvent_GivenEventAndSagaData_ShouldCreateRecordInDBAndPostMessageToNats() throws IOException, InterruptedException, TimeoutException {
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
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_PERSONAL_ACTIVATION_CODE);
    assertThat(newEvent.getEventOutcome()).isEqualTo(PERSONAL_ACTIVATION_CODE_CREATED);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_PERSONAL_ACTIVATION_CODE.toString());
    var payload = JsonUtil.getJsonObjectFromString(EdxUserActivationInviteSagaData.class,newEvent.getEventPayload());
    assertThat(payload.getValidationCode()).isNotBlank();
    assertThat(payload.getPersonalActivationCode()).isNotBlank();
    List<EdxActivationCodeEntity> activationCodeEntities = edxActivationCodeRepository.findEdxActivationCodeEntitiesByValidationCode(UUID.fromString(payload.getValidationCode()));
    assertThat(activationCodeEntities).hasSize(1);
    final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
    assertThat(sagaStates).hasSize(1);
    assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
    assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());

  }


  @Test
  public void testSendEmailEvent_GivenEventAndSagaData_ShouldCreateEmail() throws IOException, InterruptedException, TimeoutException {
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
    assertThat(newEvent.getEventType()).isEqualTo(CREATE_PERSONAL_ACTIVATION_CODE);
    assertThat(newEvent.getEventOutcome()).isEqualTo(PERSONAL_ACTIVATION_CODE_CREATED);

    final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
    assertThat(sagaFromDB).isPresent();
    assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_PERSONAL_ACTIVATION_CODE.toString());

    final var nextEvent = Event.builder()
      .eventType(CREATE_PERSONAL_ACTIVATION_CODE)
      .eventOutcome(EventOutcome.PERSONAL_ACTIVATION_CODE_CREATED)
      .sagaId(this.saga.getSagaId())
      .eventPayload(newEvent.getEventPayload())
      .build();
    this.orchestrator.handleEvent(nextEvent);

    verify(this.messagePublisher, atMost(invocations + 3)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
    final var nextNewEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
    assertThat(nextNewEvent.getEventType()).isEqualTo(SEND_EDX_USER_ACTIVATION_EMAIL);
    assertThat(nextNewEvent.getEventOutcome()).isEqualTo(EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT);

  }

  @Test
  public void testMarkSagaCompleteEvent_GivenEventAndSagaData_ShouldMarkSagaCompleted() throws IOException, InterruptedException, TimeoutException {
    final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
    final var event = Event.builder()
      .eventType(SEND_EDX_USER_ACTIVATION_EMAIL)
      .eventOutcome(EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT)
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
