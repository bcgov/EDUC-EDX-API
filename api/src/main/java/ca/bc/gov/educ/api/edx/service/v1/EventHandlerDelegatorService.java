package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EventHandlerDelegatorService {
  public static final String RESPONDING_BACK_TO_NATS_ON_CHANNEL = "responding back to NATS on {} channel ";
  private final MessagePublisher messagePublisher;
  private final EventHandlerService eventHandlerService;
  public static final String PAYLOAD_LOG = "payload is :: {}";

  @Autowired
  public EventHandlerDelegatorService(MessagePublisher messagePublisher, EventHandlerService eventHandlerService) {
    this.messagePublisher = messagePublisher;
    this.eventHandlerService = eventHandlerService;
  }

  @Async("subscriberExecutor")
  public void handleEvent(Event event) {
    byte[] response;
    try {
      switch (event.getEventType()) {
        case UPDATE_PEN_REQUEST:
          log.info("received UPDATE_PEN_REQUEST event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleUpdatePenRequest(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, event.getReplyTo());
          messagePublisher.dispatchMessage(event.getReplyTo(), response);
          break;
        case GET_PEN_REQUEST:
          log.info("received GET_PEN_REQUEST event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleGetPenRequest(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, event.getReplyTo());
          messagePublisher.dispatchMessage(event.getReplyTo(), response);
          break;
        case GET_PEN_REQUEST_DOCUMENT_METADATA:
          log.info("received GET_PEN_REQUEST_DOCUMENT_METADATA event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleGetPenRequestDocumentsMetadata(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, event.getReplyTo());
          messagePublisher.dispatchMessage(event.getReplyTo(), response);
          break;
        case ADD_PEN_REQUEST_COMMENT:
          log.info("received ADD_PEN_REQUEST_COMMENT event :: {}", event.getSagaId());
          log.trace(PAYLOAD_LOG, event.getEventPayload());
          response = eventHandlerService.handleAddPenRequestComment(event);
          log.info(RESPONDING_BACK_TO_NATS_ON_CHANNEL, event.getReplyTo());
          messagePublisher.dispatchMessage(event.getReplyTo(), response);
          break;
        default:
          log.info("silently ignoring other events :: {}", event);
          break;
      }
    } catch (final Exception e) {
      log.error("Exception", e);
    }
  }
}
