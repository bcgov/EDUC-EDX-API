package ca.bc.gov.educ.api.edx.orchestrator;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.CREATED_SCHOOL_HAS_ADMIN_USER;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.INITIAL_USER_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.INITIAL_USER_INVITED;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.SCHOOL_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_INITIAL_USER;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_SCHOOL;
import static ca.bc.gov.educ.api.edx.constants.EventType.INVITE_INITIAL_USER;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.CREATE_NEW_SCHOOL_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_API_TOPIC;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static lombok.AccessLevel.PRIVATE;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.service.v1.EdxSchoolUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.CreateSchoolSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class CreateSchoolOrchestrator extends SchoolUserActivationBaseOrchestrator<CreateSchoolSagaData> {

  @Getter(PRIVATE)
  private final Publisher publisher;

  @Getter(PRIVATE)
  private final EdxUsersService edxUsersService;

  private final EdxUserMapper userMapper = EdxUserMapper.mapper;

  /**
     * Instantiates a new Base orchestrator.
     *
     * @param sagaService                   the saga service
     * @param messagePublisher              the message publisher
     * @param orchestratorService
     * @param publisher
     */
  protected CreateSchoolOrchestrator(
    SagaService sagaService,
    MessagePublisher messagePublisher,
    EdxSchoolUserActivationInviteOrchestratorService orchestratorService,
    Publisher publisher,
    EdxUsersService edxUsersService
  ) {
    super(
      sagaService,
      messagePublisher,
      CreateSchoolSagaData.class,
      CREATE_NEW_SCHOOL_SAGA.toString(),
      EDX_API_TOPIC.toString(),
      orchestratorService
    );
    this.publisher = publisher;
    this.edxUsersService = edxUsersService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_SCHOOL, this::createSchool)
      .step(CREATE_SCHOOL, CREATED_SCHOOL_HAS_ADMIN_USER, CREATE_INITIAL_USER, this::createInitialUser)
      .end(CREATE_SCHOOL, SCHOOL_CREATED, this::completeCreateSchoolSagaWithNoUser)
      .or()
      .step(CREATE_INITIAL_USER, INITIAL_USER_CREATED, INVITE_INITIAL_USER, this::inviteInitialUser)
      .end(INVITE_INITIAL_USER, INITIAL_USER_INVITED);
  }

  public void createSchool(
    Event event,
    SagaEntity saga,
    CreateSchoolSagaData createSchoolData
  ) throws JsonProcessingException {
    final SagaEventStatesEntity eventStates =
      this.createEventState(saga, event.getEventType(), event.getEventOutcome(), event.getEventPayload());

    final Event.EventBuilder eventBuilder = Event.builder()
      .eventType(CREATE_SCHOOL)
      .replyTo(this.getTopicToSubscribe());
    if (createSchoolData.getInitialEdxUser().isEmpty()) {
      eventBuilder.eventOutcome(SCHOOL_CREATED);
      eventBuilder.eventPayload("");
    } else {
      eventBuilder.eventOutcome(CREATED_SCHOOL_HAS_ADMIN_USER);
      eventBuilder.eventPayload(JsonUtil.getJsonStringFromObject(createSchoolData));
    }
    final Event nextEvent = eventBuilder.build();

    saga.setSagaState(CREATE_SCHOOL.toString());
    this.getSagaService().updateAttachedSagaWithEvents(saga, eventStates);

    this.postMessageToTopic(INSTITUTE_API_TOPIC.toString(), nextEvent);
    log.info("message sent to INSTITUTE_API_TOPIC for CREATE SCHOOL Event. :: {}", saga.getSagaId());
  }

  private void createInitialUser(
    Event event,
    SagaEntity saga,
    CreateSchoolSagaData createSchoolSagaData
  ) throws JsonProcessingException {
    School school = JsonUtil.getJsonObjectFromString(School.class, saga.getPayload());
    createSchoolSagaData.setSchoolId(school.getSchoolId());
    EdxUser initialUser = createSchoolSagaData.getInitialEdxUser().get();
    EdxUserEntity edxUser = edxUsersService.createEdxUser(userMapper.toModel(initialUser));
    createSchoolSagaData.setInitialEdxUser(Optional.of(userMapper.toStructure(edxUser)));

    final Event nextEvent = Event.builder().sagaId(saga.getSagaId())
      .eventType(CREATE_INITIAL_USER).eventOutcome(INITIAL_USER_CREATED)
      .eventPayload(JsonUtil.getJsonStringFromObject(createSchoolSagaData))
      .build();
    this.postMessageToTopic(this.getTopicToSubscribe(), nextEvent);
    publishToJetStream(nextEvent, saga);
    log.info("message sent to EDX_API_TOPIC for CREATE_INITIAL_USER Event. :: {}");
  }

  private void inviteInitialUser(Event event, SagaEntity saga, CreateSchoolSagaData createSchoolSagaData) {
    log.info("Finish writing this saga :: {}");
  }

  private void completeCreateSchoolSagaWithNoUser(
    final Event event,
    final SagaEntity saga,
    final CreateSchoolSagaData createSchoolSagaData
  ) {
    log.info("CreateSchoolSaga has ended without an initial admin user being created :: {}");
  }

  private void publishToJetStream(final Event event, SagaEntity saga) {
    publisher.dispatchChoreographyEvent(event, saga);
  }
}
