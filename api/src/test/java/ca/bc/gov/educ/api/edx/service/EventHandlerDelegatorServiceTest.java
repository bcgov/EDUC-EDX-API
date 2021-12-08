package ca.bc.gov.educ.api.edx.service;


import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestEntityMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.DocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.service.v1.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequest;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.DocumentTypeCodeBuilder;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

@Slf4j
public class EventHandlerDelegatorServiceTest extends BasePenRequestAPITest {
  @Autowired
  PenRequestRepository penRequestRepository;

  PenRequestEntityMapper mapper = PenRequestEntityMapper.mapper;
  @Autowired
  private PenRequestEventRepository penRequestEventRepository;
  @Autowired
  private PenRequestCommentRepository penRequestCommentRepository;

  @Autowired
  private DocumentRepository documentRepository;
  @Autowired
  MessagePublisher messagePublisher;
  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;
  @Autowired
  private DocumentRepository repository;

  @Autowired
  EventHandlerDelegatorService eventHandlerDelegatorService;
  @Captor
  ArgumentCaptor<byte[]> eventCaptor;

  UUID penRequestID;

  @Before
  public void setUp() {
    openMocks(this);
    final PenRequestEntity penRequestEntity = this.mapper.toModel(this.getPenRequestEntityFromJsonString());
    this.penRequestRepository.save(penRequestEntity);
    this.penRequestID = penRequestEntity.getPenRequestID();
    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);
    final DocumentEntity document = new DocumentBuilder()
      .withoutDocumentID()
      //.withoutCreateAndUpdateUser()
      .withPenRequest(penRequestEntity)
      .withTypeCode("CAPASSPORT")
      .build();
    this.repository.save(document);
  }

  @After
  public void tearDown() {
    this.documentRepository.deleteAll();
    this.penRequestCommentRepository.deleteAll();
    this.penRequestEventRepository.deleteAll();
    this.penRequestRepository.deleteAll();
    Mockito.clearInvocations(this.messagePublisher);
  }

  @Test
  public void handleEventUpdatePenRequest_givenDBOperationFailed_shouldNotSendResponseMessageToNATS() throws JsonProcessingException {
    final var penReq = this.getPenRequestEntityFromJsonString();
    penReq.setPenRequestID(this.penRequestID.toString());
    penReq.setLegalLastName(null);
    final Event event = Event.builder()
      .eventType(EventType.UPDATE_PEN_REQUEST)
      .eventPayload(JsonUtil.getJsonStringFromObject(penReq))
      .replyTo("PROFILE_REQUEST_SAGA_TOPIC")
      .sagaId(UUID.randomUUID())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event);
    verify(this.messagePublisher, never()).dispatchMessage(eq("PROFILE_REQUEST_SAGA_TOPIC"), this.eventCaptor.capture());
  }

  @Test
  public void handleEventUpdatePenRequest_givenDBOperationSuccess_shouldSendResponseMessageToNATS() throws JsonProcessingException {
    final var penReq = this.getPenRequestEntityFromJsonString();
    penReq.setPenRequestID(this.penRequestID.toString());
    final Event event = Event.builder()
      .eventType(EventType.UPDATE_PEN_REQUEST)
      .eventPayload(JsonUtil.getJsonStringFromObject(penReq))
      .replyTo("PROFILE_REQUEST_SAGA_TOPIC")
      .sagaId(UUID.randomUUID())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq("PROFILE_REQUEST_SAGA_TOPIC"), this.eventCaptor.capture());
    final var natsResponse = new String(this.eventCaptor.getValue());
    assertThat(natsResponse).contains(PEN_REQUEST_UPDATED.toString());
  }

  @Test
  public void handleEventUpdatePenRequest_givenReplayAndDBOperationSuccess_shouldSendResponseMessageToNATS() throws JsonProcessingException {
    final var penReq = this.getPenRequestEntityFromJsonString();
    penReq.setPenRequestID(this.penRequestID.toString());
    final Event event = Event.builder()
      .eventType(EventType.UPDATE_PEN_REQUEST)
      .eventPayload(JsonUtil.getJsonStringFromObject(penReq))
      .replyTo("PROFILE_REQUEST_SAGA_TOPIC")
      .sagaId(UUID.randomUUID())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event);
    this.eventHandlerDelegatorService.handleEvent(event);
    verify(this.messagePublisher, atLeast(2)).dispatchMessage(eq("PROFILE_REQUEST_SAGA_TOPIC"), this.eventCaptor.capture());
    final var natsResponse = new String(this.eventCaptor.getValue());
    assertThat(natsResponse).contains(PEN_REQUEST_UPDATED.toString());
  }


  @Test
  public void handleEventGetPenRequest_givenDBOperationFailed_shouldNotSendResponseMessageToNATS() {
    final Event event = Event.builder()
      .eventType(EventType.GET_PEN_REQUEST)
      .eventPayload("invalid pen request id")
      .replyTo("PROFILE_REQUEST_SAGA_TOPIC")
      .sagaId(UUID.randomUUID())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event);
    verify(this.messagePublisher, never()).dispatchMessage(eq("PROFILE_REQUEST_SAGA_TOPIC"), this.eventCaptor.capture());
  }

  @Test
  public void handleEventGetPenRequest_givenDBOperationSuccess_shouldSendResponseMessageToNATS() {
    final Event event = Event.builder()
      .eventType(EventType.GET_PEN_REQUEST)
      .eventPayload(this.penRequestID.toString())
      .replyTo("PROFILE_REQUEST_SAGA_TOPIC")
      .sagaId(UUID.randomUUID())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event);
    verify(this.messagePublisher, atLeastOnce()).dispatchMessage(eq("PROFILE_REQUEST_SAGA_TOPIC"), this.eventCaptor.capture());
    final var natsResponse = new String(this.eventCaptor.getValue());
    assertThat(natsResponse).contains(PEN_REQUEST_FOUND.toString());
  }

  @Test
  public void handleEventGetPenRequest_givenReplayAndDBOperationSuccess_shouldSendResponseMessageToNATS() {
    final var penReq = this.getPenRequestEntityFromJsonString();
    penReq.setPenRequestID(this.penRequestID.toString());
    final Event event = Event.builder()
      .eventType(EventType.GET_PEN_REQUEST)
      .eventPayload(this.penRequestID.toString())
      .replyTo("PROFILE_REQUEST_SAGA_TOPIC")
      .sagaId(UUID.randomUUID())
      .build();
    val newEvent = new Event();
    BeanUtils.copyProperties(event, newEvent);
    this.eventHandlerDelegatorService.handleEvent(event);
    this.eventHandlerDelegatorService.handleEvent(newEvent);
    verify(this.messagePublisher, atLeast(2)).dispatchMessage(eq("PROFILE_REQUEST_SAGA_TOPIC"), this.eventCaptor.capture());
    final var natsResponse = new String(this.eventCaptor.getValue());
    assertThat(natsResponse).contains(PEN_REQUEST_FOUND.toString());
  }

  @Test
  public void handleEventGetPenRequestDocMetadata_givenValidPenRequestIDAndDocInDB_shouldSendResponseMessageToNATS() {
    final var penReq = this.getPenRequestEntityFromJsonString();
    penReq.setPenRequestID(this.penRequestID.toString());
    final Event event = Event.builder()
      .eventType(EventType.GET_PEN_REQUEST_DOCUMENT_METADATA)
      .eventPayload(this.penRequestID.toString())
      .replyTo("PROFILE_REQUEST_COMPLETE_SAGA_TOPIC")
      .sagaId(UUID.randomUUID())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event);
    verify(this.messagePublisher, atLeast(1)).dispatchMessage(eq("PROFILE_REQUEST_COMPLETE_SAGA_TOPIC"), this.eventCaptor.capture());
    final var natsResponse = new String(this.eventCaptor.getValue());
    log.info(natsResponse);
    assertThat(natsResponse).contains(PEN_REQUEST_DOCUMENTS_FOUND.toString());
  }

  @Test
  public void handleEventGetPenRequestDocMetadata_givenInValidPenRequestIDAndDocNotInDB_shouldSendResponseMessageToNATS() {
    final var penReq = this.getPenRequestEntityFromJsonString();
    penReq.setPenRequestID(this.penRequestID.toString());
    final Event event = Event.builder()
      .eventType(EventType.GET_PEN_REQUEST_DOCUMENT_METADATA)
      .eventPayload(UUID.randomUUID().toString())
      .replyTo("PROFILE_REQUEST_COMPLETE_SAGA_TOPIC")
      .sagaId(UUID.randomUUID())
      .build();
    this.eventHandlerDelegatorService.handleEvent(event);
    verify(this.messagePublisher, atLeast(1)).dispatchMessage(eq("PROFILE_REQUEST_COMPLETE_SAGA_TOPIC"), this.eventCaptor.capture());
    final var natsResponse = new String(this.eventCaptor.getValue());
    log.info(natsResponse);
    assertThat(natsResponse).contains(PEN_REQUEST_DOCUMENTS_NOT_FOUND.toString());
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
