package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.choreographer.ChoreographEventHandler;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.messaging.jetstream.Publisher;
import ca.bc.gov.educ.api.edx.repository.EdxEventRepository;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;

import static ca.bc.gov.educ.api.edx.constants.EventStatus.DB_COMMITTED;

/**
 * This class is responsible to check the STUDENT_EVENT table periodically and publish messages to JET STREAM, if some them are not yet published
 * this is a very edge case scenario which will occur.
 */
@Component
@Slf4j
public class JetStreamEventScheduler {

  private final EdxEventRepository eventRepository;
  private final Publisher publisher;
  private final ChoreographEventHandler choreographer;

  /**
   * Instantiates a new Stan event scheduler.
   *
   * @param eventRepository the student event repository
   * @param publisher              the publisher
   */
  public JetStreamEventScheduler(EdxEventRepository eventRepository, Publisher publisher, ChoreographEventHandler choreographer) {
    this.eventRepository = eventRepository;
    this.publisher = publisher;
    this.choreographer = choreographer;
  }

  /**
   * Find and publish student events to stan.
   */
  @Scheduled(cron = "${cron.scheduled.process.events.stan}") // every 5 minutes
  @SchedulerLock(name = "PROCESS_CHOREOGRAPHED_EVENTS_FROM_JET_STREAM", lockAtLeastFor = "${cron.scheduled.process.events.stan.lockAtLeastFor}", lockAtMostFor = "${cron.scheduled.process.events.stan.lockAtMostFor}")
  public void findAndPublishStudentEventsToJetStream() {
    var gradSchoolEventTypes = Arrays.asList(EventType.UPDATE_GRAD_SCHOOL.toString());
    LockAssert.assertLocked();
    var results = eventRepository.findByEventStatusAndEventTypeNotIn(DB_COMMITTED.toString(), gradSchoolEventTypes);
    if (!results.isEmpty()) {
      results.forEach(el -> {
        if (el.getUpdateDate().isBefore(LocalDateTime.now().minusMinutes(5))) {
          try {
            publisher.dispatchChoreographyEvent(el);
          } catch (final Exception ex) {
            log.error("Exception while trying to publish message", ex);
          }
        }
      });
    }

    final var resultsForIncoming = this.eventRepository.findAllByEventStatusAndCreateDateBeforeAndEventTypeInOrderByCreateDate(DB_COMMITTED.toString(), LocalDateTime.now().minusMinutes(1), 500, gradSchoolEventTypes);
    if (!resultsForIncoming.isEmpty()) {
      log.info("Found {} grad school choreographed events which needs to be processed.", resultsForIncoming.size());
      resultsForIncoming.forEach(this.choreographer::handleEvent);
    }
  }
}
