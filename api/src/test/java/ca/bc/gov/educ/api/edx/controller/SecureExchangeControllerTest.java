package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeController;
import ca.bc.gov.educ.api.edx.filter.FilterOperation;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeStatusCodeTableRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SearchCriteria;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.ValueType;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecureExchangeControllerTest extends BaseSecureExchangeControllerTest {

  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  SecureExchangeController controller;


  @Autowired
  SecureExchangeRequestRepository repository;

  @Autowired
  DocumentRepository documentRepository;

  @Autowired
  SecureExchangeStatusCodeTableRepository secureExchangeStatusCodeTableRepo;

  @Autowired
  MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void after() {
    this.documentRepository.deleteAll();
    this.repository.deleteAll();
  }


  @Test
  public void testRetrieveSecureExchange_GivenRandomID_ShouldThrowEntityNotFoundException() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" + UUID.randomUUID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
            .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testRetrieveSecureExchange_GivenValidID_ShouldReturnOkStatus() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" + entity.getSecureExchangeID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.secureExchangeID").value(entity.getSecureExchangeID().toString()));
  }

  @Test
  public void testFindSecureExchange_GivenOnlyPenInQueryParam_ShouldReturnOkStatusAndEntities() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/?secureExchangeID" + entity.getSecureExchangeID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(MockMvcResultMatchers.jsonPath("$[0].secureExchangeID").value(entity.getSecureExchangeID().toString()));
  }

  @Test
  public void testRetrieveSecureExchange_GivenRandomEDXUserIDAndStatusCode_ShouldReturnOkStatus() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/?edxUserID=" + UUID.randomUUID() + "&status=" + "INT")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }


  @Test
  public void testCreateSecureExchange_GivenValidCommentPayload_ShouldReturnStatusCreated() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
      .contentType(APPLICATION_JSON)
      .accept(APPLICATION_JSON).content(this.dummySecureExchangeNoCreateUpdateDateJsonWithMinAndComment(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testCreateSecureExchange_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeNoCreateUpdateDateJsonWithMin(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testCreateSecureExchange_GivenPenReqIdInPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithInvalidPenReqID())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateSecureExchange_LowercaseEmailVerifiedFlag_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithInvalidEmailVerifiedFlag())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdateSecureExchange_GivenInvalidPenReqIDInPayload_ShouldReturnStatusNotFound() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithInvalidPenReqIDWithMinOwner(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testUpdateSecureExchange_GivenValidPenReqIDInPayload_ShouldReturnStatusOk() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithValidPenReqID(penReqId, ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testUpdateSecureExchange_GivenInvalidDemogChangedInPayload_ShouldReturnBadRequest() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithInvalidDemogChanged(penReqId))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testDeleteSecureExchange_GivenInvalidId_ShouldReturn404() throws Exception {
    this.mockMvc.perform(delete(URL.BASE_URL_SECURE_EXCHANGE+"/" + UUID.randomUUID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testDeleteSecureExchange_GivenValidId_ShouldReturn204() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(delete(URL.BASE_URL_SECURE_EXCHANGE+"/" + penReqId)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  public void testDeleteSecureExchange_GivenValidIdWithAssociations_ShouldReturn204() throws Exception {
    final SecureExchangeEntity secureExchangeEntity = mapper.toModel(this.getSecureExchangeEntityFromJsonString());
    secureExchangeEntity.setSecureExchangeComment(this.createSecureExchangeComments(secureExchangeEntity));
    final SecureExchangeEntity entity = this.repository.save(secureExchangeEntity);
    final SecureExchangeDocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            //.withoutCreateAndUpdateUser()
            .withSecureExchange(entity)
            .withTypeCode("CAPASSPORT")
            .build();
    this.documentRepository.save(document);
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(delete(URL.BASE_URL_SECURE_EXCHANGE+"/" + penReqId)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  private Set<SecureExchangeCommentEntity> createSecureExchangeComments(final SecureExchangeEntity secureExchange) {
    final Set<SecureExchangeCommentEntity> commentsEntitySet = new HashSet<>();
    final SecureExchangeCommentEntity secureExchangeComment = new SecureExchangeCommentEntity();
    secureExchangeComment.setSecureExchangeEntity(secureExchange);
    secureExchangeComment.setCommentUserName("hi");
    secureExchangeComment.setCreateDate(LocalDateTime.now());
    secureExchangeComment.setUpdateDate(LocalDateTime.now());
    secureExchangeComment.setUpdateUser("TEST");
    secureExchangeComment.setCreateUser("TEST");
    secureExchangeComment.setContent("HELLO");
    commentsEntitySet.add(secureExchangeComment);
    return commentsEntitySet;
  }

  @Test
  public void testReadSecureExchangeStatus_Always_ShouldReturnStatusOkAndAllDataFromDB() throws Exception {
    this.secureExchangeStatusCodeTableRepo.save(this.createPenReqStatus());
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+URL.STATUSES)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE_STATUSES"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void testReadSecureExchangePaginated_Always_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_secure_exchanges.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL_SECURE_EXCHANGE+URL.PAGINATED+"?pageSize=2")
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE")))
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  public void testReadSecureExchangePaginated_whenNoDataInDB_ShouldReturnStatusOk() throws Exception {
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL_SECURE_EXCHANGE+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE")))
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  public void testReadSecureExchangePaginated_ministryOwnershipTeamID_ShouldReturnStatusOk() throws Exception {
    final var file = new File(
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_secure_exchanges.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("ministryOwnershipTeamID").operation(FilterOperation.EQUAL).value("ef4bd106-32a6-402f-9dd8-cf70b69a5ecb").valueType(ValueType.UUID).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final var objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
        .perform(get(URL.BASE_URL_SECURE_EXCHANGE+URL.PAGINATED)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE")))
                .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadSecureExchangePaginated_givenOperationTypeNull_ShouldReturnStatusOk() throws Exception {
    final var file = new File(
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_secure_exchanges.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("edxUserID").operation(null).value("fdf94a22-51e3-4816-8665-9f8571af1be4").valueType(ValueType.UUID).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final var objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+URL.PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE")))
            .param("searchCriteriaList", criteriaJSON)
        .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }

  private ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity createPenReqStatus() {
    final ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity entity = new ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity();
    entity.setSecureExchangeStatusCode("INITREV");
    entity.setDescription("Initial Review");
    entity.setDisplayOrder(1);
    entity.setEffectiveDate(LocalDateTime.now());
    entity.setLabel("Initial Review");
    entity.setCreateDate(LocalDateTime.now());
    entity.setCreateUser("TEST");
    entity.setUpdateUser("TEST");
    entity.setUpdateDate(LocalDateTime.now());
    entity.setExpiryDate(LocalDateTime.from(new GregorianCalendar(2099, Calendar.FEBRUARY, 1).toZonedDateTime()));
    return entity;
  }

  private MinistryOwnershipTeamEntity getMinistryOwnershipTeam(){
    MinistryOwnershipTeamEntity entity = new MinistryOwnershipTeamEntity();
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    entity.setUpdateUser("JACK");
    entity.setCreateUser("JACK");
    entity.setUpdateDate(LocalDateTime.now());
    entity.setTeamName("JOHN");
    entity.setGroupRoleIdentifier("ABC");
    return entity;
  }

  private String dummySecureExchangeCommentsJsonWithValidPenReqID(final String penReqId) {
    return "{\n" +
            "  \"penRetrievalRequestID\": \"" + penReqId + "\",\n" +
            "  \"content\": \"" + "comment1" + "\",\n" +
            "}";
  }

}
