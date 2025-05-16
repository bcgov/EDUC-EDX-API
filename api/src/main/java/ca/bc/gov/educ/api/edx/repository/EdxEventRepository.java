package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EdxEventRepository extends JpaRepository<EdxEvent, UUID> {

  Optional<EdxEvent> findByEventId(UUID eventId);
  /**
   * Find by saga id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  Optional<EdxEvent> findBySagaId(UUID sagaId);

  /**
   * Find by saga id and event type optional.
   *
   * @param sagaId    the saga id
   * @param eventType the event type
   * @return the optional
   */
  Optional<EdxEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);


  List<EdxEvent> findByEventStatusAndEventTypeNotIn(String eventStatus, List<String> eventTypes);

  @Query(value = "select event.* from INSTITUTE_EVENT event where event.EVENT_STATUS = :eventStatus " +
          "AND event.CREATE_DATE < :createDate " +
          "AND event.EVENT_TYPE in :eventTypes " +
          "ORDER BY event.CREATE_DATE asc " +
          "FETCH FIRST :limit ROWS ONLY", nativeQuery=true)
  List<EdxEvent> findAllByEventStatusAndCreateDateBeforeAndEventTypeInOrderByCreateDate(String eventStatus, LocalDateTime createDate, int limit, List<String> eventTypes);
}
