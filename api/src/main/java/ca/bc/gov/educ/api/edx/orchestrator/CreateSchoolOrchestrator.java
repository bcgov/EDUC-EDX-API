package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.service.v1.CreateSchoolOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EdxSchoolUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.CREATE_NEW_SCHOOL_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_API_TOPIC;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class CreateSchoolOrchestrator extends SchoolUserActivationBaseOrchestrator<CreateSchoolSagaData> {

  @Getter(PRIVATE)
  private final Publisher publisher;

  @Getter(PRIVATE)
  private final EdxUsersService edxUsersService;

  @Getter(PRIVATE)
  private final CreateSchoolOrchestratorService orchestratorService;
  @Getter(PRIVATE)
  private final EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService;

  protected CreateSchoolOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, CreateSchoolOrchestratorService orchestratorService, Publisher publisher, EdxUsersService edxUsersService, EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService) {
    super(sagaService, messagePublisher, CreateSchoolSagaData.class, CREATE_NEW_SCHOOL_SAGA.toString(), EDX_API_TOPIC.toString(), edxSchoolUserActivationInviteOrchestratorService);
    this.publisher = publisher;
    this.edxUsersService = edxUsersService;
    this.orchestratorService = orchestratorService;
    this.edxSchoolUserActivationInviteOrchestratorService = edxSchoolUserActivationInviteOrchestratorService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_SCHOOL, this::createSchool)
      .step(CREATE_SCHOOL, SCHOOL_CREATED, ONBOARD_INITIAL_USER, this::storeInitialUserIfFound)
      .step(ONBOARD_INITIAL_USER, INITIAL_USER_FOUND, CREATE_SCHOOL_PRIMARY_CODE, this::createPrimaryCode)
      .end(ONBOARD_INITIAL_USER, NO_INITIAL_USER_FOUND, this::completeCreateSchoolSagaWithNoUser)
      .or()
      .step(CREATE_SCHOOL_PRIMARY_CODE, SCHOOL_PRIMARY_CODE_CREATED, SEND_PRIMARY_ACTIVATION_CODE, this::sendPrimaryCode)
      .step(SEND_PRIMARY_ACTIVATION_CODE, PRIMARY_ACTIVATION_CODE_SENT, CREATE_PERSONAL_ACTIVATION_CODE, this::createPersonalActivationCode)
      .step(CREATE_PERSONAL_ACTIVATION_CODE, PERSONAL_ACTIVATION_CODE_CREATED, SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL, this::sendEdxUserActivationEmail)
      .end(SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL, EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT);
  }

  public void createSchool(Event event, SagaEntity saga, CreateSchoolSagaData sagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(CREATE_SCHOOL.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    School school = sagaData.getSchool();

    final Event instituteEvent = Event.builder()
      .eventType(CREATE_SCHOOL)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(school))
      .sagaId(saga.getSagaId())
      .build();

    this.postMessageToTopic(INSTITUTE_API_TOPIC.toString(), instituteEvent);
    log.info("message sent to INSTITUTE_API_TOPIC for CREATE_SCHOOL Event. :: {}", saga.getSagaId());
  }

  public void storeInitialUserIfFound(Event event, SagaEntity saga, CreateSchoolSagaData sagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(ONBOARD_INITIAL_USER.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event.EventBuilder nextEventBuilder = Event.builder()
      .eventType(ONBOARD_INITIAL_USER)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .sagaId(saga.getSagaId());

    if (sagaData.getInitialEdxUser() != null) {
      School createdSchoolFromInstitute = JsonUtil.getJsonObjectFromString(School.class, event.getEventPayload());
      this.orchestratorService.attachSchoolAndUserInviteToSaga(createdSchoolFromInstitute.getSchoolId(), sagaData, saga);
      nextEventBuilder.eventOutcome(INITIAL_USER_FOUND);
    } else {
      nextEventBuilder.eventOutcome(NO_INITIAL_USER_FOUND);
    }

    Event nextEvent = nextEventBuilder.build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for ONBOARD_INITIAL_USER Event. :: {}", saga.getSagaId());
  }

  public void createPrimaryCode(Event event, SagaEntity saga, CreateSchoolSagaData sagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
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
    log.info("message sent to EDX_API_TOPIC for CREATE_SCHOOL_PRIMARY_CODE Event. :: {}", saga.getSagaId());
  }

  public void sendPrimaryCode(Event event, SagaEntity saga, CreateSchoolSagaData sagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(SEND_PRIMARY_ACTIVATION_CODE.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    this.orchestratorService.sendPrimaryActivationCodeNotification(sagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(SEND_PRIMARY_ACTIVATION_CODE).eventOutcome(PRIMARY_ACTIVATION_CODE_SENT)
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for SEND_PRIMARY_ACTIVATION_CODE Event. :: {}", saga.getSagaId());
  }

  private void completeCreateSchoolSagaWithNoUser(final Event event, final SagaEntity saga, final CreateSchoolSagaData sagaData) {
    log.info("CREATE_NEW_SCHOOL_SAGA has ended with NO_INITIAL_USER_FOUND :: {}", saga.getSagaId());
  }
}
