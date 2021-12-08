package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import ca.bc.gov.educ.api.edx.model.v1.DocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.DocumentTypeCodeBuilder;
import ca.bc.gov.educ.api.edx.support.PenRequestBuilder;
import lombok.val;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class PenRequestSchedulerTest extends BasePenRequestAPITest {
  @Autowired
  private DocumentRepository repository;
  @Autowired
  private PenRequestScheduler penRequestScheduler;

  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;
  @Autowired
  private PenRequestRepository penRequestRepository;

  @Before
  public void setUp() throws IOException {
    LockAssert.TestHelper.makeAllAssertsPass(true);
    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);

    final PenRequestEntity penRequest = new PenRequestBuilder().withPenRequestStatusCode("MANUAL")
        .withoutPenRequestID().build();
    penRequest.setStatusUpdateDate(LocalDateTime.now().minusHours(25));
    final DocumentEntity document = new DocumentBuilder()
        .withoutDocumentID()
        .withData(Files.readAllBytes(new ClassPathResource(
            "../model/document-req.json", PenRequestSchedulerTest.class).getFile().toPath()))
        .withPenRequest(penRequest)
        .withTypeCode("CAPASSPORT")
        .build();
    this.penRequestRepository.save(penRequest);
    document.setCreateDate(LocalDateTime.now().minusDays(5));
    this.repository.save(document);
  }

  @After
  public void after() {
    this.repository.deleteAll();
  }

  @Test
  public void removeBlobContentsFromUploadedDocuments() {
    this.penRequestScheduler.removeBlobContentsFromUploadedDocuments();
    val results = this.repository.findAll();
    assertThat(results).size().isEqualTo(1);
    assertThat(results.get(0)).isNotNull();
    assertThat(results.get(0).getDocumentTypeCode()).isNotBlank();
    assertThat(results.get(0).getFileSize()).isZero();
    val doc = this.penRequestAPITestUtils.getDocumentBlobByDocumentID(results.get(0).getDocumentID());
    assertThat(doc).isNull();
  }
}
