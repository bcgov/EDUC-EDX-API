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
  private final SecureExchangeRequestRepository secureExchangeRequestRepository;
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeRequestCommentRepository secureExchangeRequestCommentRepository;
  @Getter(AccessLevel.PRIVATE)
  private final DocumentRepository documentRepository;

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeStatusCodeTableRepository secureExchangeStatusCodeTableRepo;

  @Autowired
  public SecureExchangeService(final SecureExchangeRequestRepository secureExchangeRequestRepository, final SecureExchangeRequestCommentRepository secureExchangeRequestCommentRepository, final DocumentRepository documentRepository, final SecureExchangeStatusCodeTableRepository secureExchangeStatusCodeTableRepo) {
    this.secureExchangeRequestRepository = secureExchangeRequestRepository;
    this.secureExchangeRequestCommentRepository = secureExchangeRequestCommentRepository;
    this.documentRepository = documentRepository;
    this.secureExchangeStatusCodeTableRepo = secureExchangeStatusCodeTableRepo;

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
   * set the status to DRAFT in the initial submit of secure exchange.
   *
   * @param secureExchangeRequest the secure exchange object to be persisted in the DB.
   * @return the persisted entity.
   */
  public SecureExchangeEntity createSecureExchange(final SecureExchangeEntity secureExchangeRequest) {
    secureExchangeRequest.setSecureExchangeStatusCode(SecureExchangeStatusCode.NEW.toString());
    secureExchangeRequest.setStatusUpdateDate(LocalDateTime.now());
    secureExchangeRequest.setIsReadByMinistry("N");
    secureExchangeRequest.setIsReadByExchangeContact("N");
    TransformUtil.uppercaseFields(secureExchangeRequest);
    return this.getSecureExchangeRequestRepository().save(secureExchangeRequest);
  }


  public Iterable<SecureExchangeStatusCodeEntity> getSecureExchangeStatusCodesList() {
    return this.getSecureExchangeStatusCodeTableRepo().findAll();
  }

  public List<SecureExchangeEntity> findSecureExchange(final UUID edxUserSchoolID, final UUID edxUserDistrictID, final UUID ministryOwnershipTeamID,final UUID ministryContactTeamID, final UUID edxUserID, final String status) {
    return this.getSecureExchangeRequestRepository().findSecureExchange(edxUserSchoolID, edxUserDistrictID, ministryOwnershipTeamID, ministryContactTeamID, edxUserID, status);
  }


  /**
   * This method has to add some DB fields values to the incoming to keep track of audit columns and parent child relationship.
   *
   * @param secureExchange the object which needs to be updated.
   * @return updated object.
   */
  public SecureExchangeEntity updateSecureExchange(final SecureExchangeEntity secureExchange) {
    final Optional<SecureExchangeEntity> curSecureExchange = this.getSecureExchangeRequestRepository().findById(secureExchange.getSecureExchangeID());
    if (curSecureExchange.isPresent()) {
      SecureExchangeEntity newExchangeRequest = curSecureExchange.get();
      this.logUpdates(secureExchange, newExchangeRequest);
      secureExchange.setSecureExchangeComment(newExchangeRequest.getSecureExchangeComment());
      BeanUtils.copyProperties(secureExchange, newExchangeRequest);
      TransformUtil.uppercaseFields(newExchangeRequest);
      newExchangeRequest = this.secureExchangeRequestRepository.save(newExchangeRequest);
      return newExchangeRequest;
    } else {
      throw new EntityNotFoundException(SecureExchange.class, "SecureExchange", secureExchange.getSecureExchangeID().toString());
    }
  }

  private void logUpdates(final SecureExchangeEntity secureExchange, final SecureExchangeEntity newSecureExchange) {
    if (log.isDebugEnabled()) {
      log.debug("secure exchange update, current :: {}, new :: {}", newSecureExchange, secureExchange);
    } else if (!StringUtils.equalsIgnoreCase(secureExchange.getSecureExchangeStatusCode(), newSecureExchange.getSecureExchangeStatusCode())) {
      log.info("secure exchange status change, secure exchange id :: {},  current :: {}, new :: {}", secureExchange.getSecureExchangeID(), newSecureExchange.getSecureExchangeStatusCode(), secureExchange.getSecureExchangeStatusCode());
    }
  }

  private void deleteAssociatedDocumentsAndComments(final SecureExchangeEntity entity) {
    val documents = this.getDocumentRepository().findBySecureExchangeSecureExchangeID(entity.getSecureExchangeID());
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
      throw new EntityNotFoundException(SecureExchangeEntity.class, "SecureExchangeID", id.toString());
    }
  }

  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<SecureExchangeEntity>> findAll(final Specification<SecureExchangeEntity> secureExchangeSpecs, final Integer pageNumber, final Integer pageSize, final List<Sort.Order> sorts) {
    final Pageable paging = PageRequest.of(pageNumber, pageSize, Sort.by(sorts));
    try {
      val result = this.getSecureExchangeRequestRepository().findAll(secureExchangeSpecs, paging);
      return CompletableFuture.completedFuture(result);
    } catch (final Exception ex) {
      throw new CompletionException(ex);
    }
  }
}
