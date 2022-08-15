package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeNoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecureExchangeRequestNoteRepository extends JpaRepository<SecureExchangeNoteEntity, UUID> {
  Optional<SecureExchangeNoteEntity> findByContent(String content);
  List<SecureExchangeNoteEntity> findSecureExchangeNoteEntitiesBySecureExchangeEntitySecureExchangeID(UUID secureExchangeID);
}
