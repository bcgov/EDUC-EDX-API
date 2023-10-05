package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeController;
import ca.bc.gov.educ.api.edx.filter.FilterOperation;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.SearchCriteria;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.ValueType;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.DocumentTypeCodeBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EdxControllerTest extends BaseEdxControllerTest {

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

  @Autowired
  SecureExchangeContactTypeCodeTableRepository secureExchangeContactTypeCodeTableRepository;

  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;

  @BeforeEach
  public void setUp() {
    this.secureExchangeContactTypeCodeTableRepository.save(createContactType());
    this.secureExchangeStatusCodeTableRepo.save(createNewStatus());
    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);
    MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void after() {
    this.secureExchangeContactTypeCodeTableRepository.deleteAll();
    this.secureExchangeStatusCodeTableRepo.deleteAll();
    this.documentRepository.deleteAll();
    this.documentTypeCodeRepository.deleteAll();
    this.repository.deleteAll();
  }


  @Test
  void testRetrieveSecureExchange_GivenRandomID_ShouldThrowEntityNotFoundException() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" + UUID.randomUUID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
            .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  void testRetrieveSecureExchange_GivenValidID_ShouldReturnOkStatus() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" + entity.getSecureExchangeID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.secureExchangeID").value(entity.getSecureExchangeID().toString()));
  }

  @Test
  void testFindSecureExchange_GivenOnlyPenInQueryParam_ShouldReturnOkStatusAndEntities() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" + entity.getSecureExchangeID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.secureExchangeID").value(entity.getSecureExchangeID().toString()));
  }

  @Test
  void testRetrieveSecureExchange_GetSecureExchangeStatusCode_ShouldReturnOkStatus() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/statuses")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE_CODES"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testCreateSecureExchange_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeNoCreateUpdateDateJsonWithMin(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print()).andExpect(status().isCreated());
  }

  @Test
  void testCreateSecureExchange_GivenValidPayloadWithStatusCode_ShouldReturnStatusCreated() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
      .contentType(APPLICATION_JSON)
      .accept(APPLICATION_JSON).content(this.dummySecureExchangeNoCreateUpdateDateJsonWithMinAndStatusCode(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print())
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.isReadByMinistry", is(true)))
      .andExpect(jsonPath("$.secureExchangeStatusCode", is("OPEN")));
  }

  @Test
  void testCreateSecureExchange_GivenValidPayloadWithStudentAdded_ShouldReturnStatusCreated() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
        .contentType(APPLICATION_JSON)
        .accept(APPLICATION_JSON).content(this.dummySecureExchangeNoCreateSecureExchangeWithCommentAndStudent(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print())
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.isReadByMinistry", is(false)))
      .andExpect(jsonPath("$.secureExchangeStatusCode", is("OPEN")))
     .andExpect(jsonPath("$.studentsList.[0].secureExchangeId", is(notNullValue())));
  }

  @Test
  void testCreateSecureExchange_GivenValidPayloadWithDocumentAdded_ShouldReturnStatusCreated() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
                    .contentType(APPLICATION_JSON)
                    .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithMinAndDocument(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print())
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.isReadByMinistry", is(false)))
            .andExpect(jsonPath("$.secureExchangeStatusCode", is("OPEN")))
            .andExpect(jsonPath("$.documentList.[0].documentID", is(notNullValue())));
  }


  @Test
  void testCreateSecureExchange_GivenPenReqIdInPayloadNoID_ShouldReturnStatusBadRequest() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
      .contentType(APPLICATION_JSON)
      .accept(APPLICATION_JSON).content(this.dummySecureExchangeNoCreateUpdateDateJsonWithMinNoID(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchange_GivenPenReqIdInPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithInvalidPenReqID())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchange_LowercaseEmailVerifiedFlag_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithInvalidEmailVerifiedFlag())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testClaimSecureExchange_ShouldReturnStatusNoContent() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));

    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + URL.CLAIM_ALL).param(
           "secureExchangeIDs", new String[]{String.valueOf(entity.getSecureExchangeID())}).param("reviewer", "TESTMIN")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
      .contentType(APPLICATION_JSON)
      .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  void testClaimSecureExchanges_ShouldReturnStatusNoContent() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    final SecureExchangeEntity entity2 = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));

    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + URL.CLAIM_ALL).param(
                    "secureExchangeIDs", new String[]{String.valueOf(entity.getSecureExchangeID()), String.valueOf(entity2.getSecureExchangeID())}).param("reviewer", "TESTMIN")
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
      .contentType(APPLICATION_JSON)
      .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  void testUpdateSecureExchange_GivenInvalidPenReqIDInPayload_ShouldReturnStatusNotFound() throws Exception {
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithInvalidPenReqIDWithMinOwner(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  void testUpdateSecureExchange_GivenValidPenReqIDInPayload_ShouldReturnStatusOk() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    MinistryOwnershipTeamEntity ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithValidPenReqID(penReqId, ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()))).andDo(print()).andExpect(status().isOk());
  }

  @Test
  void testUpdateSecureExchange_GivenInvalidDemogChangedInPayload_ShouldReturnBadRequest() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummySecureExchangeJsonWithInvalidDemogChanged(penReqId))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testDeleteSecureExchange_GivenInvalidId_ShouldReturn404() throws Exception {
    this.mockMvc.perform(delete(URL.BASE_URL_SECURE_EXCHANGE+"/" + UUID.randomUUID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  void testDeleteSecureExchange_GivenValidId_ShouldReturn204() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(delete(URL.BASE_URL_SECURE_EXCHANGE+"/" + penReqId)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_SECURE_EXCHANGE")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  void testDeleteSecureExchange_GivenValidIdWithAssociations_ShouldReturn204() throws Exception {
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
    secureExchangeComment.setCommentTimestamp(LocalDateTime.now());
    secureExchangeComment.setCreateDate(LocalDateTime.now());
    secureExchangeComment.setUpdateDate(LocalDateTime.now());
    secureExchangeComment.setUpdateUser("TEST");
    secureExchangeComment.setCreateUser("TEST");
    secureExchangeComment.setContent("HELLO");
    commentsEntitySet.add(secureExchangeComment);
    return commentsEntitySet;
  }

  @Test
  void testReadSecureExchangeStatus_Always_ShouldReturnStatusOkAndAllDataFromDB() throws Exception {
    this.secureExchangeStatusCodeTableRepo.save(this.createNewStatus());
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+URL.STATUSES)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE_CODES"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  void testReadSecureExchangePaginated_Always_ShouldReturnStatusOk() throws Exception {
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
  void testReadSecureExchangePaginated_whenNoDataInDB_ShouldReturnStatusOk() throws Exception {
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL_SECURE_EXCHANGE+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE")))
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }

  @Test
  void testReadSecureExchangePaginated_ministryOwnershipTeamID_ShouldReturnStatusOk() throws Exception {
    final var file = new File(
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_secure_exchanges.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("ministryOwnershipTeamID").operation(FilterOperation.EQUAL).value("ef4bd106-32a6-402f-9dd8-cf70b69a5ecb").valueType(ValueType.UUID).build();
    final SearchCriteria criteria2 = SearchCriteria.builder().key("isReadByMinistry").operation(FilterOperation.EQUAL).value("false").valueType(ValueType.BOOLEAN).build();

    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    criteriaList.add(criteria2);

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
  void testReadSecureExchangePaginated_ByOpenAndUnclaimed_ShouldReturnStatusOk() throws Exception {
    final var file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_secure_exchanges.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("ministryOwnershipTeamID").operation(FilterOperation.EQUAL).value("ef4bd106-32a6-402f-9dd8-cf70b69c5ecb").valueType(ValueType.UUID).build();
    final SearchCriteria criteria2 = SearchCriteria.builder().key("reviewer").operation(FilterOperation.EQUAL).value(null).valueType(ValueType.STRING).build();
    final SearchCriteria criteria3 = SearchCriteria.builder().key("secureExchangeStatusCode").operation(FilterOperation.IN).value("OPEN").valueType(ValueType.STRING).build();

    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    criteriaList.add(criteria2);
    criteriaList.add(criteria3);

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
  void testReadSecureExchangePaginated_givenOperationTypeNull_ShouldReturnStatusOk() throws Exception {
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

  private ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity createNewStatus() {
    final ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity entity = new ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity();
    entity.setSecureExchangeStatusCode("OPEN");
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

  private SecureExchangeContactTypeCodeEntity createContactType() {
    final SecureExchangeContactTypeCodeEntity entity = new SecureExchangeContactTypeCodeEntity();
    entity.setSecureExchangeContactTypeCode("EDXUSER");
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

}
