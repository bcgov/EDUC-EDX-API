package ca.bc.gov.educ.api.edx.service.v1.event;

import ca.bc.gov.educ.api.edx.repository.EdxEventRepository;
import ca.bc.gov.educ.api.edx.struct.v1.ChoreographedEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventStatus.MESSAGE_PUBLISHED;


/**
 * This class will process events from Jet Stream, which is used in choreography pattern, where messages are published if a student is created or updated.
 */
@Service
@Slf4j
public class JetStreamEventHandlerService {

  private final EdxEventRepository eventRepository;


  /**
   * Instantiates a new Stan event handler service.
   *
   * @param eventRepository the institute event repository
   */
  @Autowired
  public JetStreamEventHandlerService(EdxEventRepository eventRepository) {
    this.eventRepository = eventRepository;
  }

  /**
   * Update event status.
   *
   * @param choreographedEvent the choreographed event
   */
  @Transactional
  public void updateEventStatus(ChoreographedEvent choreographedEvent) {
    if (choreographedEvent != null && choreographedEvent.getEventID() != null) {
      var eventID = UUID.fromString(choreographedEvent.getEventID());
      var eventOptional = eventRepository.findById(eventID);
      if (eventOptional.isPresent()) {
        var studentEvent = eventOptional.get();
        studentEvent.setEventStatus(MESSAGE_PUBLISHED.toString());
        eventRepository.save(studentEvent);
      }
    }
  }
}
