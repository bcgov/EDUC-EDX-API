package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestCommentsEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.repository.PenRequestCommentRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
public class PenRequestCommentService {

  @Getter(AccessLevel.PRIVATE)
  private final PenRequestRepository penRequestRepository;

  @Getter(AccessLevel.PRIVATE)
  private final PenRequestCommentRepository penRequestCommentRepository;

  @Autowired
  PenRequestCommentService(final PenRequestRepository penRequestRepository, final PenRequestCommentRepository penRequestCommentRepository) {
    this.penRequestRepository = penRequestRepository;
    this.penRequestCommentRepository = penRequestCommentRepository;
  }

  public Set<PenRequestCommentsEntity> retrieveComments(UUID penRetrievalRequestId) {
    final Optional<PenRequestEntity> entity = getPenRequestRepository().findById(penRetrievalRequestId);
    if (entity.isPresent()) {
      return entity.get().getPenRequestComments();
    }
    throw new EntityNotFoundException(PenRequestEntity.class, "PenRequest", penRetrievalRequestId.toString());
  }

  /**
   * Need to find the entity first as it is the parent entity and system is trying to persist the child entity so need to attach it to the parent entity otherwise hibernate will throw detach entity exception.
   *
   * @param penRetrievalRequestId    The ID of the Pen Retrieval Request.
   * @param penRequestCommentsEntity The individual comment by staff or student.
   * @return PenRequestCommentsEntity, the saved instance.
   */
  public PenRequestCommentsEntity save(UUID penRetrievalRequestId, PenRequestCommentsEntity penRequestCommentsEntity) {
    val result = getPenRequestRepository().findById(penRetrievalRequestId);
    if (result.isPresent()) {
      penRequestCommentsEntity.setPenRequestEntity(result.get());
      return getPenRequestCommentRepository().save(penRequestCommentsEntity);
    }
    throw new EntityNotFoundException(PenRequestEntity.class, "PenRequest", penRetrievalRequestId.toString());
  }

}
