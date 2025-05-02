package ca.bc.gov.educ.api.edx.rest;

import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.struct.gradschool.v1.GradSchool;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

class RestUtilsTest extends BaseEdxAPITest {

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient chesWebClient;

    @Mock
    private MessagePublisher messagePublisher;

    @InjectMocks
    private RestUtils restUtils;

    @Mock
    private ApplicationProperties props;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        restUtils = spy(new RestUtils(chesWebClient, webClient, props, messagePublisher));
    }

    @Test
    void testPopulateGradSchoolMap() {
        var school1= UUID.randomUUID().toString();
        List<GradSchool> mockSchools = List.of(
                new GradSchool(null, school1, "A", "Y", "Y"),
                new GradSchool(null, UUID.randomUUID().toString(), "A", "Y", "Y")
        );

        doReturn(mockSchools).when(restUtils).getGradSchools();
        assertTrue(restUtils.getGradSchoolBySchoolID(school1).isPresent());
    }
}
