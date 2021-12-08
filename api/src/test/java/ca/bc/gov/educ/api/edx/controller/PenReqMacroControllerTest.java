package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.PenRequestMacroController;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestMacroMapper;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestMacroTypeCodeEntity;
import ca.bc.gov.educ.api.edx.repository.PenRequestMacroRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestMacroTypeCodeRepository;
import ca.bc.gov.educ.api.edx.service.v1.PenRequestMacroService;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestMacro;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
public class PenReqMacroControllerTest extends BasePenReqControllerTest {

  private static final PenRequestMacroMapper mapper = PenRequestMacroMapper.mapper;
  @Autowired
  PenRequestMacroController controller;

  @Autowired
  PenRequestMacroService service;

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  PenRequestMacroTypeCodeRepository penRequestMacroTypeCodeRepository;

  @Autowired
  PenRequestMacroRepository penRequestMacroRepository;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.penRequestMacroTypeCodeRepository.save(this.createPenReqMacroTypeCode());
  }

  @After
  public void after() {
    this.penRequestMacroTypeCodeRepository.deleteAll();
    this.penRequestMacroRepository.deleteAll();
  }

  @Test
  public void testRetrievePenRequestMacros_ShouldReturnStatusOK() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL+URL.PEN_REQUEST_MACRO)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQ_MACRO"))))
            .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testRetrievePenRequestMacros_GivenInvalidMacroID_ShouldReturnStatusNotFound() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL+URL.PEN_REQUEST_MACRO+URL.MACRO_ID,UUID.randomUUID().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQ_MACRO"))))
            .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testRetrievePenRequestMacros_GivenValidMacroID_ShouldReturnStatusOK() throws Exception {
    val entity = mapper.toModel(this.getPenRequestMacroEntityFromJsonString());
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    val savedEntity = this.service.createMacro(entity);
    final var result = this.mockMvc.perform(get(URL.BASE_URL+URL.PEN_REQUEST_MACRO+URL.MACRO_ID, savedEntity.getMacroId().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQ_MACRO"))))
            .andDo(print()).andExpect(MockMvcResultMatchers.jsonPath("$.macroId").value(entity.getMacroId().toString())).andExpect(status().isOk()).andReturn();
    assertThat(result).isNotNull();
  }
  @Test
  public void testRetrievePenRequestMacros_GivenInValidMacroID_ShouldReturnStatusNotFound() throws Exception {
    final var result = this.mockMvc.perform(get(URL.BASE_URL+URL.PEN_REQUEST_MACRO+URL.MACRO_ID, UUID.randomUUID().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQ_MACRO"))))
            .andDo(print()).andExpect(status().isNotFound()).andReturn();
    assertThat(result).isNotNull();
  }

  @Test
  public void testRetrievePenRequestMacros_GivenValidMacroTypeCode_ShouldReturnStatusOK() throws Exception {
    val entity = mapper.toModel(this.getPenRequestMacroEntityFromJsonString());
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    val savedEntity = this.service.createMacro(entity);
    final var result = this.mockMvc.perform(get(URL.BASE_URL+URL.PEN_REQUEST_MACRO+"?macroTypeCode=" + savedEntity.getMacroTypeCode())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PEN_REQ_MACRO"))))
            .andDo(print()).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)));
    assertThat(result).isNotNull();
  }

  @Test
  public void testCreatePenRequestMacros_GivenValidPayload_ShouldReturnStatusCreated() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL+URL.PEN_REQUEST_MACRO)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQ_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(this.dummyPenRequestMacroJson())).andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testCreatePenRequestMacros_GivenInValidPayload_ShouldReturnStatusBadRequest() throws Exception {
    this.mockMvc.perform(post(URL.BASE_URL+URL.PEN_REQUEST_MACRO)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQ_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(this.dummyPenRequestMacroJsonWithId())).andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testUpdatePenRequestMacros_GivenValidPayload_ShouldReturnStatusOK() throws Exception {
    val entity = mapper.toModel(this.getPenRequestMacroEntityFromJsonString());
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    val savedEntity = this.service.createMacro(entity);
    savedEntity.setCreateDate(null);
    savedEntity.setUpdateDate(null);
    savedEntity.setMacroText("updated text");
    final String jsonString = new ObjectMapper().writeValueAsString(mapper.toStructure(savedEntity));
    final var result = this.mockMvc.perform(put(URL.BASE_URL+URL.PEN_REQUEST_MACRO+URL.MACRO_ID, savedEntity.getMacroId().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQ_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON).content(jsonString)).andDo(print()).andExpect(status().isOk());
    assertThat(result).isNotNull();

  }
  @Test
  public void testUpdatePenRequestMacros_GivenInValidPayload_ShouldReturnStatusNotFound() throws Exception {
    val entity = mapper.toModel(this.getPenRequestMacroEntityFromJsonString());
    entity.setMacroId(null);
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    val savedEntity = this.service.createMacro(entity);
    savedEntity.setCreateDate(null);
    savedEntity.setUpdateDate(null);
    savedEntity.setMacroText("updated text");
    final String jsonString = new ObjectMapper().writeValueAsString(mapper.toStructure(savedEntity));
    final var result = this.mockMvc.perform(put(URL.BASE_URL+URL.PEN_REQUEST_MACRO+URL.MACRO_ID, UUID.randomUUID().toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PEN_REQ_MACRO")))
            .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON).content(jsonString)).andDo(print()).andExpect(status().isNotFound());
    assertThat(result).isNotNull();

  }

  private PenRequestMacroTypeCodeEntity createPenReqMacroTypeCode() {
    return PenRequestMacroTypeCodeEntity.builder()
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("TEST")
            .updateUser("TEST")
            .description("TEST")
            .displayOrder(1)
            .effectiveDate(LocalDate.now().minusDays(2))
            .expiryDate(LocalDate.now().plusDays(2))
            .label("TEST")
            .macroTypeCode("REJECT")
            .build();
  }

  protected String dummyPenRequestMacroJson() {
    return " {\n" +
            "    \"createUser\": \"om\",\n" +
            "    \"updateUser\": \"om\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroTypeCode\": \"REJECT\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  protected String dummyPenRequestMacroJsonWithId() {
    return " {\n" +
            "    \"createUser\": \"om\",\n" +
            "    \"updateUser\": \"om\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroId\": \"7f000101-7151-1d84-8171-5187006c0000\",\n" +
            "    \"macroTypeCode\": \"REJECT\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  protected PenRequestMacro getPenRequestMacroEntityFromJsonString() {
    try {
      return new ObjectMapper().readValue(this.dummyPenRequestMacroJson(), PenRequestMacro.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
