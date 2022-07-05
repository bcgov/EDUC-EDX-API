package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComment;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

public abstract class SecureExchangeEntityDecorator implements SecureExchangeEntityMapper {
  private final SecureExchangeEntityMapper delegate;

  protected SecureExchangeEntityDecorator(final SecureExchangeEntityMapper delegate) {
    this.delegate = delegate;
  }

  @Override
  public SecureExchange toStructure(SecureExchangeEntity entity) {
    val secureExchange = this.delegate.toStructure(entity);

    var comments = entity.getSecureExchangeComment();

    if(comments != null && !comments.isEmpty()) {
      secureExchange.setCommentsList(new ArrayList<>());

      for (val comment : comments) {
        SecureExchangeComment newComment = new SecureExchangeComment();
        newComment.setContent(comment.getContent());
        newComment.setStaffUserIdentifier(comment.getStaffUserIdentifier());
        newComment.setCommentUserName(comment.getCommentUserName());
        if(comment.getEdxUserID() != null) {
          newComment.setEdxUserID(comment.getEdxUserID().toString());
        }

        newComment.setSecureExchangeID(secureExchange.getSecureExchangeID());
        newComment.setCreateUser(comment.getCreateUser());
        newComment.setUpdateUser(comment.getUpdateUser());
        newComment.setCommentTimestamp(comment.getCommentTimestamp().toString());
        newComment.setUpdateDate(comment.getUpdateDate().toString());
        newComment.setCreateDate(comment.getCreateDate().toString());
        secureExchange.getCommentsList().add(newComment);
      }
    }

    var students = entity.getSecureExchangeStudents();
    if(students != null && !students.isEmpty()){
      secureExchange.setStudentsList(new ArrayList<>());
      for (val student : students) {
        SecureExchangeStudent secureExchangeStudent = new SecureExchangeStudent();
        secureExchangeStudent.setSecureExchangeId(entity.getSecureExchangeID().toString());
        secureExchangeStudent.setSecureExchangeStudentId(student.getSecureExchangeStudentId().toString());
        secureExchangeStudent.setStudentId(student.getStudentId().toString());
        secureExchangeStudent.setCreateUser(student.getCreateUser());
        secureExchangeStudent.setCreateDate(student.getCreateDate().toString());
        secureExchange.getStudentsList().add(secureExchangeStudent);
      }
    }

    return secureExchange;
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
        if (StringUtils.isBlank(comment.getCreateUser())) {
          newComment.setCreateUser(ApplicationProperties.CLIENT_ID);
        }
        if (StringUtils.isBlank(comment.getUpdateUser())) {
          newComment.setUpdateUser(ApplicationProperties.CLIENT_ID);
        }

        if (StringUtils.isBlank(comment.getCommentTimestamp())) {
          newComment.setCommentTimestamp(LocalDateTime.now());
        }
        newComment.setUpdateDate(LocalDateTime.now());
        newComment.setCreateDate(LocalDateTime.now());
        postedEntity.getSecureExchangeComment().add(newComment);
      }
    }

    var students = struct.getStudentsList();

    if(students != null && !students.isEmpty()) {
      postedEntity.setSecureExchangeStudents(new HashSet<>());
      for(val student : students){
        SecureExchangeStudentEntity secureExchangeStudentEntity = new SecureExchangeStudentEntity();
        secureExchangeStudentEntity.setSecureExchangeEntity(postedEntity);
        secureExchangeStudentEntity.setSecureExchangeStudentId(UUID.fromString(student.getSecureExchangeStudentId()));
        secureExchangeStudentEntity.setCreateUser(
                (StringUtils.isBlank(student.getCreateUser())) ? ApplicationProperties.CLIENT_ID : student.getCreateUser()
        );
        secureExchangeStudentEntity.setCreateDate(
                (StringUtils.isBlank(student.getCreateDate())) ? LocalDateTime.now() : LocalDateTime.parse(student.getCreateDate())
        );
        postedEntity.getSecureExchangeStudents().add(secureExchangeStudentEntity);
      }
    }

    return postedEntity;
  }

}
