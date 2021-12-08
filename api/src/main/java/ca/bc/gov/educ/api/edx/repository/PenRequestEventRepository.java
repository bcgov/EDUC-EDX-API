package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.PenRequestEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PenRequestEventRepository extends JpaRepository<PenRequestEvent, UUID> {
  Optional<PenRequestEvent> findBySagaIdAndEventType(UUID sagaId, String eventType);

}
