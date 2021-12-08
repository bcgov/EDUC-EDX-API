package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.PenRequestCommentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PenRequestCommentRepository extends JpaRepository<PenRequestCommentsEntity, UUID> {
  Optional<PenRequestCommentsEntity> findByCommentContentAndCommentTimestamp(String commentContent, LocalDateTime commentTimestamp);
}
