package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.DocumentService;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.DocumentTypeCodeBuilder;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

//import javax.transaction.Transactional;


public class SecureExchangeDocumentServiceTests extends BaseSecureExchangeAPITest {

  @Autowired
  DocumentService service;

  @Autowired
  private DocumentRepository repository;

  @Autowired
  private SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;

  private SecureExchangeDocumentEntity bcscPhoto;

  private SecureExchangeEntity secureExchange;

  private UUID secureExchangeID;

  @Before
  public void setUp() {
    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);
    this.secureExchange = new SecureExchangeBuilder()
            .withoutSecureExchangeID().build();
    this.bcscPhoto = new DocumentBuilder()
            .withoutDocumentID()
            .withSecureExchange(this.secureExchange)
            .build();
    this.secureExchange = this.secureExchangeRequestRepository.save(this.secureExchange);
    this.bcscPhoto = this.repository.save(this.bcscPhoto);
    this.secureExchangeID = this.secureExchange.getSecureExchangeID();
  }

  @Test
  public void createValidDocumentTest() {
    SecureExchangeDocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            .build();
    document = this.service.createDocument(this.secureExchangeID, document);

    assertThat(document).isNotNull();
    assertThat(document.getDocumentID()).isNotNull();
    assertThat(document.getSecureExchangeEntity().getSecureExchangeID()).isEqualTo(this.secureExchangeID);
  }

  @Test
  public void createValidDocumentCheckUnreadStatusExchangeContactTest() {
    SecureExchangeDocumentEntity document = new DocumentBuilder()
      .withoutDocumentID()
      .build();
    document = this.service.createDocument(this.secureExchangeID, document);

    assertThat(document).isNotNull();
    assertThat(document.getDocumentID()).isNotNull();
    assertThat(document.getSecureExchangeEntity().getSecureExchangeID()).isEqualTo(this.secureExchangeID);
    assertThat(document.getSecureExchangeEntity().getIsReadByExchangeContact()).isTrue();
    assertThat(document.getSecureExchangeEntity().getIsReadByMinistry()).isFalse();
  }

  @Test
  public void createValidDocumentCheckUnreadStatusMinContactTest() {
    SecureExchangeDocumentEntity document = new DocumentBuilder()
      .withoutDocumentID()
      .build();
    document.setEdxUserID(null);
    document.setStaffUserIdentifier("BILLY");
    document = this.service.createDocument(this.secureExchangeID, document);

    assertThat(document).isNotNull();
    assertThat(document.getDocumentID()).isNotNull();
    assertThat(document.getSecureExchangeEntity().getSecureExchangeID()).isEqualTo(this.secureExchangeID);
    assertThat(document.getSecureExchangeEntity().getIsReadByExchangeContact()).isTrue();
    assertThat(document.getSecureExchangeEntity().getIsReadByMinistry()).isFalse();
  }

  @Test
  public void retrieveDocumentMetadataTest() {
    final SecureExchangeDocumentEntity retrievedDocument = this.service.retrieveDocumentMetadata(this.secureExchangeID, this.bcscPhoto.getDocumentID());
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getSecureExchangeEntity().getSecureExchangeID()).isEqualTo(this.secureExchangeID);
  }

  @Test
  public void retrieveDocumentMetadataThrowsExceptionWhenInvalidDocumentIdGivenTest() {
    final UUID randomGuid =  UUID.randomUUID();
    assertThatThrownBy(() -> this.service.retrieveDocumentMetadata(this.secureExchangeID, randomGuid))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("DocumentEntity");
  }

  @Test
  public void retrieveDocumentMetadataThrowsExceptionWhenInvalidSecureExchangeIdGivenTest() {
    final UUID randomGuid =  UUID.randomUUID();
    final var docID = this.bcscPhoto.getDocumentID();
    assertThatThrownBy(() -> this.service.retrieveDocumentMetadata(randomGuid, docID))
      .isInstanceOf(EntityNotFoundException.class)
      .hasMessageContaining("DocumentEntity");
  }

  @Test
  public void retrieveDocumentDataTest() {
    final SecureExchangeDocumentEntity retrievedDocument = this.service.retrieveDocument(this.secureExchangeID, this.bcscPhoto.getDocumentID(),"Y");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isEqualTo(this.bcscPhoto.getDocumentData());
  }

  @Test
  public void retrieveDocumentDataTest1() {
    final SecureExchangeDocumentEntity retrievedDocument = this.service.retrieveDocument(this.secureExchangeID, this.bcscPhoto.getDocumentID(),"TRUE");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isEqualTo(this.bcscPhoto.getDocumentData());
  }

  @Test
  public void retrieveDocumentDataTest2() {
    final SecureExchangeDocumentEntity retrievedDocument = this.service.retrieveDocument(this.secureExchangeID, this.bcscPhoto.getDocumentID(),"N");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isNull();
  }

  @Test
  public void retrieveAllDocumentMetadataTest() {
    final SecureExchangeDocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            .withSecureExchange(this.secureExchange)
            .build();
    this.repository.save(document);

    final List<SecureExchangeDocumentEntity> documents = this.service.retrieveAllDocumentMetadata(this.secureExchangeID);
    assertThat(documents).hasSize(2);
  }


  @Test
  public void deleteDocumentTest() {
    final SecureExchangeDocumentEntity deletedDocument = this.service.deleteDocument(this.secureExchangeID, this.bcscPhoto.getDocumentID());
    assertThat(deletedDocument).isNotNull();
    final UUID guid =  this.bcscPhoto.getDocumentID();
    assertThatThrownBy(() -> this.service.retrieveDocumentMetadata(this.secureExchangeID, guid))
            .isInstanceOf(EntityNotFoundException.class);
  }

  @Test
  public void deleteDocumentThrowsExceptionWhenInvalidIdGivenTest() {
    final UUID guid =  UUID.randomUUID();
    assertThatThrownBy(() -> this.service.deleteDocument(this.secureExchangeID, guid))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("DocumentEntity");
  }
}
