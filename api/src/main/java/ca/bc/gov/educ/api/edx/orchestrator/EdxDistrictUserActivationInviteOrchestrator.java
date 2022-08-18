package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.mappers.v1.EdxActivationCodeMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.EdxDistrictUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxDistrictUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.EDX_DISTRICT_USER_ACTIVATION_EMAIL_SENT;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.PERSONAL_ACTIVATION_CODE_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_PERSONAL_ACTIVATION_CODE;
import static ca.bc.gov.educ.api.edx.constants.EventType.SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_DISTRICT_USER_ACTIVATION_INVITE_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_DISTRICT_USER_ACTIVATION_INVITE_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class EdxDistrictUserActivationInviteOrchestrator extends BaseOrchestrator<EdxDistrictUserActivationInviteSagaData> {

  protected static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;

  @Getter(PRIVATE)
  private final EdxDistrictUserActivationInviteOrchestratorService edxDistrictUserActivationInviteOrchestratorService;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                                        the saga service
   * @param messagePublisher                                   the message publisher
   * @param edxDistrictUserActivationInviteOrchestratorService
   */
  protected EdxDistrictUserActivationInviteOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, EdxDistrictUserActivationInviteOrchestratorService edxDistrictUserActivationInviteOrchestratorService) {
    super(sagaService, messagePublisher, EdxDistrictUserActivationInviteSagaData.class, EDX_DISTRICT_USER_ACTIVATION_INVITE_SAGA.toString(), EDX_DISTRICT_USER_ACTIVATION_INVITE_TOPIC.toString());
    this.edxDistrictUserActivationInviteOrchestratorService = edxDistrictUserActivationInviteOrchestratorService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_PERSONAL_ACTIVATION_CODE, this::createPersonalActivationCode)
      .step(CREATE_PERSONAL_ACTIVATION_CODE, PERSONAL_ACTIVATION_CODE_CREATED, SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL, this::sendEdxUserActivationEmail)
      .end(SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL, EDX_DISTRICT_USER_ACTIVATION_EMAIL_SENT);
  }

  protected void createPersonalActivationCode(Event event, SagaEntity saga, EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
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
      .eventType(CREATE_PERSONAL_ACTIVATION_CODE).eventOutcome(PERSONAL_ACTIVATION_CODE_CREATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(edxDistrictUserActivationInviteSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for CREATE_PERSONAL_ACTIVATION_CODE Event.");
  }


  protected void sendEdxUserActivationEmail(Event event, SagaEntity saga, EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setSagaState(SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL.toString()); // set current event as saga state.
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
    log.debug("edxDistrictUserActivationInviteSagaData :: {}", edxDistrictUserActivationInviteSagaData);
    getEdxDistrictUserActivationInviteOrchestratorService().sendEmail(edxDistrictUserActivationInviteSagaData);

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL)
      .eventOutcome(EDX_DISTRICT_USER_ACTIVATION_EMAIL_SENT)
      .replyTo(this.getTopicToSubscribe())
      .eventPayload(JsonUtil.getJsonStringFromObject(edxDistrictUserActivationInviteSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL Event.");
  }
}
