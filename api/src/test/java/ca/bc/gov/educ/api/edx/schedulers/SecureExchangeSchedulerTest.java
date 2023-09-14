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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

  @BeforeEach
  public void setUp() throws IOException {
    LockAssert.TestHelper.makeAllAssertsPass(true);
    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);

    final SecureExchangeEntity secureExchange = new SecureExchangeBuilder().withSecureExchangeStatusCode("CLOSED")
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

  @AfterEach
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
  @Test
  public void testPurgeClosedMessages_GivenMessageIsOlderThanGivenTime_MessageWillBeDeleted() throws IOException {
    val results = this.repository.findAll();
    assertThat(results).size().isEqualTo(1);
    assertThat(results.get(0)).isNotNull();

    final SecureExchangeEntity secureExchange = new SecureExchangeBuilder().withSecureExchangeStatusCode("CLOSED")
            .withoutSecureExchangeID().build();
    secureExchange.setStatusUpdateDate(LocalDateTime.now().minusHours(25));
    secureExchange.setCreateDate(LocalDateTime.now().minusDays(366));
    final SecureExchangeDocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            .withData(Files.readAllBytes(new ClassPathResource(
                    "../model/document-req.json", SecureExchangeSchedulerTest.class).getFile().toPath()))
            .withSecureExchange(secureExchange)
            .withTypeCode("CAPASSPORT")
            .build();
    this.secureExchangeRequestRepository.save(secureExchange);
    document.setCreateDate(LocalDateTime.now().minusDays(366));
    this.repository.save(document);

    val results1 = this.repository.findAll();
    assertThat(results1).size().isEqualTo(2);
    assertThat(results1.get(0)).isNotNull();

    this.secureExchangeScheduler.purgeClosedMessages();
    val resultsAfterPurge = this.repository.findAll();
    assertThat(resultsAfterPurge).size().isEqualTo(1);

  }
}
