package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxActivationCodeMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.service.v1.EdxSchoolUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationRelinkSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_SCHOOL_USER_ACTIVATION_RELINK_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_SCHOOL_USER_ACTIVATION_RELINK_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class EdxSchoolUserActivationRelinkOrchestrator extends SchoolUserActivationBaseOrchestrator<EdxUserSchoolActivationRelinkSagaData> {

  protected static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;

  @Getter(PRIVATE)
  private final EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService;

  @Getter(PRIVATE)
  private final EdxUsersService edxUsersService;

  public EdxSchoolUserActivationRelinkOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService, EdxUsersService edxUsersService) {
    super(sagaService, messagePublisher, EdxUserSchoolActivationRelinkSagaData.class, EDX_SCHOOL_USER_ACTIVATION_RELINK_SAGA.toString(), EDX_SCHOOL_USER_ACTIVATION_RELINK_TOPIC.toString(), edxSchoolUserActivationInviteOrchestratorService);
    this.edxSchoolUserActivationInviteOrchestratorService = edxSchoolUserActivationInviteOrchestratorService;
    this.edxUsersService = edxUsersService;
  }


  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(REMOVE_USER_SCHOOL_ACCESS, this::removeUserSchoolAccess)
      .step(REMOVE_USER_SCHOOL_ACCESS, EDX_USER_SCHOOL_REMOVED, CREATE_PERSONAL_ACTIVATION_CODE, this::createPersonalActivationCode)
      .step(CREATE_PERSONAL_ACTIVATION_CODE, PERSONAL_ACTIVATION_CODE_CREATED, SEND_EDX_USER_ACTIVATION_EMAIL, this::sendEdxUserActivationEmail)
      .end(SEND_EDX_USER_ACTIVATION_EMAIL, EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT);
  }

  /**
   * Remove the user's school access
   *
   * @param event
   * @param saga
   * @param edxUserActivationRelinkSagaData
   */
  protected void removeUserSchoolAccess(Event event, SagaEntity saga, EdxUserSchoolActivationRelinkSagaData edxUserActivationRelinkSagaData) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
    saga.setStatus(IN_PROGRESS.toString());
    saga.setSagaState(REMOVE_USER_SCHOOL_ACCESS.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    try {
      getEdxUsersService().deleteEdxSchoolUserById(UUID.fromString(edxUserActivationRelinkSagaData.getEdxUserId()), UUID.fromString(edxUserActivationRelinkSagaData.getEdxUserSchoolID()));
    }catch (EntityNotFoundException e){
      //Idempotent - this is ok
    }

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(REMOVE_USER_SCHOOL_ACCESS).eventOutcome(EDX_USER_SCHOOL_REMOVED)
      .eventPayload(JsonUtil.getJsonStringFromObject(edxUserActivationRelinkSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    log.info("message sent to EDX_API_TOPIC for REMOVE_USER_SCHOOL_ACCESS Event.");
  }


}
