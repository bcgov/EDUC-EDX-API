package ca.bc.gov.educ.api.edx.model;

import static org.assertj.core.api.Assertions.assertThat;


import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
import ca.bc.gov.educ.api.edx.support.PenRequestBuilder;

@RunWith(SpringRunner.class)
@DataJpaTest
public class PenRequestJpaTests {
    @Autowired
    private PenRequestRepository repository;

    private PenRequestEntity penRequest;

    @Before
    public void setUp() {
        this.penRequest = new PenRequestBuilder()
                            .withoutPenRequestID().build();
    }

    @Test
    public void saveDocumentTest() {
        PenRequestEntity savedPenRequest = this.repository.save(this.penRequest);
        assertThat(savedPenRequest.getPenRequestID()).isNotNull();
        assertThat(savedPenRequest.getInitialSubmitDate()).isNull();

        assertThat(this.repository.findById(savedPenRequest.getPenRequestID())).isPresent();
    }

}
