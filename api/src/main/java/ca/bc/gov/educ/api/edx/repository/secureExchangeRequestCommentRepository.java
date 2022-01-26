package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface secureExchangeRequestCommentRepository extends JpaRepository<SecureExchangeCommentEntity, UUID> {
  Optional<SecureExchangeCommentEntity> findByCommentContentAndCommentTimestamp(String commentContent, LocalDateTime commentTimestamp);
}
