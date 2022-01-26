package ca.bc.gov.educ.api.edx.model;

import static org.assertj.core.api.Assertions.assertThat;



import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.edx.repository.secureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.support.PenRequestBuilder;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PenRequestJpaTests {
    @Autowired
    private secureExchangeRequestRepository repository;

    private SecureExchangeEntity penRequest;

    @Before
    public void setUp() {
        this.penRequest = new PenRequestBuilder()
                            .withoutPenRequestID().build();
    }

    @Test
    public void saveDocumentTest() {
        SecureExchangeEntity savedPenRequest = this.repository.save(this.penRequest);
        assertThat(savedPenRequest.getSecureExchangeID()).isNotNull();
        assertThat(savedPenRequest.getInitialSubmitDate()).isNull();

        assertThat(this.repository.findById(savedPenRequest.getSecureExchangeID())).isPresent();
    }

}
