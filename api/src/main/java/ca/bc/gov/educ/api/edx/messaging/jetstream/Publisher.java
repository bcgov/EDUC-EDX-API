package ca.bc.gov.educ.api.edx.messaging.jetstream;

import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.model.v1.EdxEvent;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import io.nats.client.Connection;
import io.nats.client.JetStream;
import io.nats.client.JetStreamApiException;
import io.nats.client.api.StreamConfiguration;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_EVENT_TOPIC;


/**
 * The type Publisher.
 */
@Component("publisher")
@Slf4j
public class Publisher {
  private final JetStream jetStream;

  public static final String STREAM_NAME= "EDX_EVENTS";

  /**
   * Instantiates a new Publisher.
   *
   * @param natsConnection the nats connection
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  @Autowired
  public Publisher(final Connection natsConnection) throws IOException, JetStreamApiException {
    this.jetStream = natsConnection.jetStream();
    this.createOrUpdateInstituteEventStream(natsConnection);
  }

  /**
   * here only name and replicas and max messages are set, rest all are library default.
   *
   * @param natsConnection the nats connection
   * @throws IOException           the io exception
   * @throws JetStreamApiException the jet stream api exception
   */
  private void createOrUpdateInstituteEventStream(final Connection natsConnection) throws IOException, JetStreamApiException {
    val streamConfiguration = StreamConfiguration.builder().name(STREAM_NAME).replicas(1).maxMessages(10000).addSubjects(EDX_EVENT_TOPIC.toString()).build();
    try {
      natsConnection.jetStreamManagement().updateStream(streamConfiguration);
    } catch (final JetStreamApiException exception) {
      if (exception.getErrorCode() == 404) { // the stream does not exist , lets create it.
        natsConnection.jetStreamManagement().addStream(streamConfiguration);
      } else {
        log.info("exception", exception);
      }
    }

  }


  /**
   * Dispatch choreography event.
   *
   * @param event the event
   */
  public void dispatchChoreographyEvent(final Event event, SagaEntity saga) {
    if (event != null && event.getSagaId() != null) {
      val choreographedEvent = new ChoreographedEvent();
      choreographedEvent.setEventType(EventType.valueOf(event.getEventType().toString()));
      choreographedEvent.setEventOutcome(EventOutcome.valueOf(event.getEventOutcome().toString()));
      choreographedEvent.setEventPayload(event.getEventPayload());
      choreographedEvent.setEventID(event.getSagaId().toString());
      choreographedEvent.setCreateUser(saga.getCreateUser());
      choreographedEvent.setUpdateUser(saga.getUpdateUser());
      try {
        log.info("Broadcasting event :: {}", choreographedEvent);
        val pub = this.jetStream.publishAsync(EDX_EVENT_TOPIC.toString(), JsonUtil.getJsonSBytesFromObject(choreographedEvent));
        pub.thenAcceptAsync(result -> log.info("Event ID :: {} Published to JetStream :: {}", event.getSagaId(), result.getSeqno()));
      } catch (IOException e) {
        log.error("exception while broadcasting message to JetStream", e);
      }
    }
  }

  public void dispatchChoreographyEvent(final EdxEvent event) {
    if (event != null && event.getSagaId() != null) {
      val choreographedEvent = new ChoreographedEvent();
      choreographedEvent.setEventType(EventType.valueOf(event.getEventType().toString()));
      choreographedEvent.setEventOutcome(EventOutcome.valueOf(event.getEventOutcome().toString()));
      choreographedEvent.setEventPayload(event.getEventPayload());
      choreographedEvent.setEventID(event.getSagaId().toString());
      choreographedEvent.setCreateUser(event.getCreateUser());
      choreographedEvent.setUpdateUser(event.getUpdateUser());
      try {
        log.info("Broadcasting event :: {}", choreographedEvent);
        val pub = this.jetStream.publishAsync(EDX_EVENT_TOPIC.toString(), JsonUtil.getJsonSBytesFromObject(choreographedEvent));
        pub.thenAcceptAsync(result -> log.info("Event ID :: {} Published to JetStream :: {}", event.getSagaId(), result.getSeqno()));
      } catch (IOException e) {
        log.error("exception while broadcasting message to JetStream", e);
      }
    }
  }
}
