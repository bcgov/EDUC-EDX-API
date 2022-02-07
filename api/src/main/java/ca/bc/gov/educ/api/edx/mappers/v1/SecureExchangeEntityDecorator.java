package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

public abstract class SecureExchangeEntityDecorator implements SecureExchangeEntityMapper {
  private final SecureExchangeEntityMapper delegate;

  protected SecureExchangeEntityDecorator(final SecureExchangeEntityMapper delegate) {
    this.delegate = delegate;
  }

  @Override
  public SecureExchangeEntity toModel(SecureExchange struct) {
    val postedEntity = this.delegate.toModel(struct);

    var comments = struct.getCommentsList();

    if(comments != null && !comments.isEmpty()) {
      postedEntity.setSecureExchangeComment(new HashSet<>());

      for (val comment : comments) {
        SecureExchangeCommentEntity newComment = new SecureExchangeCommentEntity();
        newComment.setContent(comment.getContent());
        newComment.setStaffUserIdentifier(comment.getStaffUserIdentifier());
        newComment.setCommentUserName(comment.getCommentUserName());
        if(StringUtils.isNotBlank(comment.getEdxUserID())) {
          newComment.setEdxUserID(UUID.fromString(comment.getEdxUserID()));
        }
        newComment.setSecureExchangeEntity(postedEntity);
        newComment.setCreateUser(comment.getCreateUser());
        newComment.setUpdateUser(comment.getUpdateUser());
        newComment.setUpdateDate(LocalDateTime.now());
        newComment.setCreateDate(LocalDateTime.now());
        postedEntity.getSecureExchangeComment().add(newComment);
      }
    }
    return postedEntity;
  }
}
