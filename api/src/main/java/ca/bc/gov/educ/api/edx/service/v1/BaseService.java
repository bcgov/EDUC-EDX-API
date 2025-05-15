package ca.bc.gov.educ.api.edx.service.v1;


import ca.bc.gov.educ.api.edx.constants.EventStatus;
import ca.bc.gov.educ.api.edx.model.v1.EdxEvent;
import ca.bc.gov.educ.api.edx.repository.EdxEventRepository;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


/**
 * The type Base service.
 *
 * @param <T> the type parameter
 */
@Slf4j
public abstract class BaseService<T> implements EventService<T> {
    private final EdxEventRepository eventRepository;

    protected BaseService(EdxEventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    protected void updateEvent(final EdxEvent event) {
        this.eventRepository.findByEventId(event.getEventId()).ifPresent(existingEvent -> {
            existingEvent.setEventStatus(EventStatus.PROCESSED.toString());
            existingEvent.setUpdateDate(LocalDateTime.now());
            this.eventRepository.save(existingEvent);
        });
    }

}
