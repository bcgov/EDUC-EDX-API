package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.DocumentTypeCodeBuilder;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
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

public class SecureExchangeSchedulerTest extends BaseSecureExchangeAPITest {
  @Autowired
  private DocumentRepository repository;
  @Autowired
  private SecureExchangeScheduler secureExchangeScheduler;

  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;
  @Autowired
  private SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Before
  public void setUp() throws IOException {
    LockAssert.TestHelper.makeAllAssertsPass(true);
    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);

    final SecureExchangeEntity secureExchange = new SecureExchangeBuilder().withSecureExchangeStatusCode("MANUAL")
        .withoutSecureExchangeID().build();
    secureExchange.setStatusUpdateDate(LocalDateTime.now().minusHours(25));
    final SecureExchangeDocumentEntity document = new DocumentBuilder()
        .withoutDocumentID()
        .withData(Files.readAllBytes(new ClassPathResource(
            "../model/document-req.json", SecureExchangeSchedulerTest.class).getFile().toPath()))
        .withSecureExchange(secureExchange)
        .withTypeCode("CAPASSPORT")
        .build();
    this.secureExchangeRequestRepository.save(secureExchange);
    document.setCreateDate(LocalDateTime.now().minusDays(5));
    this.repository.save(document);
  }

  @After
  public void after() {
    this.repository.deleteAll();
  }

  @Test
  public void removeBlobContentsFromUploadedDocuments() {
    this.secureExchangeScheduler.removeBlobContentsFromUploadedDocuments();
    val results = this.repository.findAll();
    assertThat(results).size().isEqualTo(1);
    assertThat(results.get(0)).isNotNull();
    assertThat(results.get(0).getDocumentTypeCode()).isNotBlank();
    assertThat(results.get(0).getFileSize()).isZero();
    val doc = this.secureExchangeAPITestUtils.getDocumentBlobByDocumentID(results.get(0).getDocumentID());
    assertThat(doc).isNull();
  }
}
