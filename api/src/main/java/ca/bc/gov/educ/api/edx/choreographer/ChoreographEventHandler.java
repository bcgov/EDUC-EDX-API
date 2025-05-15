package ca.bc.gov.educ.api.edx.choreographer;

import ca.bc.gov.educ.api.edx.constants.EventStatus;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.model.v1.EdxEvent;
import ca.bc.gov.educ.api.edx.repository.EdxEventRepository;
import ca.bc.gov.educ.api.edx.service.v1.EventService;
import ca.bc.gov.educ.api.edx.struct.gradschool.v1.GradSchool;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.jboss.threads.EnhancedQueueExecutor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;


/**
 * The type Choreograph event handler.
 */
@Component
@Slf4j
public class ChoreographEventHandler {
  private final Executor singleTaskExecutor = new EnhancedQueueExecutor.Builder()
    .setThreadFactory(new ThreadFactoryBuilder().setNameFormat("task-executor-%d").build())
    .setCorePoolSize(2).setMaximumPoolSize(2).build();
  private final Map<String, EventService<?>> eventServiceMap;
  private final EdxEventRepository eventRepository;

  /**
   * Instantiates a new Choreograph event handler.
   *
   * @param eventServices   the event services
   * @param eventRepository the event repository
   */
  public ChoreographEventHandler(final List<EventService<?>> eventServices, final EdxEventRepository eventRepository) {
    this.eventRepository = eventRepository;
    this.eventServiceMap = new HashMap<>();
    eventServices.forEach(eventService -> this.eventServiceMap.put(eventService.getEventType(), eventService));
  }

  /**
   * Handle event.
   *
   * @param event the event
   */
  public void handleEvent(@NonNull final EdxEvent event) {
    this.singleTaskExecutor.execute(() -> {
      val eventFromDBOptional = this.eventRepository.findById(event.getEventId());
      if (eventFromDBOptional.isPresent()) {
        val eventFromDB = eventFromDBOptional.get();
        if (eventFromDB.getEventStatus().equals(EventStatus.DB_COMMITTED.toString())) {
          log.info("Processing event with event ID :: {}", event.getEventId());
          try {
            switch (event.getEventType()) {
              case "UPDATE_GRAD_SCHOOL":
                log.info("Processing UPDATE_GRAD_SCHOOL event record :: {} ", event);
                val gradSchool = JsonUtil.getJsonObjectFromString(GradSchool.class, event.getEventPayload());
                final EventService<GradSchool> gradSchoolEventService = (EventService<GradSchool>) this.eventServiceMap.get(EventType.UPDATE_GRAD_SCHOOL.toString());
                gradSchoolEventService.processEvent(gradSchool, event);
                break;
              default:
                log.warn("Silently ignoring event: {}", event);
                this.eventRepository.findByEventId(event.getEventId()).ifPresent(existingEvent -> {
                  existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
                  existingEvent.setUpdateDate(LocalDateTime.now());
                  this.eventRepository.save(existingEvent);
                });
                break;
            }
            log.info("Event was processed, ID :: {}", event.getEventId());
          } catch (final Exception exception) {
            log.error("Exception while processing event :: {}", event, exception);
          }
        }
      }

    });


  }
}
