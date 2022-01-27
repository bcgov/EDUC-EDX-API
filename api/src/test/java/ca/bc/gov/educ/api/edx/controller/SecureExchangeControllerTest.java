package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.SecureExchangeStatusCode;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeController;
import ca.bc.gov.educ.api.edx.filter.FilterOperation;
import ca.bc.gov.educ.api.edx.config.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.secureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.repository.secureExchangeStatusCodeTableRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SearchCriteria;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocument;
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

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecureExchangeControllerTest extends BasePenReqControllerTest {

  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  SecureExchangeController controller;


  @Autowired
  secureExchangeRequestRepository repository;

  @Autowired
  DocumentRepository documentRepository;

  @Autowired
  secureExchangeStatusCodeTableRepository penRequestStatusCodeTableRepo;

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
  public void testRetrievePenRequest_GivenRandomID_ShouldThrowEntityNotFoundException() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL+"/" + UUID.randomUUID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST"))))
            .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testRetrievePenRequest_GivenValidID_ShouldReturnOkStatus() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    this.mockMvc.perform(get(URL.BASE_URL+"/" + entity.getSecureExchangeID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(MockMvcResultMatchers.jsonPath("$.penRequestID").value(entity.getSecureExchangeID().toString()));
  }

  @Test
  public void testFindPenRequest_GivenOnlyPenInQueryParam_ShouldReturnOkStatusAndEntities() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    this.mockMvc.perform(get(URL.BASE_URL+"/?pen" + entity.getSecureExchangeID())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1))).andExpect(MockMvcResultMatchers.jsonPath("$[0].pen").value(entity.getSecureExchangeID()));
  }

  @Test
  public void testRetrievePenRequest_GivenRandomDigitalIdAndStatusCode_ShouldReturnOkStatus() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL+"/?digitalID=" + UUID.randomUUID() + "&status=" + "INT")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  public void testCreatePenRequest_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummyPenRequestJson())).andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testCreatePenRequest_GivenInitialSubmitDateInPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummyPenRequestJsonWithInitialSubmitDate())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreatePenRequest_GivenPenReqIdInPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummyPenRequestJsonWithInvalidPenReqID())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreatePenRequest_LowercaseEmailVerifiedFlag_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummyPenRequestJsonWithInvalidEmailVerifiedFlag())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdatePenRequest_GivenInvalidPenReqIDInPayload_ShouldReturnStatusNotFound() throws Exception {
    this.mockMvc.perform(put(URL.BASE_URL+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummyPenRequestJsonWithInvalidPenReqID())).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testUpdatePenRequest_GivenValidPenReqIDInPayload_ShouldReturnStatusOk() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(put(URL.BASE_URL+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummyPenRequestJsonWithValidPenReqID(penReqId))).andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testUpdatePenRequest_GivenInvalidDemogChangedInPayload_ShouldReturnBadRequest() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(put(URL.BASE_URL+"/")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON).content(this.dummyPenRequestJsonWithInvalidDemogChanged(penReqId))).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testDeletePenRequest_GivenInvalidId_ShouldReturn404() throws Exception {
    this.mockMvc.perform(delete(URL.BASE_URL+"/" + UUID.randomUUID().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testDeletePenRequest_GivenValidId_ShouldReturn204() throws Exception {
    final SecureExchangeEntity entity = this.repository.save(mapper.toModel(this.getPenRequestEntityFromJsonString()));
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(delete(URL.BASE_URL+"/" + penReqId)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  @Test
  public void testDeletePenRequest_GivenValidIdWithAssociations_ShouldReturn204() throws Exception {
    final SecureExchangeEntity secureExchangeEntity = mapper.toModel(this.getPenRequestEntityFromJsonString());
    secureExchangeEntity.setSecureExchangeComment(this.createSecureExchangeComments(secureExchangeEntity));
    final SecureExchangeEntity entity = this.repository.save(secureExchangeEntity);
    final SecureExchangeDocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            //.withoutCreateAndUpdateUser()
            .withPenRequest(entity)
            .withTypeCode("CAPASSPORT")
            .build();
    this.documentRepository.save(document);
    final String penReqId = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(delete(URL.BASE_URL+"/" + penReqId)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_PEN_REQUEST")))
            .contentType(APPLICATION_JSON)
            .accept(APPLICATION_JSON)).andDo(print()).andExpect(status().isNoContent());
  }

  private Set<SecureExchangeCommentEntity> createSecureExchangeComments(final SecureExchangeEntity secureExchange) {
    final Set<SecureExchangeCommentEntity> commentsEntitySet = new HashSet<>();
    final SecureExchangeCommentEntity secureExchangeComment = new SecureExchangeCommentEntity();
    secureExchangeComment.setSecureExchangeEntity(secureExchange);
    secureExchangeComment.setCommentUserName("hi");
   // secureExchangeComment.setComment(LocalDateTime.now());
    commentsEntitySet.add(secureExchangeComment);
    return commentsEntitySet;
  }

  @Test
  public void testReadPenRequestStatus_Always_ShouldReturnStatusOkAndAllDataFromDB() throws Exception {
    this.penRequestStatusCodeTableRepo.save(this.createPenReqStatus());
    this.mockMvc.perform(get(URL.BASE_URL+URL.STATUSES)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATUSES"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
  }

  @Test
  public void testReadPenRequestPaginated_Always_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL+URL.PAGINATED+"?pageSize=2")
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  public void testReadPenRequestPaginated_whenNoDataInDB_ShouldReturnStatusOk() throws Exception {
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(0)));
  }
  @Test
  public void testReadPenRequestPaginatedWithSorting_Always_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final Map<String, String> sortMap = new HashMap<>();
    sortMap.put("legalLastName", "ASC");
    sortMap.put("legalFirstName", "DESC");
    final String sort = new ObjectMapper().writeValueAsString(sortMap);
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                    .param("pageNumber","1").param("pageSize", "5").param("sort", sort)
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(5)));
  }

  @Test
  public void testReadPenRequestPaginated_GivenFirstNameFilter_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.EQUAL).value("Katina").valueType(ValueType.STRING).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final ObjectMapper objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    System.out.println(criteriaJSON);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                    .param("searchCriteriaList", criteriaJSON)
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadPenRequestPaginated_GivenLastNameFilter_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.EQUAL).value("Medling").valueType(ValueType.STRING).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final ObjectMapper objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    System.out.println(criteriaJSON);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                    .param("searchCriteriaList", criteriaJSON)
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadPenRequestPaginated_GivenSubmitDateBetween_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final String fromDate = "2020-04-01T00:00:01";
    final String toDate = "2020-04-15T00:00:01";
    final SearchCriteria criteria = SearchCriteria.builder().key("initialSubmitDate").operation(FilterOperation.BETWEEN).value(fromDate + "," + toDate).valueType(ValueType.DATE_TIME).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final ObjectMapper objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    System.out.println(criteriaJSON);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                    .param("searchCriteriaList", criteriaJSON)
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(2)));
  }

  @Test
  public void testReadPenRequestPaginated_GivenFirstAndLast_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final String fromDate = "2020-04-01T00:00:01";
    final String toDate = "2020-04-15T00:00:01";
    final SearchCriteria criteria = SearchCriteria.builder().key("initialSubmitDate").operation(FilterOperation.BETWEEN).value(fromDate + "," + toDate).valueType(ValueType.DATE_TIME).build();
    final SearchCriteria criteriaFirstName = SearchCriteria.builder().key("legalFirstName").operation(FilterOperation.CONTAINS).value("a").valueType(ValueType.STRING).build();
    final SearchCriteria criteriaLastName = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.CONTAINS).value("o").valueType(ValueType.STRING).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    criteriaList.add(criteriaFirstName);
    criteriaList.add(criteriaLastName);
    final ObjectMapper objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    System.out.println(criteriaJSON);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                    .param("searchCriteriaList", criteriaJSON)
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadPenRequestPaginated_LegalLastNameFilterIgnoreCase_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
            Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("legalLastName").operation(FilterOperation.CONTAINS_IGNORE_CASE).value("j").valueType(ValueType.STRING).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final ObjectMapper objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    System.out.println(criteriaJSON);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
            .perform(get(URL.BASE_URL+URL.PAGINATED)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                    .param("searchCriteriaList", criteriaJSON)
                    .contentType(APPLICATION_JSON))
            .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }
  @Test
  public void testReadPenRequestPaginated_digitalID_ShouldReturnStatusOk() throws Exception {
    final var file = new File(
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("digitalID").operation(FilterOperation.EQUAL).value("fdf94a22-51e3-4816-8665-9f8571af1be4").valueType(ValueType.UUID).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final var objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    final MvcResult result = this.mockMvc
        .perform(get(URL.BASE_URL+URL.PAGINATED)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
                .param("searchCriteriaList", criteriaJSON)
            .contentType(APPLICATION_JSON))
        .andReturn();
    this.mockMvc.perform(asyncDispatch(result)).andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$.content", hasSize(1)));
  }

  @Test
  public void testReadPenRequestPaginated_givenOperationTypeNull_ShouldReturnStatusOk() throws Exception {
    final var file = new File(
        Objects.requireNonNull(this.getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    final List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    final SearchCriteria criteria = SearchCriteria.builder().key("digitalID").operation(null).value("fdf94a22-51e3-4816-8665-9f8571af1be4").valueType(ValueType.UUID).build();
    final List<SearchCriteria> criteriaList = new ArrayList<>();
    criteriaList.add(criteria);
    final var objectMapper = new ObjectMapper();
    final String criteriaJSON = objectMapper.writeValueAsString(criteriaList);
    this.repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));
    this.mockMvc.perform(get(URL.BASE_URL+URL.PAGINATED)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST")))
            .param("searchCriteriaList", criteriaJSON)
        .contentType(APPLICATION_JSON)).andDo(print()).andExpect(status().isBadRequest());
  }


  @Test
  public void testGetStats_COMPLETIONS_LAST_WEEK_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    var updateDate = LocalDateTime.now().minusDays(6);
    var dayName1 = updateDate.getDayOfWeek().name();
    entities.get(0).setStatusUpdateDate(updateDate.toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.AUTO.toString());
    updateDate = LocalDateTime.now();
    var dayName2 = updateDate.getDayOfWeek().name();
    entities.get(1).setStatusUpdateDate(updateDate.toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "COMPLETIONS_LAST_WEEK")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.completionsInLastWeek." + dayName1, is(1)))
      .andExpect(jsonPath("$.completionsInLastWeek." + dayName2, is(1)));
  }

  @Test
  public void testGetStats_COMPLETIONS_LAST_12_MONTH_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    var updateDate = LocalDateTime.now();
    var month1 = updateDate.getMonth().toString();
    entities.get(0).setStatusUpdateDate(updateDate.toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.AUTO.toString());
    updateDate = LocalDateTime.now().withDayOfMonth(1).minusMonths(11);
    var month2 = updateDate.getMonth().toString();
    entities.get(1).setStatusUpdateDate(updateDate.toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "COMPLETIONS_LAST_12_MONTH")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.completionsInLastTwelveMonth." + month1, is(1)))
      .andExpect(jsonPath("$.completionsInLastTwelveMonth." + month2, is(1)));
  }

  @Test
  public void testGetStats_PERCENT_GMP_REJECTED_TO_LAST_MONTH_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.REJECTED.toString());
    entities.get(0).setStatusUpdateDate(LocalDateTime.now().minusMonths(1).toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.REJECTED.toString());
    entities.get(1).setStatusUpdateDate(LocalDateTime.now().toString());
    entities.get(2).setSecureExchangeStatusCode(SecureExchangeStatusCode.REJECTED.toString());
    entities.get(2).setStatusUpdateDate(LocalDateTime.now().toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "PERCENT_GMP_REJECTED_TO_LAST_MONTH")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.percentRejectedGmpToLastMonth", closeTo(100, 0.001)))
      .andExpect(jsonPath("$.gmpRejectedInCurrentMonth", is(2)));
  }

  @Test
  public void testGetStats_PERCENT_GMP_ABANDONED_TO_LAST_MONTH_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.ABANDONED.toString());
    entities.get(0).setStatusUpdateDate(LocalDateTime.now().minusMonths(1).toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.ABANDONED.toString());
    entities.get(1).setStatusUpdateDate(LocalDateTime.now().toString());
    entities.get(2).setSecureExchangeStatusCode(SecureExchangeStatusCode.ABANDONED.toString());
    entities.get(2).setStatusUpdateDate(LocalDateTime.now().toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "PERCENT_GMP_ABANDONED_TO_LAST_MONTH")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.percentAbandonedGmpToLastMonth", closeTo(100, 0.001)))
      .andExpect(jsonPath("$.gmpAbandonedInCurrentMonth", is(2)));
  }

  @Test
  public void testGetStats_PERCENT_GMP_COMPLETION_TO_LAST_MONTH_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(0).setStatusUpdateDate(LocalDateTime.now().minusMonths(1).toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(1).setStatusUpdateDate(LocalDateTime.now().toString());
    entities.get(2).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(2).setStatusUpdateDate(LocalDateTime.now().toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "PERCENT_GMP_COMPLETION_TO_LAST_MONTH")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.percentCompletedGmpToLastMonth", closeTo(100, 0.001)))
      .andExpect(jsonPath("$.gmpCompletedInCurrentMonth", is(2)));
  }

  @Test
  public void testGetStats_PERCENT_GMP_COMPLETED_WITH_DOCUMENTS_TO_LAST_MONTH_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(0).setStatusUpdateDate(LocalDateTime.now().toString());
    var PenRequests = repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    SecureExchangeDocumentEntity document = new DocumentBuilder()
      .withoutDocumentID()
      .withPenRequest(PenRequests.get(0))
      .withTypeCode("CAPASSPORT")
      .build();
    this.documentRepository.save(document);

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "PERCENT_GMP_COMPLETED_WITH_DOCUMENTS_TO_LAST_MONTH")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.percentGmpCompletedWithDocumentsToLastMonth", closeTo(1, 0.001)))
      .andExpect(jsonPath("$.gmpCompletedWithDocsInCurrentMonth", is(1)));
  }

  @Test
  public void testGetStats_ALL_STATUSES_LAST_12_MONTH_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(0).setStatusUpdateDate(LocalDateTime.now().minusMonths(11).toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(1).setStatusUpdateDate(LocalDateTime.now().toString());
    entities.get(2).setSecureExchangeStatusCode(SecureExchangeStatusCode.RETURNED.toString());
    entities.get(2).setStatusUpdateDate(LocalDateTime.now().minusMonths(3).toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "ALL_STATUSES_LAST_12_MONTH")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.allStatsLastTwelveMonth.MANUAL", is(2)))
      .andExpect(jsonPath("$.allStatsLastTwelveMonth.RETURNED", is(1)));
  }

  @Test
  public void testGetStats_ALL_STATUSES_LAST_6_MONTH_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(0).setStatusUpdateDate(LocalDateTime.now().minusMonths(5).toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(1).setStatusUpdateDate(LocalDateTime.now().toString());
    entities.get(2).setSecureExchangeStatusCode(SecureExchangeStatusCode.RETURNED.toString());
    entities.get(2).setStatusUpdateDate(LocalDateTime.now().minusMonths(3).toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "ALL_STATUSES_LAST_6_MONTH")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.allStatsLastSixMonth.MANUAL", is(2)))
      .andExpect(jsonPath("$.allStatsLastSixMonth.RETURNED", is(1)));
  }

  @Test
  public void testGetStats_ALL_STATUSES_LAST_1_MONTH_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(0).setStatusUpdateDate(LocalDateTime.now().minusDays(1).minusMonths(1).toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(1).setStatusUpdateDate(LocalDateTime.now().toString());
    entities.get(2).setSecureExchangeStatusCode(SecureExchangeStatusCode.RETURNED.toString());
    entities.get(2).setStatusUpdateDate(LocalDateTime.now().minusMonths(1).toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "ALL_STATUSES_LAST_1_MONTH")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.allStatsLastOneMonth.MANUAL", is(2)))
      .andExpect(jsonPath("$.allStatsLastOneMonth.RETURNED", is(1)));
  }

  @Test
  public void testGetStats_ALL_STATUSES_LAST_1_WEEK_ShouldReturnStatusOk() throws Exception {
    final File file = new File(
      Objects.requireNonNull(getClass().getClassLoader().getResource("mock_pen_requests.json")).getFile()
    );
    List<SecureExchange> entities = new ObjectMapper().readValue(file, new TypeReference<>() {
    });
    entities.get(0).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(0).setStatusUpdateDate(LocalDateTime.now().minusDays(6).toString());
    entities.get(1).setSecureExchangeStatusCode(SecureExchangeStatusCode.MANUAL.toString());
    entities.get(1).setStatusUpdateDate(LocalDateTime.now().toString());
    entities.get(2).setSecureExchangeStatusCode(SecureExchangeStatusCode.RETURNED.toString());
    entities.get(2).setStatusUpdateDate(LocalDateTime.now().minusDays(2).toString());
    repository.saveAll(entities.stream().map(mapper::toModel).collect(Collectors.toList()));

    this.mockMvc.perform(get(URL.BASE_URL + URL.STATS)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQUEST_STATS")))
      .param("statsType", "ALL_STATUSES_LAST_1_WEEK")
      .contentType(APPLICATION_JSON))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.allStatsLastOneWeek.MANUAL", is(2)))
      .andExpect(jsonPath("$.allStatsLastOneWeek.RETURNED", is(1)));
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

  private String dummyPenRequestCommentsJsonWithValidPenReqID(final String penReqId) {
    return "{\n" +
            "  \"penRetrievalRequestID\": \"" + penReqId + "\",\n" +
            "  \"content\": \"" + "comment1" + "\",\n" +
            "}";
  }

}
