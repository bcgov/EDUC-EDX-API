package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.exception.InvalidParameterException;
import ca.bc.gov.educ.api.edx.exception.InvalidValueException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentTypeCodeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class SecureExchangeDocumentsValidator {

  private final ApplicationProperties properties;
  private final DocumentTypeCodeTableRepository documentTypeCodeRepository;

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

  public void validateDocumentPayload(final SecureExchangeDocumentEntity document, boolean isCreateOperation) {
    if (isCreateOperation && document.getDocumentID() != null) {
      throw new InvalidParameterException("documentID");
    }
    if (isCreateOperation && (document.getDocumentData() == null || document.getDocumentData().length == 0)) {
      throw new InvalidValueException("documentData", null);
    }

    if (!properties.getFileExtensions().contains(document.getFileExtension())) {
      throw new InvalidValueException("fileExtension", document.getFileExtension());
    }

    if (document.getFileSize() > properties.getMaxEncodedFileSize()) {
      throw new InvalidValueException("fileSize", document.getFileSize().toString(), "Max Encoded fileSize",
              String.valueOf(properties.getMaxFileSize()));
    }

    if (isCreateOperation && document.getFileSize() != document.getDocumentData().length) {
      throw new InvalidValueException("fileSize", document.getFileSize().toString(), "documentData length",
              String.valueOf(document.getDocumentData().length));
    }

    if (!isDocumentTypeCodeValid(document.getDocumentTypeCode())) {
      throw new InvalidValueException("documentTypeCode", document.getDocumentTypeCode());
    }
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
