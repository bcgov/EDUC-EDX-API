package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.mappers.v1.DocumentMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestCommentsMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestCommentsEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEvent;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestCommentRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestEventRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequest;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestComments;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import static ca.bc.gov.educ.api.edx.constants.EventStatus.MESSAGE_PUBLISHED;
import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
public class EventHandlerService {
  public static final String NO_RECORD_SAGA_ID_EVENT_TYPE = "no record found for the saga id and event type combination, processing";
  public static final String RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE = "record found for the saga id and event type combination, might be a duplicate or replay," +
          " just updating the db status so that it will be polled and sent back again.";
  public static final String EVENT_PAYLOAD = "event is :: {}";
  @Getter(PRIVATE)
  private final PenRequestRepository penRequestRepository;
  private static final PenRequestEntityMapper mapper = PenRequestEntityMapper.mapper;
  private static final PenRequestCommentsMapper prcMapper = PenRequestCommentsMapper.mapper;
  @Getter(PRIVATE)
  private final PenRequestEventRepository penRequestEventRepository;
  @Getter(PRIVATE)
  private final PenRequestCommentRepository penRequestCommentRepository;
  @Getter(PRIVATE)
  private final DocumentRepository documentRepository;
  @Getter(PRIVATE)
  private final PenRequestService penRequestService;
  @Autowired
  public EventHandlerService(final PenRequestRepository penRequestRepository, final PenRequestEventRepository penRequestEventRepository, PenRequestCommentRepository penRequestCommentRepository, DocumentRepository documentRepository, PenRequestService penRequestService) {
    this.penRequestRepository = penRequestRepository;
    this.penRequestEventRepository = penRequestEventRepository;
    this.penRequestCommentRepository = penRequestCommentRepository;
    this.documentRepository = documentRepository;
    this.penRequestService = penRequestService;
  }


  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public byte[] handleAddPenRequestComment(Event event) throws JsonProcessingException {
    val penRequestEventOptional = getPenRequestEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    PenRequestEvent penRequestEvent;
    if (penRequestEventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      PenRequestCommentsEntity entity = prcMapper.toModel(JsonUtil.getJsonObjectFromString(PenRequestComments.class, event.getEventPayload()));
      val penReqComment = getPenRequestCommentRepository().findByCommentContentAndCommentTimestamp(entity.getCommentContent(), entity.getCommentTimestamp());
      if (penReqComment.isPresent()) {
        event.setEventOutcome(EventOutcome.PEN_REQUEST_COMMENT_ALREADY_EXIST);
        event.setEventPayload(JsonUtil.getJsonStringFromObject(prcMapper.toStructure(penReqComment.get())));
      } else {
        val result = getPenRequestRepository().findById(entity.getPenRetrievalRequestID());
        if (result.isPresent()) {
          entity.setPenRequestEntity(result.get());
          entity.setCreateDate(LocalDateTime.now());
          entity.setUpdateDate(LocalDateTime.now());
          getPenRequestCommentRepository().save(entity);
          event.setEventOutcome(EventOutcome.PEN_REQUEST_COMMENT_ADDED);
          event.setEventPayload(JsonUtil.getJsonStringFromObject(prcMapper.toStructure(entity)));
        }
      }
      penRequestEvent = createPenRequestEvent(event);
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      penRequestEvent = penRequestEventOptional.get();
      penRequestEvent.setUpdateDate(LocalDateTime.now());
    }
    getPenRequestEventRepository().save(penRequestEvent);
    return createResponseEvent(penRequestEvent);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public byte[] handleGetPenRequest(Event event) throws JsonProcessingException {
    log.trace(EVENT_PAYLOAD, event);
    val optionalPenRequestEntity = getPenRequestRepository().findById(UUID.fromString(event.getEventPayload())); // expect the payload contains the pen request id.
    if (optionalPenRequestEntity.isPresent()) {
      val attachedEntity = optionalPenRequestEntity.get();
      event.setEventPayload(JsonUtil.getJsonStringFromObject(mapper.toStructure(attachedEntity)));// need to convert to structure MANDATORY otherwise jackson will break.
      event.setEventOutcome(EventOutcome.PEN_REQUEST_FOUND);
    } else {
      event.setEventOutcome(EventOutcome.PEN_REQUEST_NOT_FOUND);
    }
    return createResponseEvent(createPenRequestEvent(event));
  }

  @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
  public byte[] handleGetPenRequestDocumentsMetadata(final Event event) throws JsonProcessingException {
      val documentList = this.getDocumentRepository().findByPenRequestPenRequestID(UUID.fromString(event.getEventPayload())); // expect the payload contains the pen request id, which is a valid guid..
      if (documentList.isEmpty()) {
        event.setEventPayload("[]");
        event.setEventOutcome(EventOutcome.PEN_REQUEST_DOCUMENTS_NOT_FOUND);
      } else {
        event.setEventPayload(JsonUtil.getJsonStringFromObject(documentList.stream().map(DocumentMapper.mapper::toMetadataStructure).collect(Collectors.toList())));// need to convert to structure MANDATORY otherwise jackson will break.
        event.setEventOutcome(EventOutcome.PEN_REQUEST_DOCUMENTS_FOUND);
      }
    return createResponseEvent(createPenRequestEvent(event));
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public byte[] handleUpdatePenRequest(Event event) throws JsonProcessingException {
    val penRequestEventOptional = getPenRequestEventRepository().findBySagaIdAndEventType(event.getSagaId(), event.getEventType().toString());
    PenRequestEvent penRequestEvent;
    if (penRequestEventOptional.isEmpty()) {
      log.info(NO_RECORD_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      PenRequestEntity entity = mapper.toModel(JsonUtil.getJsonObjectFromString(PenRequest.class, event.getEventPayload()));
      entity.setUpdateDate(LocalDateTime.now());
      try{
        val updatedEntity = getPenRequestService().updatePenRequest(entity);
        event.setEventPayload(JsonUtil.getJsonStringFromObject(mapper.toStructure(updatedEntity)));// need to convert to structure MANDATORY otherwise jackson will break.
        event.setEventOutcome(EventOutcome.PEN_REQUEST_UPDATED);
      }catch (final EntityNotFoundException entityNotFoundException){
        event.setEventOutcome(EventOutcome.PEN_REQUEST_NOT_FOUND);
      }
      penRequestEvent = createPenRequestEvent(event);
    } else {
      log.info(RECORD_FOUND_FOR_SAGA_ID_EVENT_TYPE);
      log.trace(EVENT_PAYLOAD, event);
      penRequestEvent = penRequestEventOptional.get();
      penRequestEvent.setUpdateDate(LocalDateTime.now());
    }
    getPenRequestEventRepository().save(penRequestEvent);
    return createResponseEvent(penRequestEvent);
  }

  private byte[] createResponseEvent(PenRequestEvent penRequestEvent) throws JsonProcessingException {
    val responseEvent = Event.builder()
      .sagaId(penRequestEvent.getSagaId())
      .eventType(EventType.valueOf(penRequestEvent.getEventType()))
      .eventOutcome(EventOutcome.valueOf(penRequestEvent.getEventOutcome()))
      .eventPayload(penRequestEvent.getEventPayload()).build();
    return JsonUtil.getJsonSBytesFromObject(responseEvent);
  }



  private PenRequestEvent createPenRequestEvent(Event event) {
    return PenRequestEvent.builder()
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser(event.getEventType().toString()) //need to discuss what to put here.
            .updateUser(event.getEventType().toString())
            .eventPayload(event.getEventPayload())
            .eventType(event.getEventType().toString())
            .sagaId(event.getSagaId())
            .eventStatus(MESSAGE_PUBLISHED.toString())
            .eventOutcome(event.getEventOutcome().toString())
            .replyChannel(event.getReplyTo())
            .build();
  }
}
