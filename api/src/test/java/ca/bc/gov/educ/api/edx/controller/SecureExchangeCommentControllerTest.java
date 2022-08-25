package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeCommentController;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestCommentRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecureExchangeCommentControllerTest extends BaseSecureExchangeControllerTest {
    private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    SecureExchangeCommentController controller;
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
        this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS,UUID.randomUUID())
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE_COMMENT"))))
                .andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    public void testRetrieveSecureExchangeComments_GivenValidPenReqID_ShouldReturnStatusOk() throws Exception {
        final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        final String penReqId = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS, penReqId )
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE_COMMENT"))))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testCreateSecureExchangeComments_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
        final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        final String secureExchangeID = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS,secureExchangeID )
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_COMMENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(this.dummySecureExchangeCommentsJsonWithValidSecureExchangeId(secureExchangeID))).andDo(print()).andExpect(status().isCreated());

        val updatedSecureExchangeEntity = this.secureExchangeRequestRepository.findById(entity.getSecureExchangeID()).get();
        assertThat(updatedSecureExchangeEntity.getIsReadByMinistry(), equalTo(true));
        assertThat(updatedSecureExchangeEntity.getIsReadByExchangeContact(), equalTo(false));

    }

    @Test
    public void testCreateSecureExchangeComments_GivenValidPayloadWithEdxUserId_ShouldReturnStatusCreated() throws Exception {
        final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        final String secureExchangeID = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS,secureExchangeID )
          .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_COMMENT")))
          .contentType(MediaType.APPLICATION_JSON)
          .accept(MediaType.APPLICATION_JSON).content(this.dummySecureExchangeCommentsJsonWithValidSecureExchangeIdAndEdxUserId(secureExchangeID))).andDo(print()).andExpect(status().isCreated());

        val updatedSecureExchangeEntity = this.secureExchangeRequestRepository.findById(entity.getSecureExchangeID()).get();
        assertThat(updatedSecureExchangeEntity.getIsReadByMinistry(), equalTo(false));
        assertThat(updatedSecureExchangeEntity.getIsReadByExchangeContact(), equalTo(true));

    }

    @Test
    public void testCreateSecureExchangeComments_GivenInvalidPenReqId_ShouldReturnStatusNotFound() throws Exception {
        final String penReqId = UUID.randomUUID().toString();
        this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_COMMENTS,penReqId)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_COMMENT")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(this.dummySecureExchangeCommentsJsonWithValidSecureExchangeId(penReqId))).andDo(print()).andExpect(status().isNotFound());
    }

    private String dummySecureExchangeCommentsJsonWithValidSecureExchangeId(final String secureExchangeID) {
      return "{\n" +
        "  \"secureExchangeID\": \"" + secureExchangeID + "\",\n" +
        "  \"content\": \"" + "comment1" + "\",\n" +
        "  \"commentUserName\": \"" + "user1" + "\",\n" +
        "  \"createUser\": \"" + "user1" + "\",\n" +
        "  \"updateUser\": \"" + "user1" + "\",\n" +
        "  \"staffUserIdentifier\": \"" + UUID.randomUUID() + "\",\n" +
        "  \"commentTimestamp\": \"2020-02-09T00:00:00\"\n" +
        "}";
    }

    private String dummySecureExchangeCommentsJsonWithValidSecureExchangeIdAndEdxUserId(final String secureExchangeID) {
        return "{\n" +
          "  \"secureExchangeID\": \"" + secureExchangeID + "\",\n" +
          "  \"content\": \"" + "comment1" + "\",\n" +
          "  \"commentUserName\": \"" + "user1" + "\",\n" +
          "  \"createUser\": \"" + "user1" + "\",\n" +
          "  \"updateUser\": \"" + "user1" + "\",\n" +
          "  \"edxUserID\": \"" + UUID.randomUUID() + "\",\n" +
          "  \"commentTimestamp\": \"2020-02-09T00:00:00\"\n" +
          "}";
    }
}
