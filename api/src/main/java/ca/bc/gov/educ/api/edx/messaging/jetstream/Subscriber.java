package ca.bc.gov.educ.api.edx.messaging.jetstream;

import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.helpers.LogHelper;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.service.v1.event.EventHandlerDelegatorService;
import ca.bc.gov.educ.api.edx.service.v1.event.JetStreamEventHandlerService;
import ca.bc.gov.educ.api.edx.struct.v1.ChoreographedEvent;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.nats.client.Connection;
import io.nats.client.JetStreamApiException;
import io.nats.client.Message;
import io.nats.client.PushSubscribeOptions;
import io.nats.client.api.ConsumerConfiguration;
import io.nats.client.api.DeliverPolicy;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * The type Subscriber.
 */
@Component
@DependsOn("publisher")
@Slf4j
public class Subscriber {
    private final Executor subscriberExecutor = new EnhancedQueueExecutor.Builder()
            .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("jet-stream-subscriber-%d").build())
            .setCorePoolSize(2).setMaximumPoolSize(2).setKeepAliveTime(Duration.ofMillis(1000)).build();
    private final Connection natsConnection;
    private final EventHandlerDelegatorService eventHandlerDelegatorService;
    private final Map<String, List<String>> streamTopicsMap = new HashMap<>();
    private final JetStreamEventHandlerService jetStreamEventHandlerService;


    @Autowired
    public Subscriber(final Connection natsConnection, EventHandlerDelegatorService eventHandlerDelegatorService, JetStreamEventHandlerService jetStreamEventHandlerService) {
        this.natsConnection = natsConnection;
        this.eventHandlerDelegatorService = eventHandlerDelegatorService;
        this.jetStreamEventHandlerService = jetStreamEventHandlerService;
        initializeStreamTopicMap();
    }

    private void initializeStreamTopicMap() {
        final List<String> gradSchoolEventsTopics = new ArrayList<>();
        gradSchoolEventsTopics.add("GRAD_SCHOOL_EVENTS_TOPIC");
        final List<String> instituteEventsTopics = new ArrayList<>();
        instituteEventsTopics.add("EDX_EVENT_TOPIC");
        this.streamTopicsMap.put("GRAD_SCHOOL_EVENTS", gradSchoolEventsTopics);
        this.streamTopicsMap.put("EDX_EVENT", instituteEventsTopics);
    }


    /**
     * Subscribe.
     *
     * @throws IOException           the io exception
     * @throws JetStreamApiException the jet stream api exception
     */
    @PostConstruct
    public void subscribe() throws IOException, JetStreamApiException {
        val qName = ApplicationProperties.CLIENT_ID.concat("-QUEUE");
        val autoAck = false;
        for (val entry : this.streamTopicsMap.entrySet()) {
            for (val topic : entry.getValue()) {
                final PushSubscribeOptions options = PushSubscribeOptions.builder().stream(entry.getKey())
                        .durable(ApplicationProperties.CLIENT_ID.concat("-DURABLE"))
                        .configuration(ConsumerConfiguration.builder().deliverPolicy(DeliverPolicy.New).build()).build();
                this.natsConnection.jetStream().subscribe(topic, qName, this.natsConnection.createDispatcher(), this::onMessage,
                        autoAck, options);
            }
        }
    }

    public void onMessage(final Message message) {
        if (message != null) {
            log.info("Received message Subject:: {} , SID :: {} , sequence :: {}, pending :: {} ", message.getSubject(), message.getSID(), message.metaData().consumerSequence(), message.metaData().pendingCount());
            try {
                val eventString = new String(message.getData());
                LogHelper.logMessagingEventDetails(eventString);
                final ChoreographedEvent event = JsonUtil.getJsonObjectFromString(ChoreographedEvent.class, eventString);
                if (event.getEventPayload() == null) {
                    message.ack();
                    log.warn("payload is null, ignoring event :: {}", event);
                    return;
                }
                this.subscriberExecutor.execute(() -> {
                    try {
                        if(event.getEventType().equals(EventType.UPDATE_GRAD_SCHOOL)) {
                            this.eventHandlerDelegatorService.handleChoreographyEvent(event, message);
                        }else{
                            jetStreamEventHandlerService.updateEventStatus(event);
                            message.ack();
                            log.info("Received event :: {} ", event);
                        }
                    } catch (final IOException e) {
                        log.error("IOException ", e);
                    }
                });
                log.info("received event :: {} ", event);
            } catch (final Exception ex) {
                log.error("Exception ", ex);
            }
        }
    }
}
