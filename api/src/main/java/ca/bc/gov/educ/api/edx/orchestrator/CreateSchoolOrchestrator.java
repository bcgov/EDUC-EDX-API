package ca.bc.gov.educ.api.edx.orchestrator;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.CREATE_NEW_SCHOOL_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_API_TOPIC;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

import ca.bc.gov.educ.api.edx.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.EdxSchoolUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.service.v1.CreateSchoolOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class CreateSchoolOrchestrator extends BaseOrchestrator<CreateSchoolSagaData> {

  @Getter(PRIVATE)
  private final Publisher publisher;

  @Getter(PRIVATE)
  private final EdxUsersService edxUsersService;

  @Getter(PRIVATE)
  private final CreateSchoolOrchestratorService orchestratorService;
  @Getter(PRIVATE)
  private final EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService;

  protected CreateSchoolOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, CreateSchoolOrchestratorService orchestratorService, Publisher publisher, EdxUsersService edxUsersService, EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService) {
    super(sagaService, messagePublisher, CreateSchoolSagaData.class, CREATE_NEW_SCHOOL_SAGA.toString(), EDX_API_TOPIC.toString());
    this.publisher = publisher;
    this.edxUsersService = edxUsersService;
    this.orchestratorService = orchestratorService;
    this.edxSchoolUserActivationInviteOrchestratorService = edxSchoolUserActivationInviteOrchestratorService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_SCHOOL, this::createSchool)
      .step(CREATE_SCHOOL, SCHOOL_CREATED, ONBOARD_INITIAL_USER, this::checkForInitialUser)
      .step(ONBOARD_INITIAL_USER, INITIAL_USER_FOUND, CREATE_SCHOOL_PRIMARY_CODE, this::createPrimaryCode)
      .end(ONBOARD_INITIAL_USER, NO_INITIAL_USER_FOUND, this::completeCreateSchoolSagaWithNoUser)
      .or()
      .step(CREATE_SCHOOL_PRIMARY_CODE, SCHOOL_PRIMARY_CODE_CREATED, SEND_PRIMARY_ACTIVATION_CODE, this::sendPrimaryCode)
      .step(SEND_PRIMARY_ACTIVATION_CODE, PRIMARY_ACTIVATION_CODE_SENT, CREATE_PERSONAL_ACTIVATION_CODE, this::createPersonalActivationCode)
      .step(CREATE_PERSONAL_ACTIVATION_CODE, PERSONAL_ACTIVATION_CODE_CREATED, SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL, this::sendEdxUserActivationEmail)
      .end(SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL, EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT);
  }

  protected void createPersonalActivationCode(Event event, SagaEntity saga, CreateSchoolSagaData createSchoolSagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(CREATE_PERSONAL_ACTIVATION_CODE.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    if (createSchoolSagaData.getInviteSagaData().getEdxActivationCodeId() == null ) {//idempotency check
      getEdxSchoolUserActivationInviteOrchestratorService().createPersonalActivationCodeAndUpdateSagaData(createSchoolSagaData, saga); // one transaction updates three tables.
    } else {
      EdxActivationCodeEntity edxActivationCodeEntity = getEdxSchoolUserActivationInviteOrchestratorService().getActivationCodeById(UUID.fromString(createSchoolSagaData.getInviteSagaData().getEdxActivationCodeId()));
      getEdxSchoolUserActivationInviteOrchestratorService().updateSagaData(createSchoolSagaData, edxActivationCodeEntity, saga);
    }

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(CREATE_PERSONAL_ACTIVATION_CODE).eventOutcome(PERSONAL_ACTIVATION_CODE_CREATED)
            .eventPayload(JsonUtil.getJsonStringFromObject(createSchoolSagaData))
            .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for CREATE_PERSONAL_ACTIVATION_CODE Event.");
  }

  protected void sendEdxUserActivationEmail(Event event, SagaEntity saga, CreateSchoolSagaData createSchoolSagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    log.debug("createSchoolSagaData :: {}", createSchoolSagaData);
    getEdxSchoolUserActivationInviteOrchestratorService().sendEmail(createSchoolSagaData.getInviteSagaData());

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL)
            .eventOutcome(EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT)
            .replyTo(this.getTopicToSubscribe())
            .eventPayload(JsonUtil.getJsonStringFromObject(createSchoolSagaData))
            .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for SEND_EDX_USER_ACTIVATION_EMAIL Event.");
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

  public void checkForInitialUser(Event event, SagaEntity saga, CreateSchoolSagaData sagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(ONBOARD_INITIAL_USER.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    final Event.EventBuilder nextEventBuilder = Event.builder()
      .eventType(ONBOARD_INITIAL_USER)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .sagaId(saga.getSagaId());

    if (sagaData.getInitialEdxUser().isPresent()) {
      School createdSchoolFromInstitute = JsonUtil.getJsonObjectFromString(School.class, event.getEventPayload());
      this.orchestratorService.attachInstituteSchoolToSaga(createdSchoolFromInstitute.getSchoolId(), saga);
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
