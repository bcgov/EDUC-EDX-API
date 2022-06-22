package ca.bc.gov.educ.api.edx.orchestrator.base;


import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.NotificationEvent;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.INITIATE_SUCCESS;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SAGA_COMPLETED;
import static ca.bc.gov.educ.api.edx.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.MARK_SAGA_COMPLETE;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.COMPLETED;
import static lombok.AccessLevel.PROTECTED;
import static lombok.AccessLevel.PUBLIC;

/**
 * The type Base orchestrator.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class BaseOrchestrator<T> implements EventHandler, Orchestrator {
  /**
   * The constant SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT.
   */
  protected static final String SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT = "system is going to execute next event :: {} for current event {} and SAGA ID :: {}";
  /**
   * The constant SELF
   */
  protected static final String SELF = "SELF";
  /**
   * The Clazz.
   */
  protected final Class<T> clazz;
  /**
   * The Next steps to execute.
   */
  protected final Map<EventType, List<SagaEventState<T>>> nextStepsToExecute = new LinkedHashMap<>();
  /**
   * The SagaEntity service.
   */
  @Getter(PROTECTED)
  private final SagaService sagaService;
  /**
   * The Message publisher.
   */
  @Getter(PROTECTED)
  private final MessagePublisher messagePublisher;
  /**
   * The SagaEntity name.
   */
  @Getter(PUBLIC)
  private final String sagaName;
  /**
   * The Topic to subscribe.
   */
  @Getter(PUBLIC)
  private final String topicToSubscribe;
  /**
   * The flag to indicate whether t
   */
  @Setter(PROTECTED)
  protected boolean shouldSendNotificationEvent = true;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService      the saga service
   * @param messagePublisher the message publisher
   * @param clazz            the clazz
   * @param sagaName         the saga name
   * @param topicToSubscribe the topic to subscribe
   */
  protected BaseOrchestrator(final SagaService sagaService, final MessagePublisher messagePublisher,
                             final Class<T> clazz, final String sagaName,
                             final String topicToSubscribe) {
    this.sagaService = sagaService;
    this.messagePublisher = messagePublisher;
    this.clazz = clazz;
    this.sagaName = sagaName;
    this.topicToSubscribe = topicToSubscribe;
    this.populateStepsToExecuteMap();
  }

  /**
   * Create single collection event state list.
   *
   * @param eventOutcome      the event outcome
   * @param nextStepPredicate the next step predicate
   * @param nextEventType     the next event type
   * @param stepToExecute     the step to execute
   * @return the list
   */
  protected List<SagaEventState<T>> createSingleCollectionEventState(final EventOutcome eventOutcome, final Predicate<T> nextStepPredicate, final EventType nextEventType, final SagaStep<T> stepToExecute) {
    final List<SagaEventState<T>> eventStates = new ArrayList<>();
    eventStates.add(this.buildSagaEventState(eventOutcome, nextStepPredicate, nextEventType, stepToExecute));
    return eventStates;
  }


  /**
   * Build saga event state saga event state.
   *
   * @param eventOutcome      the event outcome
   * @param nextStepPredicate the next step predicate
   * @param nextEventType     the next event type
   * @param stepToExecute     the step to execute
   * @return the saga event state
   */
  protected SagaEventState<T> buildSagaEventState(final EventOutcome eventOutcome, final Predicate<T> nextStepPredicate, final EventType nextEventType, final SagaStep<T> stepToExecute) {
    return SagaEventState.<T>builder().currentEventOutcome(eventOutcome).nextStepPredicate(nextStepPredicate).nextEventType(nextEventType).stepToExecute(stepToExecute).build();
  }


  /**
   * Register step to execute base orchestrator.
   *
   * @param initEvent         the init event
   * @param outcome           the outcome
   * @param nextStepPredicate the next step predicate
   * @param nextEvent         the next event
   * @param stepToExecute     the step to execute
   * @return the base orchestrator
   */
  protected BaseOrchestrator<T> registerStepToExecute(final EventType initEvent, final EventOutcome outcome, final Predicate<T> nextStepPredicate, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    if (this.nextStepsToExecute.containsKey(initEvent)) {
      final List<SagaEventState<T>> states = this.nextStepsToExecute.get(initEvent);
      states.add(this.buildSagaEventState(outcome, nextStepPredicate, nextEvent, stepToExecute));
    } else {
      this.nextStepsToExecute.put(initEvent, this.createSingleCollectionEventState(outcome, nextStepPredicate, nextEvent, stepToExecute));
    }
    return this;
  }

  /**
   * Step base orchestrator.
   *
   * @param currentEvent  the event that has occurred.
   * @param outcome       outcome of the event.
   * @param nextEvent     next event that will occur.
   * @param stepToExecute which method to execute for the next event. it is a lambda function.
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> step(final EventType currentEvent, final EventOutcome outcome, final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(currentEvent, outcome, (T sagaData) -> true, nextEvent, stepToExecute);
  }

  /**
   * Beginning step base orchestrator.
   *
   * @param nextEvent     next event that will occur.
   * @param stepToExecute which method to execute for the next event. it is a lambda function.
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> begin(final EventType nextEvent, final SagaStep<T> stepToExecute) {
    return this.registerStepToExecute(INITIATED, INITIATE_SUCCESS, (T sagaData) -> true, nextEvent, stepToExecute);
  }


  /**
   * End step base orchestrator with complete status.
   *
   * @param currentEvent the event that has occurred.
   * @param outcome      outcome of the event.
   */
  public void end(final EventType currentEvent, final EventOutcome outcome) {
    this.registerStepToExecute(currentEvent, outcome, (T sagaData) -> true, MARK_SAGA_COMPLETE, this::markSagaComplete);
  }


  /**
   * this is a simple and convenient method to trigger builder pattern in the child classes.
   *
   * @return {@link BaseOrchestrator}
   */
  public BaseOrchestrator<T> stepBuilder() {
    return this;
  }

  /**
   * this method will check if the event is not already processed. this could happen in SAGAs due to duplicate messages.
   * Application should be able to handle this.
   *
   * @param currentEventType current event.
   * @param saga             the model object.
   * @param eventTypes       event types stored in the hashmap
   * @return true or false based on whether the current event with outcome received from the queue is already processed or not.
   */
  protected boolean isNotProcessedEvent(final EventType currentEventType, final SagaEntity saga, final Set<EventType> eventTypes) {
    final EventType eventTypeInDB = EventType.valueOf(saga.getSagaState());
    final List<EventType> events = new LinkedList<>(eventTypes);
    final int dbEventIndex = events.indexOf(eventTypeInDB);
    final int currentEventIndex = events.indexOf(currentEventType);
    return currentEventIndex >= dbEventIndex;
  }

  /**
   * creates the PenRequestSagaEventState object
   *
   * @param saga         the payload.
   * @param eventType    event type
   * @param eventOutcome outcome
   * @param eventPayload payload.
   * @return {@link SagaEventStatesEntity}
   */
  protected SagaEventStatesEntity createEventState(@NotNull final SagaEntity saga, @NotNull final EventType eventType, @NotNull final EventOutcome eventOutcome, final String eventPayload) {
    final var user = this.sagaName.length() > 32 ? this.sagaName.substring(0, 32) : this.sagaName;
    return SagaEventStatesEntity.builder()
      .createDate(LocalDateTime.now())
      .createUser(user)
      .updateDate(LocalDateTime.now())
      .updateUser(user)
      .saga(saga)
      .sagaEventOutcome(eventOutcome.toString())
      .sagaEventState(eventType.toString())
      .sagaStepNumber(this.calculateStep(saga))
      .sagaEventResponse(eventPayload == null ? " " : eventPayload)
      .build();
  }

  /**
   * This method updates the DB and marks the process as complete.
   *
   * @param event    the current event.
   * @param saga     the saga model object.
   * @param sagaData the payload string as object.
   */
  protected void markSagaComplete(final Event event, final SagaEntity saga, final T sagaData) {
    this.markSagaComplete(event, saga, sagaData, "");
  }

  /**
   * This method updates the DB and marks the process as complete.
   *
   * @param event                the current event.
   * @param saga                 the saga model object.
   * @param sagaData             the payload string as object.
   * @param payloadToSubscribers the event payload to subscribers
   */
  protected void markSagaComplete(final Event event, final SagaEntity saga, final T sagaData, final String payloadToSubscribers) {
    log.trace("payload is {}", sagaData);
    if (this.shouldSendNotificationEvent) {
      final var finalEvent = new NotificationEvent();
      BeanUtils.copyProperties(event, finalEvent);
      finalEvent.setEventType(MARK_SAGA_COMPLETE);
      finalEvent.setEventOutcome(SAGA_COMPLETED);
      finalEvent.setSagaStatus(COMPLETED.toString());
      finalEvent.setSagaName(this.getSagaName());
      finalEvent.setEventPayload(payloadToSubscribers);
      this.postMessageToTopic(this.getTopicToSubscribe(), finalEvent);
    }

    final SagaEventStatesEntity sagaEventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(COMPLETED.toString());
    saga.setStatus(COMPLETED.toString());
    saga.setUpdateDate(LocalDateTime.now());
    this.getSagaService().updateAttachedSagaWithEvents(saga, sagaEventStates);

  }

  /**
   * calculate step number
   *
   * @param saga the model object.
   * @return step number that was calculated.
   */
  private int calculateStep(final SagaEntity saga) {
    val sagaStates = this.getSagaService().findAllSagaStates(saga);
    return (sagaStates.size() + 1);
  }

  /**
   * convenient method to post message to topic, to be used by child classes.
   *
   * @param topicName topic name where the message will be posted.
   * @param nextEvent the next event object.
   */
  protected void postMessageToTopic(final String topicName, final Event nextEvent) {
    try {
      final String eventStringOptional = JsonUtil.getJsonStringFromObject(nextEvent);
      this.getMessagePublisher().dispatchMessage(topicName, eventStringOptional.getBytes());
    } catch (JsonProcessingException e) {
      log.error("JsonProcessingException for   :: {} :: this should not have happened", nextEvent);
      throw new SagaRuntimeException(e);
    }
  }

  /**
   * it finds the last event that was processed successfully for this saga.
   *
   * @param eventStates event states corresponding to the SagaEntity.
   * @return {@link SagaEventStatesEntity} if found else null.
   */
  protected Optional<SagaEventStatesEntity> findTheLastEventOccurred(final List<SagaEventStatesEntity> eventStates) {
    final int step = eventStates.stream().map(SagaEventStatesEntity::getSagaStepNumber).mapToInt(x -> x).max().orElse(0);
    return eventStates.stream().filter(element -> element.getSagaStepNumber() == step).findFirst();
  }

  /**
   * this method is called from the cron job , which will replay the saga process based on its current state.
   *
   * @param saga the model object.
   * @throws IOException          if there is connectivity problem
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   */
  @Override
  @Transactional
  @Async("taskExecutor")
  public void replaySaga(final SagaEntity saga) throws IOException, InterruptedException, TimeoutException {
    final var eventStates = this.getSagaService().findAllSagaStates(saga);
    final var t = JsonUtil.getJsonObjectFromString(this.clazz, saga.getPayload());
    if (eventStates.isEmpty()) { //process did not start last time, lets start from beginning.
      this.replayFromBeginning(saga, t);
    } else {
      this.replayFromLastEvent(saga, eventStates, t);
    }
  }

  /**
   * This method will restart the saga process from where it was left the last time. which could occur due to various reasons
   *
   * @param saga        the model object.
   * @param eventStates the event states corresponding to the saga
   * @param t           the payload string as an object
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   * @throws IOException          if there is connectivity problem
   */
  private void replayFromLastEvent(final SagaEntity saga, final List<SagaEventStatesEntity> eventStates, final T t) throws InterruptedException, TimeoutException, IOException {
    val sagaEventOptional = this.findTheLastEventOccurred(eventStates);
    if (sagaEventOptional.isPresent()) {
      val sagaEvent = sagaEventOptional.get();
      log.trace(sagaEventOptional.toString());
      final EventType currentEvent = EventType.valueOf(sagaEvent.getSagaEventState());
      final EventOutcome eventOutcome = EventOutcome.valueOf(sagaEvent.getSagaEventOutcome());
      final Event event = Event.builder()
        .eventOutcome(eventOutcome)
        .eventType(currentEvent)
        .eventPayload(sagaEvent.getSagaEventResponse())
        .build();
      this.findAndInvokeNextStep(saga, t, currentEvent, eventOutcome, event);
    }
  }

  /**
   * Find and invoke next step.
   *
   * @param saga         the saga
   * @param t            the t
   * @param currentEvent the current event
   * @param eventOutcome the event outcome
   * @param event        the event
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   * @throws IOException          the io exception
   */
  private void findAndInvokeNextStep(final SagaEntity saga, final T t, final EventType currentEvent, final EventOutcome eventOutcome, final Event event) throws InterruptedException, TimeoutException, IOException {
    final Optional<SagaEventState<T>> sagaEventState = this.findNextSagaEventState(currentEvent, eventOutcome, t);
    if (sagaEventState.isPresent()) {
      log.trace(SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT, sagaEventState.get().getNextEventType(), event.toString(), saga.getSagaId());
      this.invokeNextEvent(event, saga, t, sagaEventState.get());
    }
  }

  /**
   * This method will restart the saga process from the beginning. which could occur due to various reasons
   *
   * @param saga the model object.
   * @param t    the payload string as an object
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   * @throws IOException          if there is connectivity problem
   */
  private void replayFromBeginning(final SagaEntity saga, final T t) throws InterruptedException, TimeoutException, IOException {
    final Event event = Event.builder()
      .eventOutcome(INITIATE_SUCCESS)
      .eventType(INITIATED)
      .build();
    this.findAndInvokeNextStep(saga, t, INITIATED, INITIATE_SUCCESS, event);
  }

  /**
   * this method is called if there is a new message on this specific topic which this service is listening.
   *
   * @param event the event
   * @throws InterruptedException if thread is interrupted.
   * @throws IOException          if there is connectivity problem
   * @throws TimeoutException     if connection to messaging system times out.
   */
  @Override
  @Async("subscriberExecutor")
  @Transactional
  public void handleEvent(@NotNull final Event event) throws InterruptedException, IOException, TimeoutException {
    log.info("executing saga event {}", event);
    if (this.sagaEventExecutionNotRequired(event)) {
      log.trace("Execution is not required for this message returning EVENT is :: {}", event);
      return;
    }
    this.broadcastSagaInitiatedMessage(event);

    final var sagaOptional = this.getSagaService().findSagaById(event.getSagaId()); // system expects a saga record to be present here.
    if (sagaOptional.isPresent()) {
      val saga = sagaOptional.get();
      if (!COMPLETED.toString().equalsIgnoreCase(sagaOptional.get().getStatus())) {//possible duplicate message or force stop scenario check
        final T sagaData = JsonUtil.getJsonObjectFromString(this.clazz, saga.getPayload());
        final var sagaEventState = this.findNextSagaEventState(event.getEventType(), event.getEventOutcome(), sagaData);
        log.trace("found next event as {}", sagaEventState);
        if (sagaEventState.isPresent()) {
          this.process(event, saga, sagaData, sagaEventState.get());
        } else {
          log.error("This should not have happened, please check that both the saga api and all the participating apis are in sync in terms of events and their outcomes. {}", event); // more explicit error message,
        }
      } else {
        log.info("got message to process saga for saga ID :: {} but saga is already :: {}", saga.getSagaId(), saga.getStatus());
      }
    } else {
      log.error("SagaEntity process without DB record is not expected. {}", event);
    }
  }

  /**
   * Start to execute saga
   *
   * @param saga the saga data
   */
  @Override
  @Async("subscriberExecutor")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void startSaga(@NotNull final SagaEntity saga) {
    try {
      this.handleEvent(Event.builder()
        .eventType(EventType.INITIATED)
        .eventOutcome(EventOutcome.INITIATE_SUCCESS)
        .sagaId(saga.getSagaId())
        .eventPayload(saga.getPayload())
        .build());
    } catch (InterruptedException e) {
      log.error("InterruptedException while startSaga", e);
      Thread.currentThread().interrupt();
    } catch (TimeoutException | IOException e) {
      log.error("Exception while startSaga", e);
    }
  }

  @Override
  @Transactional
  public SagaEntity createSaga(@NotNull final String payload, final UUID edxUserId, final String userName, final String mincode, final String emailId, final UUID secureExchangeId) {
    return this.sagaService.createSagaRecordInDB(this.sagaName, userName, payload, edxUserId, secureExchangeId, mincode, emailId);
  }


  /**
   * DONT DO ANYTHING the message was broad-casted for the frontend listeners, that a saga process has initiated, completed.
   *
   * @param event the event object received from queue.
   * @return true if this message need not be processed further.
   */
  private boolean sagaEventExecutionNotRequired(@NotNull final Event event) {
    return (event.getEventType() == INITIATED && event.getEventOutcome() == INITIATE_SUCCESS && SELF.equalsIgnoreCase(event.getReplyTo()))
      || event.getEventType() == MARK_SAGA_COMPLETE && event.getEventOutcome() == SAGA_COMPLETED;
  }

  /**
   * Broadcast the saga initiated message
   *
   * @param event the event object
   */
  private void broadcastSagaInitiatedMessage(@NotNull final Event event) {
    // !SELF.equalsIgnoreCase(event.getReplyTo()):- this check makes sure it is not broadcast-ed infinitely.
    if (this.shouldSendNotificationEvent && event.getEventType() == INITIATED && event.getEventOutcome() == INITIATE_SUCCESS
      && !SELF.equalsIgnoreCase(event.getReplyTo())) {
      final var notificationEvent = new NotificationEvent();
      BeanUtils.copyProperties(event, notificationEvent);
      notificationEvent.setSagaStatus(INITIATED.toString());
      notificationEvent.setReplyTo(SELF);
      notificationEvent.setSagaName(this.getSagaName());
      this.postMessageToTopic(this.getTopicToSubscribe(), notificationEvent);
    }
  }

  /**
   * this method finds the next event that needs to be executed.
   *
   * @param currentEvent current event
   * @param eventOutcome event outcome.
   * @param sagaData     the saga data
   * @return {@link Optional<SagaEventState>}
   */
  protected Optional<SagaEventState<T>> findNextSagaEventState(final EventType currentEvent, final EventOutcome eventOutcome, final T sagaData) {
    val sagaEventStates = this.nextStepsToExecute.get(currentEvent);
    return sagaEventStates == null ? Optional.empty() : sagaEventStates.stream().filter(el ->
      el.getCurrentEventOutcome() == eventOutcome && el.nextStepPredicate.test(sagaData)
    ).findFirst();
  }

  /**
   * this method starts the process of saga event execution.
   *
   * @param event          the current event.
   * @param saga           the model object.
   * @param sagaData       the saga data
   * @param sagaEventState the next next event from {@link BaseOrchestrator#nextStepsToExecute}
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   * @throws IOException          if there is connectivity problem
   */
  protected void process(@NotNull final Event event, final SagaEntity saga, final T sagaData, final SagaEventState<T> sagaEventState) throws InterruptedException, TimeoutException, IOException {
    if (!saga.getSagaState().equalsIgnoreCase(COMPLETED.toString())
      && this.isNotProcessedEvent(event.getEventType(), saga, this.nextStepsToExecute.keySet())) {
      log.info(SYSTEM_IS_GOING_TO_EXECUTE_NEXT_EVENT_FOR_CURRENT_EVENT, sagaEventState.getNextEventType(), event, saga.getSagaId());
      this.invokeNextEvent(event, saga, sagaData, sagaEventState);
    } else {
      log.info("ignoring this message as we have already processed it or it is completed. {}", event.toString()); // it is expected to receive duplicate message in saga pattern, system should be designed to handle duplicates.
    }
  }

  /**
   * this method will invoke the next event in the {@link BaseOrchestrator#nextStepsToExecute}
   *
   * @param event          the current event.
   * @param saga           the model object.
   * @param sagaData       the payload string
   * @param sagaEventState the next next event from {@link BaseOrchestrator#nextStepsToExecute}
   * @throws InterruptedException if thread is interrupted.
   * @throws TimeoutException     if connection to messaging system times out.
   * @throws IOException          if there is connectivity problem
   */
  protected void invokeNextEvent(final Event event, final SagaEntity saga, final T sagaData, final SagaEventState<T> sagaEventState) throws InterruptedException, TimeoutException, IOException {
    final SagaStep<T> stepToExecute = sagaEventState.getStepToExecute();
    stepToExecute.apply(event, saga, sagaData);
  }

  /**
   * Populate steps to execute map.
   */
  public abstract void populateStepsToExecuteMap();

}
