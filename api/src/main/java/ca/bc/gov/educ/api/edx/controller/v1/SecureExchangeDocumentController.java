package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.SecureExchangeDocumentEndpoint;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeDocumentMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeDocumentTypeCodeMapper;
import ca.bc.gov.educ.api.edx.service.v1.DocumentService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.validator.SecureExchangeDocumentsValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class SecureExchangeDocumentController extends BaseController implements SecureExchangeDocumentEndpoint {

  private static final SecureExchangeDocumentMapper mapper = SecureExchangeDocumentMapper.mapper;

  private static final SecureExchangeDocumentTypeCodeMapper documentTypeCodeMapper = SecureExchangeDocumentTypeCodeMapper.mapper;

  @Getter(AccessLevel.PRIVATE)
  private final DocumentService documentService;
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeDocumentsValidator validator;

  @Autowired
  SecureExchangeDocumentController(final DocumentService documentService, final SecureExchangeDocumentsValidator validator) {
    this.documentService = documentService;
    this.validator = validator;
  }

  @Override
  public SecureExchangeDocument readDocument(String secureExchangeID, String documentID, String includeDocData) {
    return mapper.toStructure(getDocumentService().retrieveDocument(UUID.fromString(secureExchangeID), UUID.fromString(documentID), includeDocData));
  }

  @Override
  public SecureExchangeDocMetadata createDocument(String secureExchangeID, SecureExchangeDocument secureExchangeDocument) {
    setAuditColumns(secureExchangeDocument);
    val model = mapper.toModel(secureExchangeDocument);
    getValidator().validateDocumentPayload(model, true);
    return mapper.toMetadataStructure(getDocumentService().createDocument(UUID.fromString(secureExchangeID), model));
  }

  @Override
  public SecureExchangeDocMetadata updateDocument(UUID secureExchangeID, UUID documentID, SecureExchangeDocument secureExchangeDocument) {
    setAuditColumns(secureExchangeDocument);
    val model = mapper.toModel(secureExchangeDocument);
    getValidator().validateDocumentPayload(model, false);
    return mapper.toMetadataStructure(getDocumentService().updateDocument(secureExchangeID, documentID, model));
  }

  public SecureExchangeDocMetadata deleteDocument(String secureExchangeID, String documentID) {
    return mapper.toMetadataStructure(getDocumentService().deleteDocument(UUID.fromString(secureExchangeID), UUID.fromString(documentID)));
  }

  public Iterable<SecureExchangeDocMetadata> readAllDocumentMetadata(String secureExchangeID) {
    return getDocumentService().retrieveAllDocumentMetadata(UUID.fromString(secureExchangeID))
            .stream().map(mapper::toMetadataStructure).collect(Collectors.toList());
  }

  public SecureExchangeDocRequirement getDocumentRequirements() {
    return documentService.getDocumentRequirements();
  }

  public List<SecureExchangeDocumentTypeCode> getDocumentTypeCodes() {
    return getDocumentService().getDocumentTypeCodeList().stream()
            .map(documentTypeCodeMapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public List<SecureExchangeDocumentMetadata> readAllDocumentsMetadata() {
    return getDocumentService().retrieveAllDocumentsMetadata().stream().map(mapper::toMetaData).collect(Collectors.toList());
  }

}
