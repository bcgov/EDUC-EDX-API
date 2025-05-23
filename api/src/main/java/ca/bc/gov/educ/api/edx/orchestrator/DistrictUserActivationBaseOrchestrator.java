package ca.bc.gov.educ.api.edx.orchestrator;


import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxActivationCodeMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.EdxDistrictUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserDistrictActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.EDX_DISTRICT_USER_ACTIVATION_EMAIL_SENT;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.PERSONAL_ACTIVATION_CODE_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_PERSONAL_ACTIVATION_CODE;
import static ca.bc.gov.educ.api.edx.constants.EventType.SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type User Activation Base orchestrator.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class DistrictUserActivationBaseOrchestrator<T> extends BaseOrchestrator<T> {

  protected static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;

  @Getter(PRIVATE)
  private final EdxDistrictUserActivationInviteOrchestratorService edxDistrictUserActivationInviteOrchestratorService;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                                      the saga service
   * @param messagePublisher                                 the message publisher
   * @param clazz                                            the clazz
   * @param sagaName                                         the saga name
   * @param topicToSubscribe                                 the topic to subscribe
   * @param edxDistrictUserActivationInviteOrchestratorService
   */
  protected DistrictUserActivationBaseOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, Class<T> clazz, String sagaName, String topicToSubscribe, EdxDistrictUserActivationInviteOrchestratorService edxDistrictUserActivationInviteOrchestratorService) {
    super(sagaService, messagePublisher, clazz, sagaName, topicToSubscribe);
    this.edxDistrictUserActivationInviteOrchestratorService = edxDistrictUserActivationInviteOrchestratorService;
  }

  protected void createPersonalActivationCode(Event event, SagaEntity saga, EdxUserDistrictActivationInviteSagaData edxDistrictUserActivationInviteSagaData) throws JsonProcessingException {
    final EventType eventTypeValue = EventType.valueOf(event.getEventType());
    final EventOutcome eventOutcomeValue = EventOutcome.valueOf(event.getEventOutcome());
    final SagaEventStatesEntity eventStates = this.createEventState(saga, eventTypeValue, eventOutcomeValue, event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(CREATE_PERSONAL_ACTIVATION_CODE.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    if (edxDistrictUserActivationInviteSagaData.getEdxActivationCodeId() == null ) {//idempotency check
      getEdxDistrictUserActivationInviteOrchestratorService().createPersonalActivationCodeAndUpdateSagaData(edxDistrictUserActivationInviteSagaData, saga); // one transaction updates three tables.
    } else {
      EdxActivationCodeEntity edxActivationCodeEntity = getEdxDistrictUserActivationInviteOrchestratorService().getActivationCodeById(UUID.fromString(edxDistrictUserActivationInviteSagaData.getEdxActivationCodeId()));
      getEdxDistrictUserActivationInviteOrchestratorService().updateSagaData(edxDistrictUserActivationInviteSagaData, edxActivationCodeEntity, saga);
    }

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_PERSONAL_ACTIVATION_CODE.toString()).eventOutcome(PERSONAL_ACTIVATION_CODE_CREATED.toString())
      .eventPayload(JsonUtil.getJsonStringFromObject(edxDistrictUserActivationInviteSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for CREATE_PERSONAL_ACTIVATION_CODE Event.");
  }


  /**
   * Send edx user activation email.
   *
   * @param event                                   the event
   * @param saga                                    the saga
   * @param edxDistrictUserActivationInviteSagaData the edx district user activation invite saga data
   * @throws JsonProcessingException the json processing exception
   */
  protected void sendEdxUserActivationEmail(Event event, SagaEntity saga, EdxUserDistrictActivationInviteSagaData edxDistrictUserActivationInviteSagaData) throws JsonProcessingException {
    final EventType eventTypeValue = EventType.valueOf(event.getEventType());
    final EventOutcome eventOutcomeValue = EventOutcome.valueOf(event.getEventOutcome());
    final SagaEventStatesEntity eventStates = this.createEventState(saga, eventTypeValue, eventOutcomeValue, event.getEventPayload());
    saga.setSagaState(SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    log.debug("edxDistrictUserActivationInviteSagaData :: {}", edxDistrictUserActivationInviteSagaData);
    getEdxDistrictUserActivationInviteOrchestratorService().sendEmail(edxDistrictUserActivationInviteSagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL.toString())
      .eventOutcome(EDX_DISTRICT_USER_ACTIVATION_EMAIL_SENT.toString())
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(edxDistrictUserActivationInviteSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
  }
}
