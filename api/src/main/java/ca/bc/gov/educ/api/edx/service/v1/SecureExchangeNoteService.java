package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeNoteEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestNoteRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class SecureExchangeNoteService {

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeRequestNoteRepository secureExchangeRequestNoteRepository;

  @Autowired
  SecureExchangeNoteService(final SecureExchangeRequestRepository secureExchangeRequestRepository, final SecureExchangeRequestNoteRepository secureExchangeRequestNoteRepository) {
    this.secureExchangeRequestRepository = secureExchangeRequestRepository;
    this.secureExchangeRequestNoteRepository = secureExchangeRequestNoteRepository;
  }

  public Set<SecureExchangeNoteEntity> retrieveNotes(UUID secureExchangeRequestId) {
    final Optional<SecureExchangeEntity> entity = this.getSecureExchangeRequestRepository().findById(secureExchangeRequestId);
    if (entity.isPresent()) {
      return (entity.get().getSecureExchangeNotes() == null) ? new HashSet<>() : entity.get().getSecureExchangeNotes();
    }
    throw new EntityNotFoundException(SecureExchangeEntity.class, "SecureExchange", secureExchangeRequestId.toString());
  }
  
  public SecureExchangeNoteEntity save(UUID secureExchangeRequestId, SecureExchangeNoteEntity secureExchangeNote) {
    val result = this.getSecureExchangeRequestRepository().findById(secureExchangeRequestId);
    if (result.isPresent()) {
      SecureExchangeEntity secureExchangeEntity = result.get();
      secureExchangeNote.setSecureExchangeEntity(secureExchangeEntity);
      this.getSecureExchangeRequestNoteRepository().save(secureExchangeNote);
      return secureExchangeNote;
    }
    throw new EntityNotFoundException(SecureExchangeEntity.class, "SecureExchange", secureExchangeRequestId.toString());
  }

}
