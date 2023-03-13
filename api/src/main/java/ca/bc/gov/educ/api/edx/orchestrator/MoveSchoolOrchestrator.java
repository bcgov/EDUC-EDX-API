package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.MoveSchoolOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.MoveSchoolSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.MOVE_SCHOOL_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class MoveSchoolOrchestrator extends BaseOrchestrator<MoveSchoolSagaData> {

    @Getter(PRIVATE)
    private final MoveSchoolOrchestratorService moveSchoolOrchestratorService;

    private final Publisher publisher;
    /**
     * Instantiates a new Base orchestrator.
     *
     * @param sagaService                   the saga service
     * @param messagePublisher              the message publisher
     * @param moveSchoolOrchestratorService
     * @param publisher
     */
    protected MoveSchoolOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, MoveSchoolOrchestratorService moveSchoolOrchestratorService, Publisher publisher) {
        super(sagaService, messagePublisher, MoveSchoolSagaData.class, MOVE_SCHOOL_SAGA.toString(), EDX_API_TOPIC.toString());
        this.moveSchoolOrchestratorService = moveSchoolOrchestratorService;
        this.publisher = publisher;
    }

    @Override
    public void populateStepsToExecuteMap() {
        this.stepBuilder()
          .begin(CREATE_SCHOOL, this::checkIfSchoolNumberIsAvailableInDistrict)
          .step(CREATE_SCHOOL, SCHOOL_CREATED, UPDATE_SCHOOL, this::updateExistingSchool)
          .step(UPDATE_SCHOOL, SCHOOL_UPDATED, MOVE_USERS_TO_NEW_SCHOOL, this::moveUsersToNewSchool)
          .end(MOVE_USERS_TO_NEW_SCHOOL, SCHOOL_MOVED);
    }

    private void checkIfSchoolNumberIsAvailableInDistrict(Event event, SagaEntity saga, MoveSchoolSagaData moveSchoolSagaData) throws JsonProcessingException{
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(CREATE_SCHOOL.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
        boolean exists = getMoveSchoolOrchestratorService().findSchoolNumberInDistrict(moveSchoolSagaData.getSchool().getSchoolNumber(), moveSchoolSagaData.getSchool().getDistrictId());
        getMoveSchoolOrchestratorService().createNewSchool(moveSchoolSagaData, saga, exists);

        final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
                .eventType(CREATE_SCHOOL).eventOutcome(SCHOOL_CREATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(moveSchoolSagaData))
                .build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        log.info("message sent to EDX_API_TOPIC for CREATE_SCHOOL Event.");
    }

    private void updateExistingSchool(Event event, SagaEntity saga, MoveSchoolSagaData moveSchoolSagaData) throws JsonProcessingException{
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(UPDATE_SCHOOL.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);
        getMoveSchoolOrchestratorService().updateSchool(moveSchoolSagaData, saga);
        final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
                .eventType(UPDATE_SCHOOL).eventOutcome(SCHOOL_UPDATED)
                .eventPayload(JsonUtil.getJsonStringFromObject(moveSchoolSagaData))
                .build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        log.info("message sent to EDX_API_TOPIC for UPDATE_EXISTING_SCHOOL Event.");
    }

    private void moveUsersToNewSchool(Event event, SagaEntity saga, MoveSchoolSagaData moveSchoolSagaData) throws JsonProcessingException{
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(MOVE_USERS_TO_NEW_SCHOOL.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        getMoveSchoolOrchestratorService().moveUsersToNewSchool(moveSchoolSagaData, saga);

        final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
                .eventType(MOVE_USERS_TO_NEW_SCHOOL).eventOutcome(SCHOOL_MOVED)
                .eventPayload(JsonUtil.getJsonStringFromObject(moveSchoolSagaData))
                .build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        publishToJetStream(nextEvent, saga);
        log.info("message sent to EDX_API_TOPIC for MOVE_USERS_TO_NEW_SCHOOL Event.");
    }

    private void publishToJetStream(final Event event, SagaEntity saga) {
        publisher.dispatchChoreographyEvent(event, saga);
    }
}
