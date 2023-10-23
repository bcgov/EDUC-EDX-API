package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.BaseOrchestrator;
import ca.bc.gov.educ.api.edx.service.v1.MoveSchoolOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.MoveSchoolData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.MOVE_SCHOOL_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_API_TOPIC;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class MoveSchoolOrchestrator extends BaseOrchestrator<MoveSchoolData> {

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
        super(sagaService, messagePublisher, MoveSchoolData.class, MOVE_SCHOOL_SAGA.toString(), EDX_API_TOPIC.toString());
        this.moveSchoolOrchestratorService = moveSchoolOrchestratorService;
        this.publisher = publisher;
    }

    @Override
    public void populateStepsToExecuteMap() {
        this.stepBuilder()
          .begin(MOVE_SCHOOL, this::moveSchool)
          .step(MOVE_SCHOOL, SCHOOL_MOVED, COPY_USERS_TO_NEW_SCHOOL, this::copyUsersToNewSchool)
          .end(COPY_USERS_TO_NEW_SCHOOL, USERS_TO_NEW_SCHOOL_COPIED);
    }

    public void moveSchool(Event event, SagaEntity saga, MoveSchoolData moveSchoolData) throws JsonProcessingException {
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(MOVE_SCHOOL.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
            .eventType(MOVE_SCHOOL)
            .replyTo(this.getTopicToSubscribe())
            .eventPayload(JsonUtil.getJsonStringFromObject(moveSchoolData))
            .build();
        this.postMessageToTopic(INSTITUTE_API_TOPIC.toString(), nextEvent);
        log.info("message sent to INSTITUTE_API_TOPIC for MOVE SCHOOL Event. :: {}", saga.getSagaId());
    }

    private void copyUsersToNewSchool(Event event, SagaEntity saga, MoveSchoolData moveSchoolData) throws JsonProcessingException{
        final SagaEventStatesEntity eventStates = this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());
        saga.setSagaState(COPY_USERS_TO_NEW_SCHOOL.toString());
        this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

        ObjectMapper objectMapper = new ObjectMapper();
        MoveSchoolData moveSchoolDataFromEvent = objectMapper.readValue(event.getEventPayload(), MoveSchoolData.class);

        if(!getMoveSchoolOrchestratorService().hasCopiedUsersAlready(moveSchoolDataFromEvent)) {
            getMoveSchoolOrchestratorService().copyUsersToNewSchool(moveSchoolDataFromEvent);
        }

        final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
                .eventType(COPY_USERS_TO_NEW_SCHOOL).eventOutcome(USERS_TO_NEW_SCHOOL_COPIED)
                .eventPayload(JsonUtil.getJsonStringFromObject(moveSchoolDataFromEvent))
                .build();
        this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
        publishToJetStream(nextEvent, saga);
        log.info("message sent to EDX_API_TOPIC for MOVE_USERS_TO_NEW_SCHOOL Event.");
    }

    private void publishToJetStream(final Event event, SagaEntity saga) {
        publisher.dispatchChoreographyEvent(event, saga);
    }
}
