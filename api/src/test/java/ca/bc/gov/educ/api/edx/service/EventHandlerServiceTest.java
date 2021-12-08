package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestCommentsMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestCommentsEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEvent;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestCommentRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestEventRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.EventHandlerService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequest;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventStatus.MESSAGE_PUBLISHED;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.MockitoAnnotations.openMocks;

public class EventHandlerServiceTest extends BasePenRequestAPITest {

  public static final String STUDENT_PROFILE_SAGA_API_TOPIC = "STUDENT_PROFILE_SAGA_API_TOPIC";
  @Autowired
  private PenRequestRepository penRequestRepository;
  @Autowired
  private PenRequestEventRepository penRequestEventRepository;
  @Autowired
  private PenRequestCommentRepository penRequestCommentRepository;
  @Autowired
  private DocumentRepository documentRepository;

  @Autowired
  private EventHandlerService eventHandlerServiceUnderTest;
  private static final PenRequestEntityMapper mapper = PenRequestEntityMapper.mapper;
  private static final PenRequestCommentsMapper prcMapper = PenRequestCommentsMapper.mapper;

  @Before
  public void setUp() {
    openMocks(this);
  }

  @After
  public void tearDown() {
    this.documentRepository.deleteAll();
    this.penRequestCommentRepository.deleteAll();
    this.penRequestRepository.deleteAll();
    this.penRequestEventRepository.deleteAll();
  }


