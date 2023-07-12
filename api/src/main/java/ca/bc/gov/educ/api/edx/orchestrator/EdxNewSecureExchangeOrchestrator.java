package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.EdxNewSecureExchangeOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.NEW_SECURE_EXCHANGE_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_NEW_SECURE_EXCHANGE_TOPIC;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Edx new secure exchange orchestrator.
 */
@Component
@Slf4j
public class EdxNewSecureExchangeOrchestrator extends BaseOrchestrator<SecureExchangeCreateSagaData> {


  /**
   * The Edx new secure exchange orchestror service.
   */
  @Getter(PRIVATE)
  private final EdxNewSecureExchangeOrchestratorService edxNewSecureExchangeOrchestratorService;
  private final Publisher publisher;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                             the saga service
   * @param messagePublisher                        the message publisher
   * @param edxNewSecureExchangeOrchestratorService the edx new secure exchange orchestror service
   */
  protected EdxNewSecureExchangeOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, EdxNewSecureExchangeOrchestratorService edxNewSecureExchangeOrchestratorService, Publisher publisher) {
    super(sagaService, messagePublisher, SecureExchangeCreateSagaData.class, NEW_SECURE_EXCHANGE_SAGA.toString(), EDX_NEW_SECURE_EXCHANGE_TOPIC.toString());
    this.edxNewSecureExchangeOrchestratorService = edxNewSecureExchangeOrchestratorService;
    this.publisher = publisher;
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_NEW_SECURE_EXCHANGE, this::createNewSecureExchange)
      .step(CREATE_NEW_SECURE_EXCHANGE, NEW_SECURE_EXCHANGE_CREATED, SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE, this::sendEmailForNewSecureExchange)
      .end(SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE, EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE_SENT);
  }

  /**
   * Create new secure exchange.
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   * @throws JsonProcessingException the json processing exception
   */
  private void createNewSecureExchange(Event event, SagaEntity saga, SecureExchangeCreateSagaData secureExchangeCreateSagaData) throws JsonProcessingException {

    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(CREATE_NEW_SECURE_EXCHANGE.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    if (secureExchangeCreateSagaData.getSecureExchangeId() == null) {// handle repeat scenario and deal with skipping the creation.
      getEdxNewSecureExchangeOrchestratorService().createNewSecureExchange(secureExchangeCreateSagaData, saga);
    } else {
      val existingSEEntity = getEdxNewSecureExchangeOrchestratorService().getSecureExchangeById(secureExchangeCreateSagaData.getSecureExchangeId());
      getEdxNewSecureExchangeOrchestratorService().updateSagaData(existingSEEntity, secureExchangeCreateSagaData, saga);
    }

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_NEW_SECURE_EXCHANGE).eventOutcome(NEW_SECURE_EXCHANGE_CREATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(secureExchangeCreateSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for CREATE_NEW_SECURE_EXCHANGE Event.");
  }

  /**
   * Send email for new secure exchange.
   *
   * @param event                        the event
   * @param saga                         the saga
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   * @throws JsonProcessingException the json processing exception
   */
  private void sendEmailForNewSecureExchange(Event event, SagaEntity saga, SecureExchangeCreateSagaData secureExchangeCreateSagaData) throws JsonProcessingException {

    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    log.debug("secureExchangeCreateSagaData :: {}", secureExchangeCreateSagaData);
    getEdxNewSecureExchangeOrchestratorService().sendEmail(secureExchangeCreateSagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE)
      .eventOutcome(EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE_SENT)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(secureExchangeCreateSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    publishToJetStream(nextEvent, saga);
    log.info("message sent to EDX_API_TOPIC for SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE Event.");
  }

  private void publishToJetStream(final Event event, SagaEntity saga) {
    publisher.dispatchChoreographyEvent(event, saga);
  }
}
