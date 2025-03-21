package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.service.v1.EdxDistrictUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.service.v1.OnboardUserOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.OnboardDistrictUserSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.ONBOARD_DISTRICT_USER_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.*;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class OnboardDistrictUserOrchestrator extends DistrictUserActivationBaseOrchestrator<OnboardDistrictUserSagaData> {

  @Getter(PRIVATE)
  private final Publisher publisher;

  @Getter(PRIVATE)
  private final EdxUsersService edxUsersService;

  @Getter(PRIVATE)
  private final OnboardUserOrchestratorService orchestratorService;
  @Getter(PRIVATE)
  private final EdxDistrictUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService;

  protected OnboardDistrictUserOrchestrator(
    SagaService sagaService,
    MessagePublisher messagePublisher,
    OnboardUserOrchestratorService orchestratorService,
    Publisher publisher,
    EdxUsersService edxUsersService,
    EdxDistrictUserActivationInviteOrchestratorService edxDistrictUserActivationInviteOrchestratorService
  ) {
    super(
      sagaService,
      messagePublisher,
      OnboardDistrictUserSagaData.class,
      ONBOARD_DISTRICT_USER_SAGA.toString(),
      EDX_ONBOARD_DISTRICT_USER_TOPIC.toString(),
      edxDistrictUserActivationInviteOrchestratorService
    );
    this.publisher = publisher;
    this.edxUsersService = edxUsersService;
    this.orchestratorService = orchestratorService;
    this.edxSchoolUserActivationInviteOrchestratorService = edxDistrictUserActivationInviteOrchestratorService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_DISTRICT_PRIMARY_CODE, this::createPrimaryCode)
      .step(CREATE_DISTRICT_PRIMARY_CODE, DISTRICT_PRIMARY_CODE_CREATED, SEND_PRIMARY_ACTIVATION_CODE, this::sendPrimaryCode)
      .step(SEND_PRIMARY_ACTIVATION_CODE, PRIMARY_ACTIVATION_CODE_SENT, CREATE_PERSONAL_ACTIVATION_CODE, this::createPersonalActivationCode)
      .step(CREATE_PERSONAL_ACTIVATION_CODE, PERSONAL_ACTIVATION_CODE_CREATED, SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL, this::sendEdxUserActivationEmail)
      .end(SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL, EDX_DISTRICT_USER_ACTIVATION_EMAIL_SENT);
  }

  public void createPrimaryCode(Event event, SagaEntity saga, OnboardDistrictUserSagaData sagaData) throws JsonProcessingException {
    final EventType eventTypeValue = EventType.valueOf(event.getEventType());
    final EventOutcome eventOutcomeValue = EventOutcome.valueOf(event.getEventOutcome());
    final SagaEventStatesEntity eventStates = this.createEventState(saga, eventTypeValue, eventOutcomeValue, event.getEventPayload());
    saga.setSagaState(CREATE_SCHOOL_PRIMARY_CODE.toString());
    saga.setStatus(IN_PROGRESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    this.orchestratorService.createPrimaryActivationCode(sagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_DISTRICT_PRIMARY_CODE.toString())
      .eventOutcome(DISTRICT_PRIMARY_CODE_CREATED.toString())
      .replyTo(getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_ONBOARD_DISTRICT_USER_TOPIC for CREATE_SCHOOL_PRIMARY_CODE Event. :: {}", saga.getSagaId());
  }

  public void sendPrimaryCode(Event event, SagaEntity saga, OnboardDistrictUserSagaData sagaData) throws JsonProcessingException {
    final EventType eventTypeValue = EventType.valueOf(event.getEventType());
    final EventOutcome eventOutcomeValue = EventOutcome.valueOf(event.getEventOutcome());
    final SagaEventStatesEntity eventStates = this.createEventState(saga, eventTypeValue, eventOutcomeValue, event.getEventPayload());
    saga.setSagaState(SEND_PRIMARY_ACTIVATION_CODE.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    this.orchestratorService.sendPrimaryActivationCodeNotification(sagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(SEND_PRIMARY_ACTIVATION_CODE.toString()).eventOutcome(PRIMARY_ACTIVATION_CODE_SENT.toString())
      .eventPayload(JsonUtil.getJsonStringFromObject(sagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_ONBOARD_DISTRICT_USER_TOPIC for SEND_PRIMARY_ACTIVATION_CODE Event. :: {}", saga.getSagaId());
  }
}
