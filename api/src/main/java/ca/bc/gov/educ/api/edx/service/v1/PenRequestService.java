package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.PenRequestStatusCode;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.GenderCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestStatusCodeEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.utils.TransformUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PenRequestService {

  @Getter(AccessLevel.PRIVATE)
  private final PenRequestRepository penRequestRepository;
  @Getter(AccessLevel.PRIVATE)
  private final PenRequestCommentRepository penRequestCommentRepository;
  @Getter(AccessLevel.PRIVATE)
  private final DocumentRepository documentRepository;

  @Getter(AccessLevel.PRIVATE)
  private final PenRequestStatusCodeTableRepository penRequestStatusCodeTableRepo;

  @Getter(AccessLevel.PRIVATE)
  private final GenderCodeTableRepository genderCodeTableRepo;

  @Autowired
  public PenRequestService(final PenRequestRepository penRequestRepository, final PenRequestCommentRepository penRequestCommentRepository, final DocumentRepository documentRepository, final PenRequestStatusCodeTableRepository penRequestStatusCodeTableRepo, final GenderCodeTableRepository genderCodeTableRepo) {
    this.penRequestRepository = penRequestRepository;
    this.penRequestCommentRepository = penRequestCommentRepository;
    this.documentRepository = documentRepository;
    this.penRequestStatusCodeTableRepo = penRequestStatusCodeTableRepo;
    this.genderCodeTableRepo = genderCodeTableRepo;
  }

  public PenRequestEntity retrievePenRequest(final UUID id) {
    final Optional<PenRequestEntity> res = this.getPenRequestRepository().findById(id);
    if (res.isPresent()) {
      return res.get();
    } else {
      throw new EntityNotFoundException(PenRequestEntity.class, "penRequestId", id.toString());
    }
  }

  /**
   * set the status to DRAFT in the initial submit of pen request.
   *
   * @param penRequest the pen request object to be persisted in the DB.
   * @return the persisted entity.
   */
  public PenRequestEntity createPenRequest(final PenRequestEntity penRequest) {
    penRequest.setPenRequestStatusCode(PenRequestStatusCode.DRAFT.toString());
    penRequest.setStatusUpdateDate(LocalDateTime.now());
    TransformUtil.uppercaseFields(penRequest);
    return this.getPenRequestRepository().save(penRequest);
  }


  public Iterable<PenRequestStatusCodeEntity> getPenRequestStatusCodesList() {
    return this.getPenRequestStatusCodeTableRepo().findAll();
  }

  public List<PenRequestEntity> findPenRequests(final UUID digitalID, final String statusCode, final String pen) {
    return this.getPenRequestRepository().findPenRequests(digitalID, statusCode, pen);
  }

  /**
   * Returns the full list of access channel codes
   *
   * @return {@link List<GenderCodeEntity>}
   */
  @Cacheable("genderCodes")
  public List<GenderCodeEntity> getGenderCodesList() {
    return this.genderCodeTableRepo.findAll();
  }

  private Map<String, GenderCodeEntity> loadGenderCodes() {
    return this.getGenderCodesList().stream().collect(Collectors.toMap(GenderCodeEntity::getGenderCode, genderCodeEntity -> genderCodeEntity));
  }

  public Optional<GenderCodeEntity> findGenderCode(final String genderCode) {
    return Optional.ofNullable(this.loadGenderCodes().get(genderCode));
  }

  /**
   * This method has to add some DB fields values to the incoming to keep track of audit columns and parent child relationship.
   *
   * @param penRequest the object which needs to be updated.
   * @return updated object.
   */
  public PenRequestEntity updatePenRequest(final PenRequestEntity penRequest) {
    final Optional<PenRequestEntity> curPenRequest = this.getPenRequestRepository().findById(penRequest.getPenRequestID());
    if (curPenRequest.isPresent()) {
      PenRequestEntity newPenRequest = curPenRequest.get();
      this.logUpdates(penRequest, newPenRequest);
      penRequest.setPenRequestComments(newPenRequest.getPenRequestComments());
      BeanUtils.copyProperties(penRequest, newPenRequest);
      TransformUtil.uppercaseFields(newPenRequest);
      newPenRequest = this.penRequestRepository.save(newPenRequest);
      return newPenRequest;
    } else {
      throw new EntityNotFoundException(PenRequestEntity.class, "PenRequest", penRequest.getPenRequestID().toString());
    }
  }

  private void logUpdates(final PenRequestEntity penRequest, final PenRequestEntity newPenRequest) {
    if (log.isDebugEnabled()) {
      log.debug("Pen Request update, current :: {}, new :: {}", newPenRequest, penRequest);
    } else if (!StringUtils.equalsIgnoreCase(penRequest.getPenRequestStatusCode(), newPenRequest.getPenRequestStatusCode())) {
      log.info("Pen Request status change, pen request id :: {},  current :: {}, new :: {}",penRequest.getPenRequestID(), newPenRequest.getPenRequestStatusCode(), penRequest.getPenRequestStatusCode());
    }
  }

  private void deleteAssociatedDocumentsAndComments(final PenRequestEntity entity) {
    val documents = this.getDocumentRepository().findByPenRequestPenRequestID(entity.getPenRequestID());
    if (documents != null && !documents.isEmpty()) {
      this.getDocumentRepository().deleteAll(documents);
    }
    if (entity.getPenRequestComments() != null && !entity.getPenRequestComments().isEmpty()) {
      this.getPenRequestCommentRepository().deleteAll(entity.getPenRequestComments());
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteById(final UUID id) {
    val entity = this.getPenRequestRepository().findById(id);
    if (entity.isPresent()) {
      this.deleteAssociatedDocumentsAndComments(entity.get());
      this.getPenRequestRepository().delete(entity.get());
    } else {
      throw new EntityNotFoundException(PenRequestEntity.class, "PenRequestID", id.toString());
    }
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<PenRequestEntity>> findAll(final Specification<PenRequestEntity> penRequestSpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    final Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
    try {
      val result = this.getPenRequestRepository().findAll(penRequestSpecs, paging);
      return CompletableFuture.completedFuture(result);
    } catch (final Exception ex) {
      throw new CompletionException(ex);
    }
  }
}
