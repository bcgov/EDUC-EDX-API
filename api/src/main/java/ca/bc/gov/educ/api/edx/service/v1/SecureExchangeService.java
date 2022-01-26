package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.SecureExchangeStatusCode;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.utils.TransformUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Service
@Slf4j
public class SecureExchangeService {

  @Getter(AccessLevel.PRIVATE)
  private final secureExchangeRequestRepository secureExchangeRequestRepository;
  @Getter(AccessLevel.PRIVATE)
  private final secureExchangeRequestCommentRepository secureExchangeRequestCommentRepository;
  @Getter(AccessLevel.PRIVATE)
  private final DocumentRepository documentRepository;

  @Getter(AccessLevel.PRIVATE)
  private final secureExchangeStatusCodeTableRepository penRequestStatusCodeTableRepo;

  @Autowired
  public SecureExchangeService(final secureExchangeRequestRepository secureExchangeRequestRepository, final secureExchangeRequestCommentRepository secureExchangeRequestCommentRepository, final DocumentRepository documentRepository, final secureExchangeStatusCodeTableRepository penRequestStatusCodeTableRepo) {
    this.secureExchangeRequestRepository = secureExchangeRequestRepository;
    this.secureExchangeRequestCommentRepository = secureExchangeRequestCommentRepository;
    this.documentRepository = documentRepository;
    this.penRequestStatusCodeTableRepo = penRequestStatusCodeTableRepo;

  }

  public SecureExchangeEntity retrieveSecureExchange(final UUID id) {
    final Optional<SecureExchangeEntity> res = this.getSecureExchangeRequestRepository().findById(id);
    if (res.isPresent()) {
      return res.get();
    } else {
      throw new EntityNotFoundException(SecureExchange.class, "secureExchangeID", id.toString());
    }
  }

  /**
   * set the status to DRAFT in the initial submit of pen request.
   *
   * @param secureExchangeRequest the pen request object to be persisted in the DB.
   * @return the persisted entity.
   */
  public SecureExchangeEntity createSecureExchange(final SecureExchangeEntity secureExchangeRequest) {
    secureExchangeRequest.setSecureExchangeStatusCode(SecureExchangeStatusCode.DRAFT.toString());
    secureExchangeRequest.setStatusUpdateDate(LocalDateTime.now());
    TransformUtil.uppercaseFields(secureExchangeRequest);
    return this.getSecureExchangeRequestRepository().save(secureExchangeRequest);
  }


  public Iterable<SecureExchangeStatusCodeEntity> getPenRequestStatusCodesList() {
    return this.getPenRequestStatusCodeTableRepo().findAll();
  }

  public List<SecureExchangeEntity> findSecureExchange(final UUID digitalID, final String statusCode) {
    return this.getSecureExchangeRequestRepository().findSecureExchange(digitalID, statusCode);
  }



  /**
   * This method has to add some DB fields values to the incoming to keep track of audit columns and parent child relationship.
   *
   * @param secureExchange the object which needs to be updated.
   * @return updated object.
   */
  public SecureExchangeEntity updatePenRequest(final SecureExchangeEntity secureExchange) {
    final Optional<SecureExchangeEntity> curPenRequest = this.getSecureExchangeRequestRepository().findById(secureExchange.getSecureExchangeID());
    if (curPenRequest.isPresent()) {
      SecureExchangeEntity newExchangeRequest = curPenRequest.get();
      this.logUpdates(secureExchange, newExchangeRequest);
      secureExchange.setSecureExchangeComment(newExchangeRequest.getSecureExchangeComment());
      BeanUtils.copyProperties(secureExchange, newExchangeRequest);
      TransformUtil.uppercaseFields(newExchangeRequest);
      newExchangeRequest = this.secureExchangeRequestRepository.save(newExchangeRequest);
      return newExchangeRequest;
    } else {
      throw new EntityNotFoundException(SecureExchange.class, "PenRequest", secureExchange.getSecureExchangeID().toString());
    }
  }

  private void logUpdates(final SecureExchangeEntity secureExchange, final SecureExchangeEntity newSecureExchange) {
    if (log.isDebugEnabled()) {
      log.debug("Pen Request update, current :: {}, new :: {}", newSecureExchange, secureExchange);
    } else if (!StringUtils.equalsIgnoreCase(secureExchange.getSecureExchangeStatusCode(), newSecureExchange.getSecureExchangeStatusCode())) {
      log.info("Pen Request status change, pen request id :: {},  current :: {}, new :: {}",secureExchange.getSecureExchangeID(), newSecureExchange.getSecureExchangeStatusCode(), secureExchange.getSecureExchangeStatusCode());
    }
  }

  private void deleteAssociatedDocumentsAndComments(final SecureExchangeEntity entity) {
    val documents = this.getDocumentRepository().findByPenRequestPenRequestID(entity.getSecureExchangeID());
    if (documents != null && !documents.isEmpty()) {
      this.getDocumentRepository().deleteAll(documents);
    }
    if (entity.getSecureExchangeComment() != null && !entity.getSecureExchangeComment().isEmpty()) {
      this.getSecureExchangeRequestCommentRepository().deleteAll(entity.getSecureExchangeComment());
    }
  }

  @Transactional(propagation = Propagation.MANDATORY)
  public void deleteById(final UUID id) {
    val entity = this.getSecureExchangeRequestRepository().findById(id);
    if (entity.isPresent()) {
      this.deleteAssociatedDocumentsAndComments(entity.get());
      this.getSecureExchangeRequestRepository().delete(entity.get());
    } else {
      throw new EntityNotFoundException(SecureExchangeEntity.class, "PenRequestID", id.toString());
    }
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<SecureExchangeEntity>> findAll(final Specification<SecureExchangeEntity> penRequestSpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    final Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
    try {
      val result = this.getSecureExchangeRequestRepository().findAll(penRequestSpecs, paging);
      return CompletableFuture.completedFuture(result);
    } catch (final Exception ex) {
      throw new CompletionException(ex);
    }
  }
}
