package ca.bc.gov.educ.api.edx.messaging;

import ca.bc.gov.educ.api.edx.orchestrator.base.EventHandler;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.Message;
import io.nats.client.MessageHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Message subscriber.
 */
@Component
@Slf4j
public class MessageSubscriber {

  /**
   * The Event Handlers as orchestrator for SAGA
   */
  @Getter(PRIVATE)
  private final Map<String, EventHandler> handlerMap = new HashMap<>();

  /**
   * The Connection.
   */
  private final Connection connection;

  /**
   * Instantiates a new Message subscriber.
   *
   * @param con           the con
   * @param eventHandlers the event handlers
   */
  @Autowired
  public MessageSubscriber(final Connection con, final List<EventHandler> eventHandlers) {
    this.connection = con;
    eventHandlers.forEach(handler -> {
      this.handlerMap.put(handler.getTopicToSubscribe(), handler);
      this.subscribe(handler.getTopicToSubscribe(), handler);
    });
  }

  /**
   * On message, event handler for SAGA
   *
   * @param eventHandler the orchestrator
   * @return the message handler
   */
  private static MessageHandler onMessage(final EventHandler eventHandler) {
    return (Message message) -> {
      if (message != null) {
        log.info("Message received subject :: {},  replyTo :: {}, subscriptionID :: {}", message.getSubject(), message.getReplyTo(), message.getSID());
        try {
          final var eventString = new String(message.getData());
          final var event = JsonUtil.getJsonObjectFromString(Event.class, eventString);
          eventHandler.handleEvent(event);
        } catch (final InterruptedException e) {
          Thread.currentThread().interrupt();
          log.error("Exception ", e);
        } catch (final Exception e) {
          log.error("Exception ", e);
        }
      }
    };
  }

  /**
   * Subscribe the topic on messages for SAGA
   *
   * @param topic        the topic name
   * @param eventHandler the orchestrator
   */
  private void subscribe(final String topic, final EventHandler eventHandler) {
    this.handlerMap.computeIfAbsent(topic, k -> eventHandler);
    final String queue = topic.replace("_", "-");
    final var dispatcher = this.connection.createDispatcher(MessageSubscriber.onMessage(eventHandler));
    dispatcher.subscribe(topic, queue);
  }

}
