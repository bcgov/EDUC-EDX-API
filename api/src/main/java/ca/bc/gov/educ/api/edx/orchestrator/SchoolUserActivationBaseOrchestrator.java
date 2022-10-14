package ca.bc.gov.educ.api.edx.orchestrator;


import ca.bc.gov.educ.api.edx.mappers.v1.EdxActivationCodeMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.EdxSchoolUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.PERSONAL_ACTIVATION_CODE_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_PERSONAL_ACTIVATION_CODE;
import static ca.bc.gov.educ.api.edx.constants.EventType.SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type User Activation Base orchestrator.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class SchoolUserActivationBaseOrchestrator<T> extends BaseOrchestrator<T> {

  protected static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;

  @Getter(PRIVATE)
  private final EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                                      the saga service
   * @param messagePublisher                                 the message publisher
   * @param clazz                                            the clazz
   * @param sagaName                                         the saga name
   * @param topicToSubscribe                                 the topic to subscribe
   * @param edxSchoolUserActivationInviteOrchestratorService
   */
  protected SchoolUserActivationBaseOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, Class<T> clazz, String sagaName, String topicToSubscribe, EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService) {
    super(sagaService, messagePublisher, clazz, sagaName, topicToSubscribe);
    this.edxSchoolUserActivationInviteOrchestratorService = edxSchoolUserActivationInviteOrchestratorService;
  }

  /**
   * Create the personal activation code for the user
   *
   * @param event
   * @param saga
   * @param edxUserActivationInviteSagaData
   */
  protected void createPersonalActivationCode(Event event, SagaEntity saga, EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(CREATE_PERSONAL_ACTIVATION_CODE.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    if (edxUserActivationInviteSagaData.getEdxActivationCodeId() == null ) {//idempotency check
      getEdxSchoolUserActivationInviteOrchestratorService().createPersonalActivationCodeAndUpdateSagaData(edxUserActivationInviteSagaData, saga); // one transaction updates three tables.
    } else {
      EdxActivationCodeEntity edxActivationCodeEntity = getEdxSchoolUserActivationInviteOrchestratorService().getActivationCodeById(UUID.fromString(edxUserActivationInviteSagaData.getEdxActivationCodeId()));
      getEdxSchoolUserActivationInviteOrchestratorService().updateSagaData(edxUserActivationInviteSagaData, edxActivationCodeEntity, saga);
    }

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_PERSONAL_ACTIVATION_CODE).eventOutcome(PERSONAL_ACTIVATION_CODE_CREATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(edxUserActivationInviteSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for CREATE_PERSONAL_ACTIVATION_CODE Event.");
  }


  protected void sendEdxUserActivationEmail(Event event, SagaEntity saga, EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    log.debug("edxUserActivationInviteSagaData :: {}", edxUserActivationInviteSagaData);
    getEdxSchoolUserActivationInviteOrchestratorService().sendEmail(edxUserActivationInviteSagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL)
      .eventOutcome(EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(edxUserActivationInviteSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for SEND_EDX_USER_ACTIVATION_EMAIL Event.");
  }
}
