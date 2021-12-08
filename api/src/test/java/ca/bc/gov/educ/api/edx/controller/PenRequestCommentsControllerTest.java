package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.PenRequestCommentsController;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.repository.PenRequestCommentRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
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

public class PenRequestCommentsControllerTest extends BasePenReqControllerTest {
    private static final PenRequestEntityMapper mapper = PenRequestEntityMapper.mapper;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    PenRequestCommentsController controller;
    @Autowired
    PenRequestRepository penRequestRepository;
    @Autowired
    PenRequestCommentRepository repository;

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
    public void testRetrievePenRequestComments_GivenInvalidPenReqID_ShouldReturnStatusNotFound() throws Exception {
        this.mockMvc.perform(get(URL.BASE_URL+"/" +URL.PEN_REQUEST_ID_COMMENTS,UUID.randomUUID())
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST"))))
                .andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    public void testRetrievePenRequestComments_GivenValidPenReqID_ShouldReturnStatusOk() throws Exception {
        final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
        final String penReqId = entity.getPenRequestID().toString();
        this.mockMvc.perform(get(URL.BASE_URL+"/" +URL.PEN_REQUEST_ID_COMMENTS, penReqId )
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST"))))
                .andDo(print()).andExpect(status().isOk());
    }

    @Test
    public void testCreatePenRequestComments_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
        final PenRequestEntity entity = this.penRequestRepository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
        final String penReqId = entity.getPenRequestID().toString();
        this.mockMvc.perform(post(URL.BASE_URL+"/" +URL.PEN_REQUEST_ID_COMMENTS,penReqId )
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(this.dummyPenRequestCommentsJsonWithValidPenReqID(penReqId))).andDo(print()).andExpect(status().isCreated());
    }

    @Test
    public void testCreatePenRequestComments_GivenInvalidPenReqId_ShouldReturnStatusNotFound() throws Exception {
        final String penReqId = UUID.randomUUID().toString();
        this.mockMvc.perform(post(URL.BASE_URL+"/" +URL.PEN_REQUEST_ID_COMMENTS,penReqId)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).content(this.dummyPenRequestCommentsJsonWithValidPenReqID(penReqId))).andDo(print()).andExpect(status().isNotFound());
    }

    private String dummyPenRequestCommentsJsonWithValidPenReqID(final String penReqId) {
        return "{\n" +
                "  \"penRetrievalRequestID\": \"" + penReqId + "\",\n" +
                "  \"commentContent\": \"" + "comment1" + "\",\n" +
                "  \"commentTimestamp\": \"2020-02-09T00:00:00\"\n" +
                "}";
    }
}
