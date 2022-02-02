package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestCommentRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class SecureExchangeCommentService {

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeRequestCommentRepository secureExchangeRequestCommentRepository;

  @Autowired
  SecureExchangeCommentService(final SecureExchangeRequestRepository secureExchangeRequestRepository, final SecureExchangeRequestCommentRepository secureExchangeRequestCommentRepository) {
    this.secureExchangeRequestRepository = secureExchangeRequestRepository;
    this.secureExchangeRequestCommentRepository = secureExchangeRequestCommentRepository;
  }

  public Set<SecureExchangeCommentEntity> retrieveComments(UUID secureExchangeRequestId) {
    final Optional<SecureExchangeEntity> entity = this.getSecureExchangeRequestRepository().findById(secureExchangeRequestId);
    if (entity.isPresent()) {
      return entity.get().getSecureExchangeComment();
    }
    throw new EntityNotFoundException(SecureExchangeEntity.class, "SecureExchange", secureExchangeRequestId.toString());
  }

  /**
   * Need to find the entity first as it is the parent entity and system is trying to persist the child entity so need to attach it to the parent entity otherwise hibernate will throw detach entity exception.
   *
   * @param secureExchangeRequestId    The ID of the Pen Retrieval Request.
   * @param secureExchangeComment The individual comment by staff or student.
   * @return SecureExchangeCommentEntity, the saved instance.
   */
  public SecureExchangeCommentEntity save(UUID secureExchangeRequestId, SecureExchangeCommentEntity secureExchangeComment) {
    val result = this.getSecureExchangeRequestRepository().findById(secureExchangeRequestId);
    if (result.isPresent()) {
      secureExchangeComment.setSecureExchangeEntity(result.get());
      return this.getSecureExchangeRequestCommentRepository().save(secureExchangeComment);
    }
    throw new EntityNotFoundException(SecureExchangeEntity.class, "SecureExchange", secureExchangeRequestId.toString());
  }

}
