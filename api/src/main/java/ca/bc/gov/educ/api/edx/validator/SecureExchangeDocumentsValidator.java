package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.mappers.Base64Mapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentTypeCodeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocument;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class SecureExchangeDocumentsValidator {

  private final ApplicationProperties properties;
  private final DocumentTypeCodeTableRepository documentTypeCodeRepository;
  private final Base64Mapper base64Mapper = new Base64Mapper();

  @Autowired
  public SecureExchangeDocumentsValidator(final ApplicationProperties properties, final DocumentTypeCodeTableRepository documentTypeCodeRepository) {
    this.properties = properties;
    this.documentTypeCodeRepository = documentTypeCodeRepository;
  }

  @PostConstruct
  public void init() {
    loadDocumentType();
  }

  @Cacheable("documentTypeCodes")
  public List<SecureExchangeDocumentTypeCodeEntity> loadDocumentType() {
    return documentTypeCodeRepository.findAll();
  }

  public List<FieldError> validateDocumentPayload(final SecureExchangeDocument document, boolean isCreateOperation) {
    final List<FieldError> apiValidationErrors = new ArrayList<>();
    if (isCreateOperation && document.getDocumentID() != null) {
      apiValidationErrors.add(createFieldError("documentID", document.getDocumentID(), "documentID must be null for new documents"));
      return apiValidationErrors;
    }
    if (isCreateOperation && (document.getDocumentData() == null || document.getDocumentData().getBytes().length == 0)) {
      apiValidationErrors.add(createFieldError("documentData", null, "No document data provided"));
      return apiValidationErrors;
    }

    if (properties.getFileExtensions().stream().noneMatch(ext -> ext.equalsIgnoreCase(document.getFileExtension().toLowerCase()))) {
      apiValidationErrors.add(createFieldError("fileExtension", document.getFileExtension(), "fileExtension provided is invalid"));
      return apiValidationErrors;
    }

    if (document.getFileSize() > properties.getMaxEncodedFileSize()) {
      apiValidationErrors.add(createFieldError("fileSize", document.getFileSize().toString(), "Document fileSize encoded is too large"));
      return apiValidationErrors;
    }

    if (isCreateOperation && document.getFileSize() != base64Mapper.map(document.getDocumentData()).length) {
      apiValidationErrors.add(createFieldError("fileSize", document.getFileSize().toString(), "Document fileSize does not match provided file size"));
      return apiValidationErrors;
    }

    if (!isDocumentTypeCodeValid(document.getDocumentTypeCode())) {
      apiValidationErrors.add(createFieldError("documentTypeCode", document.getDocumentTypeCode(), "Document type code is invalid"));
      return apiValidationErrors;
    }
    return apiValidationErrors;
  }

  private FieldError createFieldError(String fieldName, Object rejectedValue, String message) {
    return new FieldError("secureExchange", fieldName, rejectedValue, false, null, null, message);
  }

  public boolean isDocumentTypeCodeValid(final String documentTypeCode) {
    for (SecureExchangeDocumentTypeCodeEntity entity : loadDocumentType()) {
      if (entity.getSecureExchangeDocumentTypeCode().equalsIgnoreCase(documentTypeCode)) {
        return entity.getEffectiveDate().isBefore(LocalDateTime.now()) && entity.getExpiryDate().isAfter(LocalDateTime.now());
      }
    }
    return false;
  }
}
