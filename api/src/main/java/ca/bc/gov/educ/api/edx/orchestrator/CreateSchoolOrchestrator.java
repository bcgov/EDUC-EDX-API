package ca.bc.gov.educ.api.edx.orchestrator;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.CREATE_SCHOOL_SAGA_HAS_ADMIN;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.CREATE_SCHOOL_SAGA_HAS_NO_ADMIN;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SCHOOL_PRIMARY_CODE_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.PRIMARY_ACTIVATION_CODE_SENT;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.INITIAL_USER_INVITED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_SCHOOL;
import static ca.bc.gov.educ.api.edx.constants.EventType.INVITE_INITIAL_USER;
import static ca.bc.gov.educ.api.edx.constants.EventType.SEND_PRIMARY_ACTIVATION_CODE;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_SCHOOL_PRIMARY_CODE;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.CREATE_NEW_SCHOOL_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_API_TOPIC;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.beans.BeanUtils.copyProperties;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;


import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.CreateSchoolOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CreateSchoolOrchestrator extends BaseOrchestrator<CreateSchoolSagaData> {

  @Getter(PRIVATE)
  private final Publisher publisher;

  @Getter(PRIVATE)
  private final EdxUsersService edxUsersService;

  @Getter(PRIVATE)
  private final CreateSchoolOrchestratorService orchestratorService;


  /**
     * Instantiates a new Base orchestrator.
     *
     * @param sagaService                   the saga service
     * @param messagePublisher              the message publisher
     * @param inviteOrchestratorService
     * @param publisher
     */
  protected CreateSchoolOrchestrator(
    SagaService sagaService,
    MessagePublisher messagePublisher,
    CreateSchoolOrchestratorService orchestratorService,
    Publisher publisher,
    EdxUsersService edxUsersService
  ) {
    super(
      sagaService,
      messagePublisher,
      CreateSchoolSagaData.class,
      CREATE_NEW_SCHOOL_SAGA.toString(),
      EDX_API_TOPIC.toString()
    );
    this.publisher = publisher;
    this.edxUsersService = edxUsersService;
    this.orchestratorService = orchestratorService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_SCHOOL, this::createSchool)
      .step(CREATE_SCHOOL, CREATE_SCHOOL_SAGA_HAS_ADMIN, CREATE_SCHOOL_PRIMARY_CODE, this::createPrimaryCode)
      .end(CREATE_SCHOOL, CREATE_SCHOOL_SAGA_HAS_NO_ADMIN, this::completeCreateSchoolSagaWithNoUser)
      .or()
      .step(CREATE_SCHOOL_PRIMARY_CODE, SCHOOL_PRIMARY_CODE_CREATED, SEND_PRIMARY_ACTIVATION_CODE, this::sendPrimaryCode)
      .step(SEND_PRIMARY_ACTIVATION_CODE, PRIMARY_ACTIVATION_CODE_SENT, INVITE_INITIAL_USER, this::inviteInitialUser)
      .end(INVITE_INITIAL_USER, INITIAL_USER_INVITED);
  }

  public void createSchool(Event event, SagaEntity saga, CreateSchoolSagaData sagaData)
  throws JsonProcessingException {
    final SagaEventStatesEntity eventStates =
      this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_SCHOOL.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    School school = sagaData.getSchool();

    final Event.EventBuilder eventBuilder = Event.builder()
      .eventType(CREATE_SCHOOL)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(school));

    if (sagaData.getInitialEdxUser().isEmpty()) {
      eventBuilder.eventOutcome(CREATE_SCHOOL_SAGA_HAS_NO_ADMIN);
    } else {
      eventBuilder.eventOutcome(CREATE_SCHOOL_SAGA_HAS_ADMIN);
    }

    this.postMessageToTopic(INSTITUTE_API_TOPIC.toString(), eventBuilder.build());
    log.info("message sent to INSTITUTE_API_TOPIC for CREATE_SCHOOL Event. :: {}", saga.getSagaId());
  }

  public void createPrimaryCode(Event event, SagaEntity saga, CreateSchoolSagaData sagaData)
  throws JsonProcessingException {
    final SagaEventStatesEntity eventStates =
      this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_SCHOOL_PRIMARY_CODE.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    this.orchestratorService.createPrimaryActivationCode(sagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_SCHOOL_PRIMARY_CODE)
      .eventOutcome(SCHOOL_PRIMARY_CODE_CREATED)
      .replyTo(getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    publishToJetStream(nextEvent, saga);
    log.info("message sent to EDX_API_TOPIC for CREATE_SCHOOL_PRIMARY_CODE Event. :: {}", saga.getSagaId());
  }

  public void sendPrimaryCode(Event event, SagaEntity saga, CreateSchoolSagaData sagaData)
  throws JsonProcessingException {
    final SagaEventStatesEntity eventStates =
      this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(SEND_PRIMARY_ACTIVATION_CODE.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    School school = JsonUtil.getJsonObjectFromString(School.class, saga.getPayload());
    copyProperties(school, sagaData);

    this.orchestratorService.sendPrimaryActivationCodeNotification(sagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(SEND_PRIMARY_ACTIVATION_CODE).eventOutcome(PRIMARY_ACTIVATION_CODE_SENT)
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    publishToJetStream(nextEvent, saga);
    log.info("message sent to EDX_API_TOPIC for SEND_PRIMARY_ACTIVATION_CODE Event. :: {}", saga.getSagaId());
  }

  private void inviteInitialUser(Event event, SagaEntity saga, CreateSchoolSagaData sagaData)
  throws JsonProcessingException {
    final SagaEventStatesEntity eventStates =
      this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(INVITE_INITIAL_USER.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    this.orchestratorService.startEdxSchoolUserInviteSaga(sagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(INVITE_INITIAL_USER).eventOutcome(INITIAL_USER_INVITED)
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    publishToJetStream(nextEvent, saga);
    log.info("message sent to EDX_API_TOPIC for INVITE_INITIAL_USER Event. :: {}", saga.getSagaId());
  }

  private void completeCreateSchoolSagaWithNoUser(
    final Event event,
    final SagaEntity saga,
    final CreateSchoolSagaData sagaData
  ) throws JsonProcessingException {
    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_SCHOOL)
      .eventOutcome(CREATE_SCHOOL_SAGA_HAS_NO_ADMIN)
      .replyTo(getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    publishToJetStream(nextEvent, saga);
    log.info("CreateSchoolSaga has ended without an initial admin user being created :: {}", saga.getSagaId());
  }

  private void publishToJetStream(final Event event, SagaEntity saga) {
    publisher.dispatchChoreographyEvent(event, saga);
  }
}