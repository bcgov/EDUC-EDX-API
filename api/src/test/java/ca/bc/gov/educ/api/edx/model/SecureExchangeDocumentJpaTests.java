package ca.bc.gov.educ.api.edx.model;


import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.PenRequestBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SecureExchangeDocumentJpaTests {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private DocumentRepository repository;

    private SecureExchangeDocumentEntity document;

    private SecureExchangeEntity penRequest;

    @Before
    public void setUp() {
        this.penRequest = new PenRequestBuilder()
                                            .withoutPenRequestID().build();
        this.document = new DocumentBuilder()
                            .withoutDocumentID()
                            .withPenRequest(this.penRequest).build();

        this.entityManager.persist(this.penRequest);
        this.entityManager.persist(this.document);
        this.entityManager.flush();
        //document = this.repository.save(document);
        this.entityManager.clear();
    }

    @Test
    public void findDocumentTest() {
        Optional<SecureExchangeDocumentEntity> myDocument = this.repository.findById(this.document.getSecureExchangeDocumentID());
        assertThat(myDocument).isPresent();
        assertThat(myDocument.get().getSecureExchangeDocumentTypeCode()).isEqualTo("BCSCPHOTO");
    }

    @Test
    public void saveDocumentTest() {
        SecureExchangeDocumentEntity myDocument = new DocumentBuilder()
                                        .withoutDocumentID()
                                        .withPenRequest(this.penRequest).build();
        SecureExchangeDocumentEntity savedDocument = this.repository.save(myDocument);
        assertThat(savedDocument.getSecureExchangeDocumentID()).isNotEqualTo(this.document.getSecureExchangeDocumentID());
        assertThat(savedDocument.getSecureExchange()).isNotNull();

        assertThat(this.repository.findById(savedDocument.getSecureExchangeDocumentID())).isPresent();
    }

    @Test
    public void findDocumentByPenRequestTest() {
        SecureExchangeDocumentEntity myDocument = new DocumentBuilder()
                                        .withoutDocumentID()
                                        .withPenRequest(this.penRequest).build();
        SecureExchangeDocumentEntity savedDocument = this.repository.save(myDocument);
        assertThat(savedDocument.getSecureExchangeDocumentID()).isNotEqualTo(this.document.getSecureExchangeDocumentID());

        assertThat(this.repository.findByPenRequestPenRequestID(this.penRequest.getSecureExchangeID()).size()).isEqualTo(2);
    }

    @Test
    public void deleteDocumentTest() {
        this.repository.deleteById(this.document.getSecureExchangeDocumentID());
        assertThat(this.repository.findById(this.document.getSecureExchangeDocumentID())).isEmpty();
    }
}
