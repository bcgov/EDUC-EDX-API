package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.PenReqDocumentEndpoint;
import ca.bc.gov.educ.api.edx.config.mappers.v1.DocumentMapper;
import ca.bc.gov.educ.api.edx.config.mappers.v1.DocumentTypeCodeMapper;
import ca.bc.gov.educ.api.edx.service.v1.DocumentService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.validator.PenRequestDocumentsValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class SecureExchangeDocumentController extends BaseController implements PenReqDocumentEndpoint {

  private static final DocumentMapper mapper = DocumentMapper.mapper;

  private static final DocumentTypeCodeMapper documentTypeCodeMapper = DocumentTypeCodeMapper.mapper;

  @Getter(AccessLevel.PRIVATE)
  private final DocumentService documentService;
  @Getter(AccessLevel.PRIVATE)
  private final PenRequestDocumentsValidator validator;

  @Autowired
  SecureExchangeDocumentController(final DocumentService documentService, final PenRequestDocumentsValidator validator) {
    this.documentService = documentService;
    this.validator = validator;
  }

  @Override
  public SecureExchangeDocument readDocument(String penRequestID, String documentID, String includeDocData) {
    return mapper.toStructure(getDocumentService().retrieveDocument(UUID.fromString(penRequestID), UUID.fromString(documentID), includeDocData));
  }

  @Override
  public SecureExchangeDocMetadata createDocument(String penRequestID, SecureExchangeDocument secureExchangeDocument) {
    setAuditColumns(secureExchangeDocument);
    val model = mapper.toModel(secureExchangeDocument);
    getValidator().validateDocumentPayload(model, true);
    return mapper.toMetadataStructure(getDocumentService().createDocument(UUID.fromString(penRequestID), model));
  }

  @Override
  public SecureExchangeDocMetadata updateDocument(UUID penRequestID, UUID documentID, SecureExchangeDocument secureExchangeDocument) {
    setAuditColumns(secureExchangeDocument);
    val model = mapper.toModel(secureExchangeDocument);
    getValidator().validateDocumentPayload(model, false);
    return mapper.toMetadataStructure(getDocumentService().updateDocument(penRequestID, documentID, model));
  }

  public SecureExchangeDocMetadata deleteDocument(String penRequestID, String documentID) {
    return mapper.toMetadataStructure(getDocumentService().deleteDocument(UUID.fromString(penRequestID), UUID.fromString(documentID)));
  }

  public Iterable<SecureExchangeDocMetadata> readAllDocumentMetadata(String penRequestID) {
    return getDocumentService().retrieveAllDocumentMetadata(UUID.fromString(penRequestID))
            .stream().map(mapper::toMetadataStructure).collect(Collectors.toList());
  }

  public PenReqDocRequirement getDocumentRequirements() {
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