  @Test
  public void testHandleEvent_givenEventTypeGET__PENREQUEST__whenNoPenRequestExist_shouldHaveEventOutcomePEN__REQUEST__NOT__FOUND() throws IOException {
    final var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_PEN_REQUEST).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(UUID.randomUUID().toString()).build();
    val resBytes = this.eventHandlerServiceUnderTest.handleGetPenRequest(event);
    final var penReqEventUpdated = JsonUtil.getJsonObjectFromBytes(Event.class, resBytes);
    assertThat(penReqEventUpdated.getEventPayload()).isNotBlank();
    assertThat(penReqEventUpdated.getEventOutcome()).isEqualTo(PEN_REQUEST_NOT_FOUND);
  }

  @Test
  public void testHandleEvent_givenEventTypeGET__PENREQUEST__whenPenRequestExist_shouldHaveEventOutcomePEN__REQUEST__FOUND() throws IOException {
    final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    final var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(GET_PEN_REQUEST).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(entity.getPenRequestID().toString()).build();
    val resBytes = this.eventHandlerServiceUnderTest.handleGetPenRequest(event);
    final var penReqEventUpdated = JsonUtil.getJsonObjectFromBytes(Event.class, resBytes);
    assertThat(penReqEventUpdated.getEventPayload()).isNotBlank();
    assertThat(penReqEventUpdated.getEventPayload()).contains("penRequestStatusCode");
    assertThat(penReqEventUpdated.getEventOutcome()).isEqualTo(PEN_REQUEST_FOUND);
  }
  @Test
  public void testHandleEvent_givenEventTypeGET__PENREQUEST__whenPenRequestExistAndDuplicateSagaMessage_shouldHaveEventOutcomePEN__REQUEST__FOUND() throws JsonProcessingException {
    final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));

    final var sagaId = UUID.randomUUID();
    final var penRequestEvent = PenRequestEvent.builder().sagaId(sagaId).replyChannel(STUDENT_PROFILE_SAGA_API_TOPIC).eventType(GET_PEN_REQUEST.toString())
      .eventPayload(entity.getPenRequestID().toString()).eventOutcome(PEN_REQUEST_FOUND.toString()).eventStatus(MESSAGE_PUBLISHED.toString())
      .createDate(LocalDateTime.now()).createUser("TEST").build();
    this.penRequestEventRepository.save(penRequestEvent);

    final Event event = Event.builder().eventType(GET_PEN_REQUEST).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(entity.getPenRequestID().toString()).build();
    this.eventHandlerServiceUnderTest.handleGetPenRequest(event);
    final var penReqEventUpdated = this.penRequestEventRepository.findBySagaIdAndEventType(sagaId, GET_PEN_REQUEST.toString());
    assertThat(penReqEventUpdated).isPresent();
    assertThat(penReqEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(penReqEventUpdated.get().getEventOutcome()).isEqualTo(PEN_REQUEST_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE__PENREQUEST__whenPenRequestDoNotExist_shouldHaveEventOutcomePEN__REQUEST__NOT__FOUND() throws JsonProcessingException {
    final PenRequest entity = this.getPenRequestEntityFromJsonString();
    entity.setPenRequestID(UUID.randomUUID().toString());
    final var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(UPDATE_PEN_REQUEST).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(entity)).build();
    this.eventHandlerServiceUnderTest.handleUpdatePenRequest(event);
    final var penReqEventUpdated = this.penRequestEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_PEN_REQUEST.toString());
    assertThat(penReqEventUpdated).isPresent();
    assertThat(penReqEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(penReqEventUpdated.get().getEventOutcome()).isEqualTo(PEN_REQUEST_NOT_FOUND.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE__PENREQUEST__whenPenRequestExist_shouldHaveEventOutcomePEN__REQUEST__FOUND() throws JsonProcessingException {
    final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    entity.setCompleteComment("Manual");
    final var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(UPDATE_PEN_REQUEST).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(mapper.toStructure(entity))).build();
    this.eventHandlerServiceUnderTest.handleUpdatePenRequest(event);
    final var penReqEventUpdated = this.penRequestEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_PEN_REQUEST.toString());
    assertThat(penReqEventUpdated).isPresent();
    assertThat(penReqEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(penReqEventUpdated.get().getEventOutcome()).isEqualTo(PEN_REQUEST_UPDATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeUPDATE__PENREQUEST__whenPenRequestExistAndDuplicateSagaMessage_shouldHaveEventOutcomePEN__REQUEST__FOUND() throws JsonProcessingException {
    final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));

    final var sagaId = UUID.randomUUID();
    final var penRequestEvent = PenRequestEvent.builder().sagaId(sagaId).replyChannel(STUDENT_PROFILE_SAGA_API_TOPIC).eventType(UPDATE_PEN_REQUEST.toString())
      .eventPayload(entity.getPenRequestID().toString()).eventOutcome(PEN_REQUEST_UPDATED.toString()).eventStatus(MESSAGE_PUBLISHED.toString())
      .createDate(LocalDateTime.now()).createUser("TEST").build();
    this.penRequestEventRepository.save(penRequestEvent);

    final Event event = Event.builder().eventType(UPDATE_PEN_REQUEST).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(entity.getPenRequestID().toString()).build();
    this.eventHandlerServiceUnderTest.handleUpdatePenRequest(event);
    final var penReqEventUpdated = this.penRequestEventRepository.findBySagaIdAndEventType(sagaId, UPDATE_PEN_REQUEST.toString());
    assertThat(penReqEventUpdated).isPresent();
    assertThat(penReqEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(penReqEventUpdated.get().getEventOutcome()).isEqualTo(PEN_REQUEST_UPDATED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeADD__PENREQUEST__COMMENT_whenPenRequestExist_shouldHaveEventOutcomePEN__REQUEST__COMMENT__ADDED() throws JsonProcessingException {
    final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    final PenRequestCommentsEntity penRequestCommentsEntity = new PenRequestCommentsEntity();
    penRequestCommentsEntity.setPenRetrievalRequestID(entity.getPenRequestID());
    penRequestCommentsEntity.setCommentContent("Please provide other ID..");
    penRequestCommentsEntity.setCommentTimestamp(LocalDateTime.now());
    penRequestCommentsEntity.setCreateUser("API");
    penRequestCommentsEntity.setUpdateUser("API");
    final var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(ADD_PEN_REQUEST_COMMENT).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(prcMapper.toStructure(penRequestCommentsEntity))).build();
    this.eventHandlerServiceUnderTest.handleAddPenRequestComment(event);
    final var penReqEventUpdated = this.penRequestEventRepository.findBySagaIdAndEventType(sagaId, ADD_PEN_REQUEST_COMMENT.toString());
    assertThat(penReqEventUpdated).isPresent();
    assertThat(penReqEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(penReqEventUpdated.get().getEventOutcome()).isEqualTo(PEN_REQUEST_COMMENT_ADDED.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeADD__PENREQUEST__COMMENT_whenPenRequestCommentExist_shouldHaveEventOutcomePEN__REQUEST__COMMENT__ALREADY__EXIST() throws JsonProcessingException {
    final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    final PenRequestCommentsEntity penRequestCommentsEntity = new PenRequestCommentsEntity();
    penRequestCommentsEntity.setPenRetrievalRequestID(entity.getPenRequestID());
    penRequestCommentsEntity.setCommentContent("Please provide other ID..");
    penRequestCommentsEntity.setCommentTimestamp(LocalDateTime.now());
    penRequestCommentsEntity.setCreateUser("API");
    penRequestCommentsEntity.setUpdateUser("API");
    var sagaId = UUID.randomUUID();
    final Event event = Event.builder().eventType(ADD_PEN_REQUEST_COMMENT).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(JsonUtil.getJsonStringFromObject(prcMapper.toStructure(penRequestCommentsEntity))).build();
    this.eventHandlerServiceUnderTest.handleAddPenRequestComment(event);
    sagaId = UUID.randomUUID();
    event.setSagaId(sagaId);
    this.eventHandlerServiceUnderTest.handleAddPenRequestComment(event);
    final var penReqEventUpdated = this.penRequestEventRepository.findBySagaIdAndEventType(sagaId, ADD_PEN_REQUEST_COMMENT.toString());
    assertThat(penReqEventUpdated).isPresent();
    assertThat(penReqEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString());
    assertThat(penReqEventUpdated.get().getEventOutcome()).isEqualTo(PEN_REQUEST_COMMENT_ALREADY_EXIST.toString());
  }

  @Test
  public void testHandleEvent_givenEventTypeADD__PENREQUEST__COMMENT_whenDuplicateSagaMessage_shouldHaveEventOutcomePEN__REQUEST__COMMENT__ADDED() throws JsonProcessingException {
    final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    final PenRequestCommentsEntity penRequestCommentsEntity = new PenRequestCommentsEntity();
    penRequestCommentsEntity.setPenRetrievalRequestID(entity.getPenRequestID());
    penRequestCommentsEntity.setCommentContent("Please provide other ID..");
    penRequestCommentsEntity.setCommentTimestamp(LocalDateTime.now());
    penRequestCommentsEntity.setCreateUser("API");
    penRequestCommentsEntity.setUpdateUser("API");

    final var sagaId = UUID.randomUUID();
    final var payload = JsonUtil.getJsonStringFromObject(prcMapper.toStructure(penRequestCommentsEntity));

    final var penRequestEvent = PenRequestEvent.builder().sagaId(sagaId).replyChannel(STUDENT_PROFILE_SAGA_API_TOPIC).eventType(ADD_PEN_REQUEST_COMMENT.toString())
      .eventPayload(payload).eventOutcome(PEN_REQUEST_COMMENT_ADDED.toString()).eventStatus(MESSAGE_PUBLISHED.toString())
      .createDate(LocalDateTime.now()).createUser("TEST").build();
    this.penRequestEventRepository.save(penRequestEvent);

    final Event event = Event.builder().eventType(ADD_PEN_REQUEST_COMMENT).sagaId(sagaId).replyTo(STUDENT_PROFILE_SAGA_API_TOPIC).eventPayload(payload).build();
    this.eventHandlerServiceUnderTest.handleAddPenRequestComment(event);
    final var penReqEventUpdated = this.penRequestEventRepository.findBySagaIdAndEventType(sagaId, ADD_PEN_REQUEST_COMMENT.toString());
    assertThat(penReqEventUpdated).isPresent();
    assertThat(penReqEventUpdated.get().getEventStatus()).isEqualTo(MESSAGE_PUBLISHED.toString()); // the db status is updated from  MESSAGE_PUBLISHED
    assertThat(penReqEventUpdated.get().getEventOutcome()).isEqualTo(PEN_REQUEST_COMMENT_ADDED.toString());
  }



  private PenRequest getPenRequestEntityFromJsonString() {
    try {
      return new ObjectMapper().readValue(this.placeHolderPenReqJSON(), PenRequest.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected String placeHolderPenReqJSON() {
    return "{\"digitalID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\",\"pen\":\"127054021\"}";
  }
}
