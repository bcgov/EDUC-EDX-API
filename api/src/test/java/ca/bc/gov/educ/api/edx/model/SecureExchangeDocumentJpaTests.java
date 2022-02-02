package ca.bc.gov.educ.api.edx.model;


import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
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

    private SecureExchangeEntity secureExchange;

    @Before
    public void setUp() {
        this.secureExchange = new SecureExchangeBuilder()
                                            .withoutSecureExchangeID().build();
        this.document = new DocumentBuilder()
                            .withoutDocumentID()
                            .withSecureExchange(this.secureExchange).build();

        this.entityManager.persist(this.secureExchange);
        this.entityManager.persist(this.document);
        this.entityManager.flush();
        //document = this.repository.save(document);
        this.entityManager.clear();
    }

    @Test
    public void findDocumentTest() {
        Optional<SecureExchangeDocumentEntity> myDocument = this.repository.findById(this.document.getDocumentID());
        assertThat(myDocument).isPresent();
        assertThat(myDocument.get().getDocumentTypeCode()).isEqualTo("BCSCPHOTO");
    }

    @Test
    public void saveDocumentTest() {
        SecureExchangeDocumentEntity myDocument = new DocumentBuilder()
                                        .withoutDocumentID()
                                        .withSecureExchange(this.secureExchange).build();
        SecureExchangeDocumentEntity savedDocument = this.repository.save(myDocument);
        assertThat(savedDocument.getDocumentID()).isNotEqualTo(this.document.getDocumentID());
        assertThat(savedDocument.getSecureExchange()).isNotNull();

        assertThat(this.repository.findById(savedDocument.getDocumentID())).isPresent();
    }

    @Test
    public void findDocumentBySecureExchangeTest() {
        SecureExchangeDocumentEntity myDocument = new DocumentBuilder()
                                        .withoutDocumentID()
                                        .withSecureExchange(this.secureExchange).build();
        SecureExchangeDocumentEntity savedDocument = this.repository.save(myDocument);
        assertThat(savedDocument.getDocumentID()).isNotEqualTo(this.document.getDocumentID());

        assertThat(this.repository.findBySecureExchangeSecureExchangeID(this.secureExchange.getSecureExchangeID()).size()).isEqualTo(2);
    }

    @Test
    public void deleteDocumentTest() {
        this.repository.deleteById(this.document.getDocumentID());
        assertThat(this.repository.findById(this.document.getDocumentID())).isEmpty();
    }
}
