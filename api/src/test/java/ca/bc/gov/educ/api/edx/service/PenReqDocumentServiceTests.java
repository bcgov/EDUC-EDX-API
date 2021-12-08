package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.DocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
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


public class PenReqDocumentServiceTests extends BasePenRequestAPITest {

  @Autowired
  DocumentService service;

  @Autowired
  private DocumentRepository repository;

  @Autowired
  private PenRequestRepository PenRequestRepository;

  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;

  private DocumentEntity bcscPhoto;

  private PenRequestEntity penRequest;

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
    this.penRequest = this.PenRequestRepository.save(this.penRequest);
    this.bcscPhoto = this.repository.save(this.bcscPhoto);
    this.penRequestID = this.penRequest.getPenRequestID();
  }

  @Test
  public void createValidDocumentTest() {
    DocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            .withoutCreateAndUpdateUser()
            .build();
    document = this.service.createDocument(this.penRequestID, document);

    assertThat(document).isNotNull();
    assertThat(document.getDocumentID()).isNotNull();
    assertThat(document.getPenRequest().getPenRequestID()).isEqualTo(this.penRequestID);
  }

  @Test
  public void retrieveDocumentMetadataTest() {
    final DocumentEntity retrievedDocument = this.service.retrieveDocumentMetadata(this.penRequestID, this.bcscPhoto.getDocumentID());
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getPenRequest().getPenRequestID()).isEqualTo(this.penRequestID);
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
    final DocumentEntity retrievedDocument = this.service.retrieveDocument(this.penRequestID, this.bcscPhoto.getDocumentID(),"Y");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isEqualTo(this.bcscPhoto.getDocumentData());
  }

  @Test
  public void retrieveDocumentDataTest1() {
    final DocumentEntity retrievedDocument = this.service.retrieveDocument(this.penRequestID, this.bcscPhoto.getDocumentID(),"TRUE");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isEqualTo(this.bcscPhoto.getDocumentData());
  }

  @Test
  public void retrieveDocumentDataTest2() {
    final DocumentEntity retrievedDocument = this.service.retrieveDocument(this.penRequestID, this.bcscPhoto.getDocumentID(),"N");
    assertThat(retrievedDocument).isNotNull();
    assertThat(retrievedDocument.getDocumentTypeCode()).isEqualTo("BCSCPHOTO");

    assertThat(retrievedDocument.getDocumentData()).isNull();
  }

  @Test
  public void retrieveAllDocumentMetadataTest() {
    final DocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            .withoutCreateAndUpdateUser()
            .withPenRequest(this.penRequest)
            .build();
    this.repository.save(document);

    final List<DocumentEntity> documents = this.service.retrieveAllDocumentMetadata(this.penRequestID);
    assertThat(documents.size()).isEqualTo(2);
  }


  @Test
  public void deleteDocumentTest() {
    final DocumentEntity deletedDocument = this.service.deleteDocument(this.penRequestID, this.bcscPhoto.getDocumentID());
    assertThat(deletedDocument).isNotNull();
    final UUID guid =  this.bcscPhoto.getDocumentID();
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
