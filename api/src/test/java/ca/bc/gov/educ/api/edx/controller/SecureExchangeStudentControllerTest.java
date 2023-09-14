package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.exception.NotFoundException;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeStudentRepository;
import ca.bc.gov.educ.api.edx.service.v1.RESTService;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeStudentService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SecureExchangeStudentControllerTest extends BaseSecureExchangeControllerTest {

  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  SecureExchangeRequestRepository secureExchangeRequestRepository;
  @Autowired
  SecureExchangeStudentRepository secureExchangeStudentRepository;

  @Autowired
  @InjectMocks
  SecureExchangeStudentService studentService;

  @MockBean
  RESTService restServiceMock;

  private static final String LEGIT_STUDENT_ID = "ac339d70-7649-1a2e-8176-49fbef5e0059";

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void after() {
    this.secureExchangeRequestRepository.deleteAll();
  }


  @Test
  void testAddSecureExchangeStudents_GivenInvalidStudentID_ShouldReturnStatusNotFound() throws Exception {
    final SecureExchangeEntity entity = createSecureExchangeEntityWithStudents(null);
    final String sid = entity.getSecureExchangeID().toString();
    when(restServiceMock.get(anyString(), any(Class.class))).thenThrow(NotFoundException.class);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/" + URL.SECURE_EXCHANGE_ID_STUDENTS, sid)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_STUDENT")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(getStudentJson(UUID.randomUUID().toString()))
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isNotFound());
  }

  @Test
  void testAddExchangeStudents_GivenInvalidExchangeID_ShouldReturnStatusNotFound() throws Exception {
    when(restServiceMock.get(anyString(), any(Class.class))).thenReturn("OK");
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/" + URL.SECURE_EXCHANGE_ID_STUDENTS, UUID.randomUUID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_STUDENT")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(getStudentJson(LEGIT_STUDENT_ID))
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isNotFound());
  }

  @Test
  void testAddSecureExchangeStudents_ShouldReturnStatusCreatedWithUpdatedExchangeObject() throws Exception {
    final SecureExchangeEntity entity = createSecureExchangeEntityWithStudents(null);
    final String sid = entity.getSecureExchangeID().toString();
    when(restServiceMock.get(anyString(), any(Class.class))).thenReturn("OK");
    final String jsonPath = "$.studentsList[?(@.studentId=='" + LEGIT_STUDENT_ID + "')].studentId";
    final String studentEdxUserJsonPath = "$.studentsList[?(@.studentId=='" + LEGIT_STUDENT_ID + "')].staffUserIdentifier";
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/" + URL.SECURE_EXCHANGE_ID_STUDENTS, sid)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_STUDENT")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(getStudentJson(LEGIT_STUDENT_ID))
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isCreated())
      .andExpect(
        MockMvcResultMatchers.jsonPath(jsonPath)
          .value(LEGIT_STUDENT_ID))
      .andExpect(
        MockMvcResultMatchers.jsonPath(studentEdxUserJsonPath)
          .value("TESTUSER"));
  }

  @Test
  void testAddSecureExchangeStudents_ShouldReturnStatusBadRequest() throws Exception {
    final SecureExchangeEntity entity = createSecureExchangeEntityWithStudents(null);
    final String sid = entity.getSecureExchangeID().toString();
    when(restServiceMock.get(anyString(), any(Class.class))).thenReturn("OK");
    final String jsonPath = "$.studentsList[?(@.studentId=='" + LEGIT_STUDENT_ID + "')].studentId";
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/" + URL.SECURE_EXCHANGE_ID_STUDENTS, sid)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_STUDENT")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(getStudentJsonNoStaff(LEGIT_STUDENT_ID))
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  void testAddSecureExchangeStudents_BothIdentifiersShouldReturnStatusBadRequest() throws Exception {
    final SecureExchangeEntity entity = createSecureExchangeEntityWithStudents(null);
    final String sid = entity.getSecureExchangeID().toString();
    when(restServiceMock.get(anyString(), any(Class.class))).thenReturn("OK");
    final String jsonPath = "$.studentsList[?(@.studentId=='" + LEGIT_STUDENT_ID + "')].studentId";
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/" + URL.SECURE_EXCHANGE_ID_STUDENTS, sid)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_STUDENT")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(getStudentJsonBothStaffAndEDXUser(LEGIT_STUDENT_ID))
        .accept(MediaType.APPLICATION_JSON))
      .andDo(print())
      .andExpect(status().isBadRequest());
  }

  @Test
  @Transactional
  public void testDeleteSecureExchangeStudents_ShouldReturnStatusNoContent_AndShouldDeleteFromExchange() throws Exception {
    SecureExchangeEntity entity = createSecureExchangeEntityWithStudents(Arrays.asList(LEGIT_STUDENT_ID));
    final String sid = entity.getSecureExchangeID().toString();
    List<SecureExchangeStudentEntity> students = new ArrayList<>();
    students.addAll(entity.getSecureExchangeStudents());
    String secureExchangeStudentID = String.valueOf(students.get(0).getSecureExchangeStudentId());
    this.mockMvc.perform(delete(URL.BASE_URL_SECURE_EXCHANGE + "/" + URL.SECURE_EXCHANGE_ID_STUDENTS + "/" + secureExchangeStudentID, sid)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE_STUDENT"))))
      .andDo(print())
      .andExpect(status().isNoContent());
  }

  @Test
  @Transactional
  public void testGetStudentsFromExchange_shouldReceiveStatusOK_withListOfStudents() throws Exception {
    final SecureExchangeEntity entity = createSecureExchangeEntityWithStudents(Arrays.asList(LEGIT_STUDENT_ID, UUID.randomUUID().toString()));
    final String sid = entity.getSecureExchangeID().toString();
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE + "/" + URL.SECURE_EXCHANGE_ID_STUDENTS, sid)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE_STUDENT"))))
      .andDo(print())
      .andExpect(status().isOk())
      .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)));
  }

  @Test
  void testGetStudentsFromExchange_GivenInvalidExchangeID_ShouldReturnNotFound() throws Exception {
    when(restServiceMock.get(anyString(), any(Class.class))).thenReturn("OK");
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE + "/" + URL.SECURE_EXCHANGE_ID_STUDENTS, UUID.randomUUID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE_STUDENT"))))
      .andDo(print()).andExpect(status().isNotFound());
  }

  /**
   * Convenience method for creating an exchange entity with n students
   */
  private SecureExchangeEntity createSecureExchangeEntityWithStudents(List<String> studentIDs) {
    SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
    if (studentIDs != null && studentIDs.size() > 0) {
      entity.setSecureExchangeStudents(new HashSet<>());
      for (String s : studentIDs) {
        SecureExchangeStudentEntity student = new SecureExchangeStudentEntity();
        student.setSecureExchangeEntity(entity);
        student.setStudentId(UUID.fromString(s));
        student.setEdxUserID(UUID.randomUUID());
        student.setCreateUser(ApplicationProperties.CLIENT_ID);
        student.setCreateDate(LocalDateTime.now());
        entity.getSecureExchangeStudents().add(student);
      }
      this.secureExchangeRequestRepository.save(entity);
    }
    return entity;
  }

  /**
   * Convenience method for adding a mock student to an existing entity. Does not persist.
   */
  private SecureExchange createSecureExchangeFromEntityWithStudent(String studentId, SecureExchangeEntity secureExchangeEntity) {
    SecureExchangeStudentEntity secureExchangeStudentEntity = new SecureExchangeStudentEntity();
    secureExchangeStudentEntity.setStudentId(UUID.fromString(studentId));
    secureExchangeStudentEntity.setSecureExchangeEntity(secureExchangeEntity);
    secureExchangeStudentEntity.setSecureExchangeStudentId(UUID.randomUUID());
    secureExchangeStudentEntity.setCreateDate(LocalDateTime.now());
    secureExchangeStudentEntity.setCreateUser("test");
    secureExchangeEntity.setSecureExchangeStudents(new HashSet<>());
    secureExchangeEntity.getSecureExchangeStudents().add(secureExchangeStudentEntity);
    return mapper.toStructure(secureExchangeEntity);
  }

  private String getStudentJson(String studentId) {
    return "{\"studentId\": \"" + studentId + "\", \"staffUserIdentifier\": \"" + "TESTUSER" + "\" }";
  }

  private String getStudentJsonBothStaffAndEDXUser(String studentId) {
    return "{\"studentId\": \"" + studentId + "\", \"staffUserIdentifier\": \"" + "TESTUSER" + "\" , \"edxUserID\": \"" + UUID.randomUUID().toString() + "\" }";
  }

  private String getStudentJsonNoStaff(String studentId) {
    return "{\"studentId\": \"" + studentId + "\"}";
  }

}
