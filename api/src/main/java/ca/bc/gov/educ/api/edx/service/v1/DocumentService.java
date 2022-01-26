package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.exception.InvalidParameterException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentTypeCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.secureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.struct.v1.PenReqDocRequirement;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocumentTypeCode;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class DocumentService {

  public static final String PEN_REQUEST_ID = "penRequestId";
  private final DocumentRepository documentRepository;

  private final secureExchangeRequestRepository secureExchangeRequestRepository;

  private final DocumentTypeCodeTableRepository documentTypeCodeRepository;

  private final ApplicationProperties properties;

  @Autowired
  public DocumentService(final DocumentRepository documentRepository, final secureExchangeRequestRepository secureExchangeRequestRepository, final DocumentTypeCodeTableRepository documentTypeCodeRepository, final ApplicationProperties properties) {
    this.documentRepository = documentRepository;
    this.secureExchangeRequestRepository = secureExchangeRequestRepository;
    this.documentTypeCodeRepository = documentTypeCodeRepository;
    this.properties = properties;
  }


  /**
   * Search for Document Metadata by id
   *
   * @param documentID the documentID to fetch the Document from DB
   * @return The Document {@link SecureExchangeDocumentEntity} if found.
   * @throws EntityNotFoundException if no document found by the ID or penRequestID does not match.
   */
  public SecureExchangeDocumentEntity retrieveDocumentMetadata(final UUID penRequestId, final UUID documentID) {
    log.info("retrieving Document Metadata, documentID: " + documentID.toString());

    val result = this.documentRepository.findById(documentID);
    if (result.isEmpty()) {
      throw new EntityNotFoundException(SecureExchangeDocumentEntity.class, "documentID", documentID.toString());
    }

    val document = result.get();

    if (!document.getSecureExchange().getSecureExchangeID().equals(penRequestId)) {
      throw new EntityNotFoundException(SecureExchangeDocumentEntity.class, PEN_REQUEST_ID, penRequestId.toString());
    }

    return document;
  }

  /**
   * Search for Document with data by id
   *
   * @param documentID the documentID to fetch the Document from DB
   * @return The Document {@link SecureExchangeDocumentEntity} if found.
   * @throws EntityNotFoundException if no document found by the ID or penRequestID does not match.
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public SecureExchangeDocumentEntity retrieveDocument(final UUID penRequestId, final UUID documentID, final String includeDocData) {
    log.info("retrieving Document, documentID: " + documentID.toString());

    val document = this.retrieveDocumentMetadata(penRequestId, documentID);
    // trigger lazy loading
    if (ApplicationProperties.YES.equalsIgnoreCase(includeDocData) || ApplicationProperties.TRUE.equalsIgnoreCase(includeDocData)) {
      if (document.getDocumentData() == null || document.getDocumentData().length == 0) {
        document.setFileSize(0);
      }
    } else {
      // set it to null so that map struct would not trigger lazy loading.
      document.setDocumentData(null);
    }
    return document;
  }

  /**
   * Search for all document metadata by penRequestId
   *
   * @return {@link List< SecureExchangeDocumentEntity > }
   */
  public List<SecureExchangeDocumentEntity> retrieveAllDocumentMetadata(final UUID penRequestId) {
    return this.documentRepository.findByPenRequestPenRequestID(penRequestId);
  }

  public List<SecureExchangeDocumentEntity> retrieveAllDocumentsMetadata(){
    return this.documentRepository.findAll();
  }
  /**
   * Creates a DocumentEntity
   *
   * @param document DocumentEntity payload to be saved in DB
   * @return saved DocumentEntity.
   * @throws InvalidParameterException,EntityNotFoundException if payload contains invalid parameters
   */
  public SecureExchangeDocumentEntity createDocument(final UUID penRequestId, final SecureExchangeDocumentEntity document) {
    log.info(
            "creating Document, penRequestId: " + penRequestId.toString() + ", document: " + document.toString());
    val option = this.secureExchangeRequestRepository.findById(penRequestId);
    if (option.isPresent()) {
      val penRequest = option.get();
      document.setSecureExchange(penRequest);
      return this.documentRepository.save(document);
    } else {
      throw new EntityNotFoundException(SecureExchange.class, PEN_REQUEST_ID, penRequestId.toString());
    }
  }

  /**
   * Delete DocumentEntity by id
   *
   * @param documentID delete the document with this id.
   * @return DocumentEntity which was deleted.
   * @throws EntityNotFoundException if no entity exist by this id
   */
  public SecureExchangeDocumentEntity deleteDocument(final UUID penRequestId, final UUID documentID) {
    log.info("deleting Document, documentID: " + documentID.toString());
    val document = this.retrieveDocumentMetadata(penRequestId, documentID);
    this.documentRepository.delete(document);
    return document;
  }

  @Cacheable("documentTypeCodeList")
  public List<SecureExchangeDocumentTypeCodeEntity> getDocumentTypeCodeList() {
    return this.documentTypeCodeRepository.findAll();
  }

  /**
   * Get File Upload Requirement
   *
   * @return DocumentRequirementEntity
   */
  public PenReqDocRequirement getDocumentRequirements() {
    log.info("retrieving Document Requirements");
    return new PenReqDocRequirement(this.properties.getMaxFileSize(), this.properties.getFileExtensions());
  }

  /**
   * updates a DocumentEntity
   *
   * @param document DocumentEntity payload to be saved in DB
   * @return saved DocumentEntity.
   * @throws InvalidParameterException,EntityNotFoundException if payload contains invalid parameters
   */
  public SecureExchangeDocumentEntity updateDocument(final UUID penRequestId, final UUID documentId, final SecureExchangeDocumentEntity document) {
    log.info(
            "updating Document, documentId :: {} penRequestId :: {} :: ", documentId, penRequestId);
    final Optional<SecureExchangeDocumentEntity> documentEntityOptional = this.documentRepository.findById(documentId);
    if (documentEntityOptional.isPresent()) {
      val documentEntity = documentEntityOptional.get();
      val penRequestEntity = documentEntity.getSecureExchange();
      if (!penRequestEntity.getSecureExchangeID().equals(penRequestId)) {
        throw new EntityNotFoundException(SecureExchange.class, PEN_REQUEST_ID, penRequestId.toString());
      }
      documentEntity.setFileExtension(document.getFileExtension());
      documentEntity.setSecureExchangeDocumentTypeCode(document.getSecureExchangeDocumentTypeCode());
      documentEntity.setFileName(document.getFileName());
      documentEntity.setUpdateUser(document.getUpdateUser());
      documentEntity.setUpdateDate(document.getUpdateDate());
      return this.documentRepository.save(documentEntity);
    } else {
      throw new EntityNotFoundException(SecureExchangeDocumentEntity.class, "documentId", documentId.toString());
    }
  }
}
