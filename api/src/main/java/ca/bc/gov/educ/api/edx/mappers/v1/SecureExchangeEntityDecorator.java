package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.mappers.Base64Mapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComment;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreate;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocMetadata;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public abstract class SecureExchangeEntityDecorator implements SecureExchangeEntityMapper {
  private final SecureExchangeEntityMapper delegate;
  private final UUIDMapper uUIDMapper = new UUIDMapper();
  private final LocalDateTimeMapper localDateTimeMapper = new LocalDateTimeMapper();
  private final Base64Mapper base64Mapper = new Base64Mapper();

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

    var documents = entity.getSecureExchangeDocument();

    if(documents != null && !documents.isEmpty()) {
      secureExchange.setDocumentList(new ArrayList<>());

      for (val document : documents) {
        SecureExchangeDocMetadata doc = new SecureExchangeDocMetadata();
        doc.setDocumentTypeCode(document.getDocumentTypeCode());
        doc.setDocumentID(document.getDocumentID().toString());
        doc.setFileExtension(document.getFileExtension());
        doc.setFileSize(document.getFileSize());
        doc.setFileName(document.getFileName());
        if(document.getEdxUserID() != null) {
          doc.setEdxUserID(document.getEdxUserID().toString());
        }
        doc.setStaffUserIdentifier(document.getStaffUserIdentifier());
        doc.setCreateDate(document.getCreateDate().toString());
        secureExchange.getDocumentList().add(doc);
      }
    }
    return secureExchange;
  }

  @Override
  public SecureExchangeEntity toModel(SecureExchange struct) {
    val postedEntity = this.delegate.toModel(struct);

    var comments = struct.getCommentsList();

    setupComments(comments, postedEntity);
    setupStudents(struct.getStudentsList(), postedEntity);
    return postedEntity;
  }

  private void setupStudents(List<SecureExchangeStudent> studentsList, SecureExchangeEntity postedEntity) {
    if(studentsList != null && !studentsList.isEmpty()) {
      postedEntity.setSecureExchangeStudents(new HashSet<>());
      for(val student : studentsList){
        SecureExchangeStudentEntity secureExchangeStudentEntity = new SecureExchangeStudentEntity();
        secureExchangeStudentEntity.setSecureExchangeEntity(postedEntity);
        if(!StringUtils.isBlank(student.getSecureExchangeStudentId())){
          secureExchangeStudentEntity.setSecureExchangeStudentId(uUIDMapper.map(student.getSecureExchangeStudentId()));
        }
        secureExchangeStudentEntity.setCreateUser(
                (StringUtils.isBlank(student.getCreateUser())) ? ApplicationProperties.CLIENT_ID : student.getCreateUser()
        );
        secureExchangeStudentEntity.setCreateDate(
                (StringUtils.isBlank(student.getCreateDate())) ? LocalDateTime.now() : localDateTimeMapper.map(student.getCreateDate())
        );
        secureExchangeStudentEntity.setStudentId(uUIDMapper.map(student.getStudentId()));
        postedEntity.getSecureExchangeStudents().add(secureExchangeStudentEntity);
      }
    }
  }

  @Override
  public SecureExchangeEntity toModel(SecureExchangeCreate struct) {
    val postedEntity = this.delegate.toModel(struct);

    var comments = struct.getCommentsList();

    setupComments(comments, postedEntity);
    setupStudents(struct.getStudentList(), postedEntity);
    var documents = struct.getDocumentList();

    if(documents != null && !documents.isEmpty()) {
      postedEntity.setSecureExchangeDocument(new HashSet<>());

      for (val document : documents) {
        SecureExchangeDocumentEntity newDocument = new SecureExchangeDocumentEntity();
        newDocument.setStaffUserIdentifier(document.getStaffUserIdentifier());
        newDocument.setDocumentData(base64Mapper.map(document.getDocumentData()));

        newDocument.setSecureExchangeEntity(postedEntity);

        newDocument.setCreateDate(LocalDateTime.now());
        newDocument.setUpdateDate(LocalDateTime.now());

        newDocument.setUpdateUser(document.getUpdateUser());
        newDocument.setCreateUser(document.getCreateUser());

        newDocument.setFileExtension(document.getFileExtension());
        newDocument.setFileName(document.getFileName());
        newDocument.setFileSize(document.getFileSize());

        newDocument.setDocumentTypeCode(document.getDocumentTypeCode());

        if(StringUtils.isNotBlank(document.getEdxUserID())) {
          newDocument.setEdxUserID(UUID.fromString(document.getEdxUserID()));
        }

        if (StringUtils.isBlank(document.getCreateUser())) {
          newDocument.setCreateUser(ApplicationProperties.CLIENT_ID);
        }
        if (StringUtils.isBlank(document.getUpdateUser())) {
          newDocument.setUpdateUser(ApplicationProperties.CLIENT_ID);
        }
        postedEntity.getSecureExchangeDocument().add(newDocument);
      }
    }

    return postedEntity;
  }

  private void setupComments(List<SecureExchangeComment> comments, SecureExchangeEntity postedEntity){
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

  }

}
