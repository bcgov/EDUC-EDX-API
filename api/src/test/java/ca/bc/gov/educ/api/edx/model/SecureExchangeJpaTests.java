package ca.bc.gov.educ.api.edx.model;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

//@RunWith(SpringRunner.class)
//@DataJpaTest
class SecureExchangeJpaTests {
//    @Autowired
//    private SecureExchangeRequestRepository repository;

//    private SecureExchangeEntity secureExchange;
//
//    @BeforeEach
//    public void setUp() {
//        this.secureExchange = new SecureExchangeBuilder()
//                            .withoutSecureExchangeID().build();
//    }

//    @Test
//    public void saveDocumentTest() {
//        SecureExchangeEntity savedSecureExchange = this.repository.save(this.secureExchange);
//        assertThat(savedSecureExchange.getSecureExchangeID()).isNotNull();
//
//        assertThat(this.repository.findById(savedSecureExchange.getSecureExchangeID())).isPresent();
//    }

}
