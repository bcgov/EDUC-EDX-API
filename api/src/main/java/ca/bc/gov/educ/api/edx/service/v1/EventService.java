package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.model.v1.EdxEvent;

/**
 * The interface Event service.
 *
 * @param <T> the type parameter
 */
public interface EventService<T> {

  /**
   * Process event.
   *
   * @param request the request
   * @param event   the event
   */
  void processEvent(T request, EdxEvent event);

  /**
   * Gets event type.
   *
   * @return the event type
   */
  String getEventType();
}
