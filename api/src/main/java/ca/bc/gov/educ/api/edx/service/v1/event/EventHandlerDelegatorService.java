package ca.bc.gov.educ.api.edx.service.v1.event;

import ca.bc.gov.educ.api.edx.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.edx.exception.BusinessException;
import ca.bc.gov.educ.api.edx.service.v1.ChoreographedEventPersistenceService;
import ca.bc.gov.educ.api.edx.struct.v1.ChoreographedEvent;
import io.nats.client.Message;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * The type Event handler service.
 */
@Service
@Slf4j
@SuppressWarnings({"java:S3864", "java:S3776"})
public class EventHandlerDelegatorService {


    /**
     * The constant RESPONDING_BACK_TO_NATS_ON_CHANNEL.
     */
    private final ChoreographedEventPersistenceService choreographedEventPersistenceService;
    private final ChoreographEventHandler choreographer;

    /**
     * Instantiates a new Event handler delegator service.
     *
     */
    @Autowired
    public EventHandlerDelegatorService(ChoreographedEventPersistenceService choreographedEventPersistenceService, ChoreographEventHandler choreographer) {
        this.choreographedEventPersistenceService = choreographedEventPersistenceService;
        this.choreographer = choreographer;
    }

    public void handleChoreographyEvent(@NonNull final ChoreographedEvent choreographedEvent, final Message message) throws IOException {
        try {
            final var persistedEvent = this.choreographedEventPersistenceService.persistEventToDB(choreographedEvent);
            message.ack(); // acknowledge to Jet Stream that api got the message and it is now in DB.
            log.info("acknowledged to Jet Stream...");
            this.choreographer.handleEvent(persistedEvent);
        } catch (final BusinessException businessException) {
            message.ack(); // acknowledge to Jet Stream that api got the message already...
            log.info("acknowledged to Jet Stream...");
        }
    }

}
