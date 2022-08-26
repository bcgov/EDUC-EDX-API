package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestCommentRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.utils.TransformUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
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
   * @param secureExchangeRequestId The ID of the Pen Retrieval Request.
   * @param secureExchangeComment   The individual comment by staff or student.
   * @return SecureExchangeCommentEntity, the saved instance.
   */
  public SecureExchangeCommentEntity save(UUID secureExchangeRequestId, SecureExchangeCommentEntity secureExchangeComment) {
    val result = this.getSecureExchangeRequestRepository().findById(secureExchangeRequestId);
    if (result.isPresent()) {
      SecureExchangeEntity secureExchangeEntity = result.get();
      secureExchangeComment.setSecureExchangeEntity(secureExchangeEntity);
      if (null == secureExchangeComment.getEdxUserID() && null != secureExchangeComment.getStaffUserIdentifier()) {
        // EdxUserID doesn't exists implies call is from Ministry Side
        secureExchangeEntity.setIsReadByExchangeContact(false);
        secureExchangeEntity.setIsReadByMinistry(true);
        secureExchangeEntity.setReviewer(secureExchangeComment.getStaffUserIdentifier());
        TransformUtil.uppercaseFields(secureExchangeEntity);
        TransformUtil.uppercaseFields(secureExchangeComment);
      } else {
        // EdxUserID exists implies call is from School Side
        secureExchangeEntity.setIsReadByMinistry(false);
        secureExchangeEntity.setIsReadByExchangeContact(true);
      }
      secureExchangeEntity.getSecureExchangeComment().add(secureExchangeComment);
      this.getSecureExchangeRequestRepository().save(secureExchangeEntity);

      return secureExchangeComment;
    }
    throw new EntityNotFoundException(SecureExchangeEntity.class, "SecureExchange", secureExchangeRequestId.toString());
  }


  public Optional<SecureExchangeCommentEntity> findCommentForSecureExchange(LocalDateTime commentTimestamp, String commentUserName, UUID secureExchangeID, String content){
   return getSecureExchangeRequestCommentRepository().findByCommentTimestampAndCommentUserNameAndSecureExchangeEntity_SecureExchangeIDAndContent(commentTimestamp,commentUserName,secureExchangeID,content);
  }
}
