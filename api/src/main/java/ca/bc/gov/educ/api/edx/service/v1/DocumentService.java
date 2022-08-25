package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.exception.InvalidParameterException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentTypeCodeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocRequirement;
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

  public static final String SECURE_EXCHANGE_ID = "secureExchangeId";
  private final DocumentRepository documentRepository;

  private final SecureExchangeRequestRepository secureExchangeRequestRepository;

  private final DocumentTypeCodeTableRepository documentTypeCodeRepository;

  private final ApplicationProperties properties;

  @Autowired
  public DocumentService(final DocumentRepository documentRepository, final SecureExchangeRequestRepository secureExchangeRequestRepository, final DocumentTypeCodeTableRepository documentTypeCodeRepository, final ApplicationProperties properties) {
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
   * @throws EntityNotFoundException if no document found by the ID or secureExchangeID does not match.
   */
  public SecureExchangeDocumentEntity retrieveDocumentMetadata(final UUID secureExchangeId, final UUID documentID) {
    log.info("retrieving Document Metadata, documentID: " + documentID.toString());

    val result = this.documentRepository.findById(documentID);
    if (result.isEmpty()) {
      throw new EntityNotFoundException(SecureExchangeDocumentEntity.class, "documentID", documentID.toString());
    }

    val document = result.get();

    if (!document.getSecureExchangeEntity().getSecureExchangeID().equals(secureExchangeId)) {
      throw new EntityNotFoundException(SecureExchangeDocumentEntity.class, SECURE_EXCHANGE_ID, secureExchangeId.toString());
    }

    return document;
  }

  /**
   * Search for Document with data by id
   *
   * @param documentID the documentID to fetch the Document from DB
   * @return The Document {@link SecureExchangeDocumentEntity} if found.
   * @throws EntityNotFoundException if no document found by the ID or secureExchangeID does not match.
   */
  @Transactional(propagation = Propagation.REQUIRED, readOnly = true)
  public SecureExchangeDocumentEntity retrieveDocument(final UUID secureExchangeId, final UUID documentID, final String includeDocData) {
    log.info("retrieving Document, documentID: " + documentID.toString());

    val document = this.retrieveDocumentMetadata(secureExchangeId, documentID);
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
   * Search for all document metadata by secureExchangeId
   */
  public List<SecureExchangeDocumentEntity> retrieveAllDocumentMetadata(final UUID secureExchangeId) {
    return this.documentRepository.findBySecureExchangeEntitySecureExchangeID(secureExchangeId);
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
  public SecureExchangeDocumentEntity createDocument(final UUID secureExchangeId, final SecureExchangeDocumentEntity document) {
    log.info(
            "creating Document, secureExchangeId: " + secureExchangeId.toString() + ", document: " + document.toString());
    val option = this.secureExchangeRequestRepository.findById(secureExchangeId);
    if (option.isPresent()) {
      val secureExchange = option.get();

      if ( document.getEdxUserID() == null && document.getStaffUserIdentifier() != null) {
        // EdxUserID doesn't exists implies call is from Ministry Side
        secureExchange.setReviewer(document.getStaffUserIdentifier());
        secureExchange.setIsReadByExchangeContact(false);
        secureExchange.setIsReadByMinistry(true);
      } else {
        // EdxUserID exists implies call is from School Side
        secureExchange.setIsReadByMinistry(false);
        secureExchange.setIsReadByExchangeContact(true);
      }

      this.secureExchangeRequestRepository.save(secureExchange);

      document.setSecureExchangeEntity(secureExchange);
      return this.documentRepository.save(document);
    } else {
      throw new EntityNotFoundException(SecureExchange.class, SECURE_EXCHANGE_ID, secureExchangeId.toString());
    }
  }

  /**
   * Delete DocumentEntity by id
   *
   * @param documentID delete the document with this id.
   * @return DocumentEntity which was deleted.
   * @throws EntityNotFoundException if no entity exist by this id
   */
  public SecureExchangeDocumentEntity deleteDocument(final UUID secureExchangeId, final UUID documentID) {
    log.info("deleting Document, documentID: " + documentID.toString());
    val document = this.retrieveDocumentMetadata(secureExchangeId, documentID);
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
  public SecureExchangeDocRequirement getDocumentRequirements() {
    log.info("retrieving Document Requirements");
    return new SecureExchangeDocRequirement(this.properties.getMaxFileSize(), this.properties.getFileExtensions());
  }

  /**
   * updates a DocumentEntity
   *
   * @param document DocumentEntity payload to be saved in DB
   * @return saved DocumentEntity.
   * @throws InvalidParameterException,EntityNotFoundException if payload contains invalid parameters
   */
  public SecureExchangeDocumentEntity updateDocument(final UUID secureExchangeId, final UUID documentId, final SecureExchangeDocumentEntity document) {
    log.info(
            "updating Document, documentId :: {} secureExchangeId :: {} :: ", documentId, secureExchangeId);
    final Optional<SecureExchangeDocumentEntity> documentEntityOptional = this.documentRepository.findById(documentId);
    if (documentEntityOptional.isPresent()) {
      val documentEntity = documentEntityOptional.get();
      val secureExchangeEntity = documentEntity.getSecureExchangeEntity();
      if (!secureExchangeEntity.getSecureExchangeID().equals(secureExchangeId)) {
        throw new EntityNotFoundException(SecureExchange.class, SECURE_EXCHANGE_ID, secureExchangeId.toString());
      }
      documentEntity.setFileExtension(document.getFileExtension());
      documentEntity.setDocumentTypeCode(document.getDocumentTypeCode());
      documentEntity.setFileName(document.getFileName());
      documentEntity.setUpdateUser(document.getUpdateUser());
      documentEntity.setUpdateDate(document.getUpdateDate());
      return this.documentRepository.save(documentEntity);
    } else {
      throw new EntityNotFoundException(SecureExchangeDocumentEntity.class, "documentId", documentId.toString());
    }
  }
}
