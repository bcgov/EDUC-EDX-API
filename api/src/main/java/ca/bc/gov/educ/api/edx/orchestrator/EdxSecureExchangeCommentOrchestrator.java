package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.EdxSecureExchangeCommentOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCommentSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.SECURE_EXCHANGE_COMMENT_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_SECURE_EXCHANGE_COMMENT_TOPIC;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Edx secure exchange comment orchestrator.
 */
@Component
@Slf4j
public class EdxSecureExchangeCommentOrchestrator extends BaseOrchestrator<SecureExchangeCommentSagaData> {

  /**
   * The Edx secure exchange comment orchestrator service.
   */
  @Getter(PRIVATE)
  private final EdxSecureExchangeCommentOrchestratorService edxSecureExchangeCommentOrchestratorService;

  /**
   * Instantiates a new Edx secure exchange comment orchestrator.
   *
   * @param sagaService                                 the saga service
   * @param messagePublisher                            the message publisher
   * @param edxSecureExchangeCommentOrchestratorService the edx secure exchange comment orchestrator service
   */
  public EdxSecureExchangeCommentOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, EdxSecureExchangeCommentOrchestratorService edxSecureExchangeCommentOrchestratorService) {
    super(sagaService, messagePublisher, SecureExchangeCommentSagaData.class, SECURE_EXCHANGE_COMMENT_SAGA.toString(), EDX_SECURE_EXCHANGE_COMMENT_TOPIC.toString());
    this.edxSecureExchangeCommentOrchestratorService = edxSecureExchangeCommentOrchestratorService;
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_SECURE_EXCHANGE_COMMENT, this::createSecureExchangeComment)
      .step(CREATE_SECURE_EXCHANGE_COMMENT, SECURE_EXCHANGE_COMMENT_CREATED, SEND_EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT, this::sendEmailForSecureExchangeComment)
      .end(SEND_EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT, EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT_SENT);

  }

  /**
   * Send email for secure exchange comment.
   *
   * @param event                         the event
   * @param saga                          the saga
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @throws JsonProcessingException the json processing exception
   */
  private void sendEmailForSecureExchangeComment(Event event, SagaEntity saga, SecureExchangeCommentSagaData secureExchangeCommentSagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(SEND_EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    log.debug("secureExchangeCommentSagaData :: {}", secureExchangeCommentSagaData);
    getEdxSecureExchangeCommentOrchestratorService().sendEmail(secureExchangeCommentSagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(SEND_EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT)
      .eventOutcome(EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT_SENT)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(secureExchangeCommentSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE Event.");

  }

  /**
   * Create secure exchange comment.
   *
   * @param event                         the event
   * @param saga                          the saga
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @throws JsonProcessingException the json processing exception
   */
  private void createSecureExchangeComment(Event event, SagaEntity saga, SecureExchangeCommentSagaData secureExchangeCommentSagaData) throws JsonProcessingException {

    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(CREATE_SECURE_EXCHANGE_COMMENT.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    val secureExchangeCommentEntityOptional = getEdxSecureExchangeCommentOrchestratorService().findCommentForSecureExchange(secureExchangeCommentSagaData);
    if (secureExchangeCommentEntityOptional.isPresent()) { // handle repeat scenario and deal with skipping the creation.
      getEdxSecureExchangeCommentOrchestratorService().updateSagaData(secureExchangeCommentEntityOptional.get(), secureExchangeCommentSagaData, saga);
    } else {
      getEdxSecureExchangeCommentOrchestratorService().createSecureExchangeComment(secureExchangeCommentSagaData, saga);
    }
    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_SECURE_EXCHANGE_COMMENT).eventOutcome(SECURE_EXCHANGE_COMMENT_CREATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(secureExchangeCommentSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for CREATE_SECURE_EXCHANGE_COMMENT Event.");
  }
}
