package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecureExchangeRequestCommentRepository extends JpaRepository<SecureExchangeCommentEntity, UUID> {
  Optional<SecureExchangeCommentEntity> findByContent(String content);
  List<SecureExchangeCommentEntity> findSecureExchangeCommentEntitiesBySecureExchangeEntitySecureExchangeID(UUID secureExchangeID);
}
