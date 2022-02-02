package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.config.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeCommentsController;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestCommentRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecureExchangeCommentsControllerTest extends BaseSecureExchangeControllerTest {
    private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    SecureExchangeCommentsController controller;
    @Autowired
    SecureExchangeRequestRepository secureExchangeRequestRepository;
    @Autowired
    SecureExchangeRequestCommentRepository repository;

    @BeforeClass
    public static void beforeClass() {

    }

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @After
    public void after() {
      this.repository.deleteAll();
    }

    @Test
    public void testRetrieveSecureExchangeComments_GivenInvalidPenReqID_ShouldReturnStatusNotFound() throws Exception {
        this.mockMvc.perform(get(URL.BASE_URL+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS,UUID.randomUUID())
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    public void testRetrieveSecureExchangeComments_GivenValidPenReqID_ShouldReturnStatusOk() throws Exception {
        final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        final String penReqId = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(get(URL.BASE_URL+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS, penReqId )
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testCreateSecureExchangeComments_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
        final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        final String penReqId = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(post(URL.BASE_URL+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS,penReqId )
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(this.dummySecureExchangeCommentsJsonWithValidPenReqID(penReqId))).andDo(print()).andExpect(status().isCreated());
    }

    @Test
    public void testCreateSecureExchangeComments_GivenInvalidPenReqId_ShouldReturnStatusNotFound() throws Exception {
        final String penReqId = UUID.randomUUID().toString();
        this.mockMvc.perform(post(URL.BASE_URL+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS,penReqId)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(this.dummySecureExchangeCommentsJsonWithValidPenReqID(penReqId))).andDo(print()).andExpect(status().isNotFound());
    }

    private String dummySecureExchangeCommentsJsonWithValidPenReqID(final String penReqId) {
        return "{\n" +
                "  \"penRetrievalRequestID\": \"" + penReqId + "\",\n" +
                "  \"content\": \"" + "comment1" + "\",\n" +
                "}";
    }
}
