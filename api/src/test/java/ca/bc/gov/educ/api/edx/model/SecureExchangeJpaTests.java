package ca.bc.gov.educ.api.edx.model;

import static org.assertj.core.api.Assertions.assertThat;



import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;

@RunWith(SpringRunner.class)
@DataJpaTest
public class SecureExchangeJpaTests {
    @Autowired
    private SecureExchangeRequestRepository repository;

    private SecureExchangeEntity secureExchange;

    @Before
    public void setUp() {
        this.secureExchange = new SecureExchangeBuilder()
                            .withoutSecureExchangeID().build();
    }

    @Test
    public void saveDocumentTest() {
        SecureExchangeEntity savedSecureExchange = this.repository.save(this.secureExchange);
        assertThat(savedSecureExchange.getSecureExchangeID()).isNotNull();

        assertThat(this.repository.findById(savedSecureExchange.getSecureExchangeID())).isPresent();
    }

}
