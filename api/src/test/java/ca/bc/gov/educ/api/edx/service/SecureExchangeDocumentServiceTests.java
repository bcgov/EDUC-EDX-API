package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.secureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.DocumentService;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.DocumentTypeCodeBuilder;
import ca.bc.gov.educ.api.edx.support.PenRequestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//import javax.transaction.Transactional;


public class SecureExchangeDocumentServiceTests extends BasePenRequestAPITest {

  @Autowired
  DocumentService service;

  @Autowired
  private DocumentRepository repository;

  @Autowired
  private secureExchangeRequestRepository secureExchangeRequestRepository;

  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;

  private SecureExchangeDocumentEntity bcscPhoto;

  private SecureExchangeEntity penRequest;

  private UUID penRequestID;

  @Before
  public void setUp() {
    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);
    this.penRequest = new PenRequestBuilder()
            .withoutPenRequestID().build();
    this.bcscPhoto = new DocumentBuilder()
            .withoutDocumentID()
            .withPenRequest(this.penRequest)
            .build();
    this.penRequest = this.secureExchangeRequestRepository.save(this.penRequest);
    this.bcscPhoto = this.repository.save(this.bcscPhoto);
    this.penRequestID = this.penRequest.getSecureExchangeID();
  }

  @Test
  public void createValidDocumentTest() {
    SecureExchangeDocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            .withoutCreateAndUpdateUser()
            .build();
    document = this.service.createDocument(this.penRequestID, document);

    assertThat(document).isNotNull();
    assertThat(document.getSecureExchangeDocumentID()).isNotNull();
    assertThat(document.getSecureExchange().getSecureExchangeID()).isEqualTo(this.penRequestID);
  }

  @Test
  public void retrieveDocumentMetadataTest() {
    final SecureExchangeDocumentEntity retrievedDocument = this.service.retrieveDocumentMetadata(this.penRequestID, this.bcscPhoto.getSecureExchangeDocumentID());
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getSecureExchangeDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getSecureExchange().getSecureExchangeID()).isEqualTo(this.penRequestID);
  }

  @Test
  public void retrieveDocumentMetadataThrowsExceptionWhenInvalidDocumentIdGivenTest() {
    final UUID randomGuid =  UUID.randomUUID();
    assertThatThrownBy(() -> this.service.retrieveDocumentMetadata(this.penRequestID, randomGuid))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("DocumentEntity");
  }

  @Test
  public void retrieveDocumentMetadataThrowsExceptionWhenInvalidPenRequestIdGivenTest() {
    final UUID randomGuid = UUID.randomUUID();
    assertThatThrownBy(() -> this.service.retrieveDocumentMetadata(randomGuid, randomGuid))
        .isInstanceOf(EntityNotFoundException.class)
        .hasMessageContaining("DocumentEntity");
  }

  @Test
  public void retrieveDocumentDataTest() {
    final SecureExchangeDocumentEntity retrievedDocument = this.service.retrieveDocument(this.penRequestID, this.bcscPhoto.getSecureExchangeDocumentID(),"Y");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getSecureExchangeDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isEqualTo(this.bcscPhoto.getDocumentData());
  }

  @Test
  public void retrieveDocumentDataTest1() {
    final SecureExchangeDocumentEntity retrievedDocument = this.service.retrieveDocument(this.penRequestID, this.bcscPhoto.getSecureExchangeDocumentID(),"TRUE");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getSecureExchangeDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isEqualTo(this.bcscPhoto.getDocumentData());
  }

  @Test
  public void retrieveDocumentDataTest2() {
    final SecureExchangeDocumentEntity retrievedDocument = this.service.retrieveDocument(this.penRequestID, this.bcscPhoto.getSecureExchangeDocumentID(),"N");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getSecureExchangeDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isNull();
  }

  @Test
  public void retrieveAllDocumentMetadataTest() {
    final SecureExchangeDocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            .withoutCreateAndUpdateUser()
            .withPenRequest(this.penRequest)
            .build();
    this.repository.save(document);

    final List<SecureExchangeDocumentEntity> documents = this.service.retrieveAllDocumentMetadata(this.penRequestID);
    assertThat(documents.size()).isEqualTo(2);
  }


  @Test
  public void deleteDocumentTest() {
    final SecureExchangeDocumentEntity deletedDocument = this.service.deleteDocument(this.penRequestID, this.bcscPhoto.getSecureExchangeDocumentID());
    assertThat(deletedDocument).isNotNull();
    final UUID guid =  this.bcscPhoto.getSecureExchangeDocumentID();
    assertThatThrownBy(() -> this.service.retrieveDocumentMetadata(this.penRequestID, guid))
            .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  public void deleteDocumentThrowsExceptionWhenInvalidIdGivenTest() {
    final UUID guid =  UUID.randomUUID();
    assertThatThrownBy(() -> this.service.deleteDocument(this.penRequestID, guid))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("DocumentEntity");
  }
}
