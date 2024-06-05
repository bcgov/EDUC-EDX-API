package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxSagaController;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.support.DocumentTypeCodeBuilder;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.restassured.internal.util.IOUtils;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.io.FileInputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EdxSagaControllerTest extends BaseSagaControllerTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  RestUtils restUtils;
  @Autowired
  SagaRepository sagaRepository;

  @Autowired
  SagaEventStateRepository sagaEventStateRepository;
  @Autowired
  EdxActivationCodeRepository edxActivationCodeRepository;
  @Autowired
  SagaService sagaService;
  @Autowired
  EdxSagaController edxSagaController;

  @Autowired
  EdxUserRepository edxUserRepository;

  @Autowired
  EdxUserSchoolRepository edxUserSchoolRepository;

  @Autowired
  EdxUserDistrictRepository edxUserDistrictRepository;

  @Autowired
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;

  @Autowired
  MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Autowired
  SecureExchangeContactTypeCodeTableRepository secureExchangeContactTypeCodeTableRepository;

  @Autowired
  SecureExchangeStatusCodeTableRepository secureExchangeStatusCodeTableRepo;

  @Autowired
  DocumentRepository documentRepository;

  @Autowired
  SecureExchangeRequestRepository secureExchangeRequestRepository;

  MinistryOwnershipTeamEntity ministryOwnershipTeamEntity;

  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;

  @Autowired
  SecureExchangeRequestCommentRepository secureExchangeRequestCommentRepository;

  private static final SecureExchangeEntityMapper SECURE_EXCHANGE_ENTITY_MAPPER = SecureExchangeEntityMapper.mapper;

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);
    this.secureExchangeContactTypeCodeTableRepository.save(createContactType());
    ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    this.ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.secureExchangeStatusCodeTableRepo.save(createNewStatus());
    doNothing().when(this.restUtils).sendEmail(any(), any(), any(), any());
  }

  @AfterEach
  public void after() {
    sagaEventStateRepository.deleteAll();
    sagaRepository.deleteAll();
    edxActivationCodeRepository.deleteAll();
    edxRoleRepository.deleteAll();
    edxPermissionRepository.deleteAll();
    documentRepository.deleteAll();
    secureExchangeRequestRepository.deleteAll();
    ministryOwnershipTeamRepository.deleteAll();
    secureExchangeStatusCodeTableRepo.deleteAll();
    secureExchangeContactTypeCodeTableRepository.deleteAll();
    this.documentTypeCodeRepository.deleteAll();
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenInputWithMissingLastNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setLastName(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Last Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenInputWithMissingFirstNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setFirstName(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("First Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenInputWithMissingEmailRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setEmail(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Email cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenInputWithMissingSchoolNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setSchoolName(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenInputWithMissingSchoolIDRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setSchoolID(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School ID cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenInputWithMissingRoleIdsRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.getEdxActivationRoleCodes().clear();
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Roles list cannot be empty.")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenInputWithInvalidRoleIdsRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.getEdxActivationRoleCodes().add("ABCD");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxActivationRoleCode is not valid according to the allow list.")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenInputWithSagaAlreadyInProgress_ShouldReturnStatusConflict() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    String jsonString = getJsonString(sagaData);
    createSagaEntity(jsonString, sagaData);

    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isConflict());
  }

  @Test
  void testEdxSchoolUserActivationInvite_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenInputWithMissingLastNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationRelinkSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setLastName(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Last Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenInputWithMissingFirstNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setFirstName(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("First Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenInputWithMissingEmailRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setEmail(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Email cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenInputWithMissingSchoolNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setSchoolName(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenInputWithMissingSchoolIDRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setSchoolID(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School ID cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenInputWithMissingRoleIdsRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.getEdxActivationRoleCodes().clear();
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Roles list cannot be empty.")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenInputWithInvalidRoleIdsRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.getEdxActivationRoleCodes().add("ABCD");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxActivationRoleCode is not valid according to the allow list.")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenInputWithSagaAlreadyInProgress_ShouldReturnStatusConflict() throws Exception {
    EdxUserSchoolActivationRelinkSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    String jsonString = getJsonString(sagaData);
    createSagaSchoolRelinkEntity(jsonString, sagaData);

    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isConflict());
  }

  @Test
  void testEdxDistrictUserActivationRelink_GivenInputWithSagaAlreadyInProgress_ShouldReturnStatusConflict() throws Exception {
    EdxUserDistrictActivationRelinkSagaData sagaData = createUserDistrictActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    String jsonString = getJsonString(sagaData);
    createSagaDistrictRelinkEntity(jsonString, sagaData);

    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isConflict());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {
    EdxUserSchoolActivationInviteSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testEdxSchoolUserActivationRelink_GivenValidInputAndValidSchool_ShouldReturnStatusAcceptedRequest() throws Exception {
    List<UUID> schoolIDs = new ArrayList<>();
    schoolIDs.add(UUID.randomUUID());
    var edxUser = createUserEntityWithMultipleSchools(edxUserRepository, edxPermissionRepository, edxRoleRepository, edxUserSchoolRepository, edxUserDistrictRepository, schoolIDs);
    EdxUserSchoolActivationInviteSagaData sagaData = createUserSchoolActivationRelinkData("firstName", "lastName", "test@bcgov.ca", edxUser.getEdxUserID().toString(), edxUser.getEdxUserSchoolEntities().iterator().next().getEdxUserSchoolID().toString());
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testCreateNewSecureExchange_GivenInputWithMissingSchoolIDRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJson(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString(), "SCHOOL"), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, null, "schoolName", null, null, "MinTeam");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[1].message", is("School ID cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateNewSecureExchange_GivenInputWithMissingDistrictIDRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJson(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString(), "DISTRICT"), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, null, null, null, "name", "MinTeam");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[1].message", is("District ID cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateNewSecureExchange_GivenInputWithMissingDistrictNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJson(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString(), "DISTRICT"), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, null, null, UUID.randomUUID(), null, "MinTeam");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[1].message", is("District Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateNewSecureExchange_GivenInputWithMissingSchoolNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJson(this.ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString(),"SCHOOL"), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, UUID.randomUUID(), null, null, null, "MinTeam");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[1].message", is("School Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateNewSecureExchange_GivenInputWithMissingTeamNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJsonWithMinAndComment(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, UUID.randomUUID(), "WildFlower", null, null, null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Ministry Team Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateNewSecureExchange_GivenInputWithMissingSecureExchangeRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    val sagaData = createSecureExchangeCreateSagaData(null, UUID.randomUUID(), "WildFlower", null, null, "ABC Team");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("SecureExchange cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }


  @Test
  void testCreateNewSecureExchange_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {
    final SecureExchangeDocument secureExchangeDocument = this.getSampleDocument(null);
    final SecureExchangeStudent secureExchangeStudent = this.getSampleStudent();
    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJsonWithMinAndComment(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, UUID.randomUUID(), "WildFlower", null, null, "Min Team");
    sagaData.getSecureExchangeCreate().setDocumentList(new ArrayList<>());
    sagaData.getSecureExchangeCreate().setStudentList(new ArrayList<>());
    sagaData.getSecureExchangeCreate().getDocumentList().add(secureExchangeDocument);
    sagaData.getSecureExchangeCreate().getStudentList().add(secureExchangeStudent);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testCreateSecureExchangeComment_GivenInputWithMissingSchoolIDRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, null, null, null, "WildFlower", "ABC Team", UUID.randomUUID(), "10", "SCHOOL");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School ID cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchangeComment_GivenInputWithMissingSchoolNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, UUID.randomUUID(), null, null,null, "ABC Team", UUID.randomUUID(), "10", "SCHOOL");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchangeComment_GivenInputWithMissingDistrictNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, null, UUID.randomUUID(), null, null, "ABC Team", UUID.randomUUID(), "10", "DISTRICT");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("District Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchangeComment_GivenInputWithMissingDistrictIdRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, null, null, "DGroup", null, "ABC Team", UUID.randomUUID(), "10", "DISTRICT");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("District ID cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchangeComment_GivenInputWithMissingMinistryTeamNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, UUID.randomUUID(),null, null, "WildFlower", null, UUID.randomUUID(), "10", "SCHOOL");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("MinistryTeamName cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchangeComment_GivenInputWithMissingSecureExchangeIdRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, UUID.randomUUID(), null, null,"WildFlower", "ABC Team", null, "10", "SCHOOL");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("SecureExchangeId cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchangeComment_GivenInputWithMissingSequenceNumberRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, UUID.randomUUID(),null, null, "WildFlower", "ABC Team", UUID.randomUUID(), null, "SCHOOL");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Sequence Number cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSecureExchangeComment_GivenInputWithMissingCommentRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createSecureExchangeCommentSagaData(null, UUID.randomUUID(),null, null, "WildFlower", "ABC Team", UUID.randomUUID(), "12", "SCHOOL");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Secure Exchange Comment cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }



  @Test
  void testCreateSecureExchangeComment_GivenValidInputForSchool_ShouldReturnStatusAcceptedRequest() throws Exception {
    final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(SECURE_EXCHANGE_ENTITY_MAPPER.toModel(this.getSecureExchangeEntityFromJsonString()));
    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(entity.getSecureExchangeID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, UUID.randomUUID(),null, null, "WildFlower", "ABC Team", entity.getSecureExchangeID(), "10", "SCHOOL");

    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testCreateSecureExchangeComment_GivenValidInputForDistrict_ShouldReturnStatusAcceptedRequest() throws Exception {
    final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(SECURE_EXCHANGE_ENTITY_MAPPER.toModel(this.getSecureExchangeEntityFromJsonString()));
    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(entity.getSecureExchangeID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, null,UUID.randomUUID(), "DGroup", null, "ABC Team", entity.getSecureExchangeID(), "10", "DISTRICT");

    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }
  @Test
  void testEdxDistrictUserActivationInvite_GivenInputWithMissingFirstNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createDistrictUserActivationInviteData(null, "lastName", "test@bcgov.ca");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("First Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxDistrictUserActivationInvite_GivenInputWithMissingLastNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createDistrictUserActivationInviteData("firstName", null, "test@bcgov.ca");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Last Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxDistrictUserActivationInvite_GivenInputWithMissingEmailRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createDistrictUserActivationInviteData("firstName", "lastName", null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Email cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxDistrictUserActivationInvite_GivenInputWithInvalidEmailRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createDistrictUserActivationInviteData("firstName", "lastName", "test.abc");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Email address should be a valid email address")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxDistrictUserActivationInvite_GivenInputWithMissingDistrictIdRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createDistrictUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setDistrictID(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("DistrictID cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxDistrictUserActivationInvite_GivenInputWithMissingDistrictNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createDistrictUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setDistrictName(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("District Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxDistrictUserActivationInvite_GivenInputWithMissingDistrictIDRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    val sagaData = createDistrictUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setDistrictID(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("DistrictID cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxDistrictUserActivationInvite_GivenInputWithMissingEdxActivationRoleCodeRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createDistrictUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setEdxActivationRoleCodes(null);
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Activation Roles cannot be null or empty")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testEdxDistrictUserActivationInvite_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {

    val sagaData = createDistrictUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/district-user-activation-invite-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "DISTRICT_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testEdxMoveSchool_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {
    MoveSchoolData sagaData = createDummyMoveSchoolSagaData();
    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/move-school-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "MOVE_SCHOOL_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testCreateSchool_GivenAValidPayload_ShouldReturnStatusAcceptedRequest() throws Exception {
    School school = createDummySchool();
    EdxUser user = createDummyUser();

    CreateSchoolSagaData sagaData = new CreateSchoolSagaData();
    sagaData.setSchool(school);
    sagaData.setInitialEdxUser(user);

    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/create-school-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getJsonString(sagaData))
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SCHOOL_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testCreateSchool_GivenInvalidEdxUser_ShouldReturnBadRequest() throws Exception {
    School school = createDummySchool();
    EdxUser user = createDummyUser();

    // The invalid data
    user.setEdxUserID(UUID.randomUUID().toString());

    CreateSchoolSagaData sagaData = new CreateSchoolSagaData();
    sagaData.setSchool(school);
    sagaData.setInitialEdxUser(user);

    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/create-school-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getJsonString(sagaData))
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SCHOOL_SAGA"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  void testCreateSchool_GivenNoInitialUserAndIndependentOffshoreCategory_ShouldReturnOk() throws Exception {
    School school = createDummySchool();
    school.setSchoolCategoryCode("INDEPEND");

    CreateSchoolSagaData sagaData = new CreateSchoolSagaData();
    sagaData.setSchool(school);

    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/create-school-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(getJsonString(sagaData))
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SCHOOL_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testEdxMoveSchoolWithDisplayNameNoSpecChars_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {
    School school = createDummySchool();
    school.setDisplayNameNoSpecialChars("Test Special Chars.");

    MoveSchoolData sagaData = new MoveSchoolData();
    sagaData.setToSchool(school);
    sagaData.setMoveDate(String.valueOf(LocalDateTime.now().minusDays(1).withNano(0)));
    sagaData.setFromSchoolId("be44a3f7-1a04-938e-dcdc-118989f6dd23");
    sagaData.setCreateUser("Test");
    sagaData.setUpdateUser("Test");

    String jsonString = getJsonString(sagaData);
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/move-school-saga")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "MOVE_SCHOOL_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  void testProcessOnboardingFile_givenValidPayloadSchoolsMissing_ShouldReturnStatusOk() throws Exception {
    final FileInputStream fis = new FileInputStream("src/test/resources/edx-onboading-sample.csv");
    final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
    assertThat(fileContents).isNotEmpty();
    val body = OnboardingFileUpload.builder().fileContents(fileContents).createUser("test").build();
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/onboarding-file")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE")))
            .content(JsonUtil.getJsonStringFromObject(body))
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.processedCount", is("0")));
  }

  @Test
  void testProcessOnboardingFile_givenValidPayload_ShouldReturnStatusOk() throws Exception {
    Map<String, School> schoolMap = new ConcurrentHashMap<>();
    schoolMap.put("12345678", createFakeSchool(UUID.randomUUID().toString(), "12345678"));
    schoolMap.put("98765432", createFakeSchool(UUID.randomUUID().toString(), "98765432"));
    Map<String, District> districtMap = new ConcurrentHashMap<>();
    Mockito.when(this.restUtils.getDistrictNumberMap()).thenReturn(districtMap);
    Mockito.when(this.restUtils.getSchoolMincodeMap()).thenReturn(schoolMap);
    final FileInputStream fis = new FileInputStream("src/test/resources/edx-onboading-sample.csv");
    final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
    assertThat(fileContents).isNotEmpty();
    val body = OnboardingFileUpload.builder().fileContents(fileContents).createUser("test").build();
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/onboarding-file")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE")))
            .content(JsonUtil.getJsonStringFromObject(body))
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.processedCount", is("2")));
  }

  @Test
  void testProcessOnboardingFileWithDistrict_givenValidPayload_ShouldReturnStatusOk() throws Exception {
    Map<String, School> schoolMap = new ConcurrentHashMap<>();
    schoolMap.put("12345678", createFakeSchool(UUID.randomUUID().toString(), "12345678"));
    schoolMap.put("98765432", createFakeSchool(UUID.randomUUID().toString(), "98765432"));

    Map<String, District> districtMap = new ConcurrentHashMap<>();
    districtMap.put("123", createFakeDistrict(UUID.randomUUID().toString(), "123"));
    districtMap.put("987", createFakeDistrict(UUID.randomUUID().toString(), "987"));

    Mockito.when(this.restUtils.getDistrictNumberMap()).thenReturn(districtMap);
    Mockito.when(this.restUtils.getSchoolMincodeMap()).thenReturn(schoolMap);
    final FileInputStream fis = new FileInputStream("src/test/resources/edx-onboading-sample.csv");
    final String fileContents = Base64.getEncoder().encodeToString(IOUtils.toByteArray(fis));
    assertThat(fileContents).isNotEmpty();
    val body = OnboardingFileUpload.builder().fileContents(fileContents).createUser("test").build();
    this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/onboarding-file")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE")))
            .content(JsonUtil.getJsonStringFromObject(body))
            .contentType(APPLICATION_JSON)).andExpect(status().isOk()).andExpect(jsonPath("$.processedCount", is("3")));
  }

  private School createFakeSchool(String schoolId, String mincode) {
    School school = new School();
    school.setDistrictId("34bb7566-ff59-653e-f778-2c1a4d669b00");
    school.setSchoolId(schoolId);
    school.setMincode(mincode);
    school.setSchoolNumber("00002");
    school.setDisplayName("Test School");
    school.setSchoolOrganizationCode("TRIMESTER");
    school.setSchoolCategoryCode("FED_BAND");
    school.setFacilityTypeCode("STANDARD");
    return school;
  }

  private District createFakeDistrict(String districtId, String districtNumber) {
    District district = new District();
    district.setDistrictId(districtId);
    district.setDistrictNumber(districtNumber);
    district.setDisplayName("Test District");
    district.setDistrictRegionCode("METRO");
    district.setDistrictStatusCode("ACTIVE");
    return district;
  }

  private MoveSchoolData createDummyMoveSchoolSagaData() {
    MoveSchoolData moveSchool = new MoveSchoolData();
    moveSchool.setToSchool(createDummySchool());
    moveSchool.setMoveDate(String.valueOf(LocalDateTime.now().minusDays(1).withNano(0)));
    moveSchool.setFromSchoolId("be44a3f7-1a04-938e-dcdc-118989f6dd23");
    moveSchool.setCreateUser("Test");
    moveSchool.setUpdateUser("Test");
    return moveSchool;
  }

  private School createDummySchool() {
    School school = new School();
    school.setDistrictId("34bb7566-ff59-653e-f778-2c1a4d669b00");
    school.setSchoolNumber("00002");
    school.setDisplayName("Test College");
    school.setSchoolOrganizationCode("TRIMESTER");
    school.setSchoolCategoryCode("FED_BAND");
    school.setSchoolReportingRequirementCode("REGULAR");
    school.setFacilityTypeCode("STANDARD");
    school.setGrades(List.of(createSchoolGrade()));
    school.setNeighborhoodLearning(List.of(createNeighborhoodLearning()));
    return school;
  }

  private EdxUser createDummyUser() {
    EdxUser user = new EdxUser();
    user.setFirstName("TestFirst");
    user.setLastName("TestLast");
    user.setEmail("test@test.ca");
    return user;
  }

  private SchoolGrade createSchoolGrade() {
    SchoolGrade schoolGrade = new SchoolGrade();
    schoolGrade.setSchoolGradeCode("01");
    schoolGrade.setCreateUser("TEST");
    schoolGrade.setUpdateUser("TEST");
    return schoolGrade;
  }
  private NeighborhoodLearning createNeighborhoodLearning() {
    NeighborhoodLearning neighborhoodLearning = new NeighborhoodLearning();
    neighborhoodLearning.setNeighborhoodLearningTypeCode("COMM_USE");
    neighborhoodLearning.setCreateUser("TEST");
    neighborhoodLearning.setUpdateUser("TEST");
    return neighborhoodLearning;
  }

  private EdxUserSchoolActivationInviteSagaData createUserActivationInviteData(String firstName, String lastName, String email) {

    EdxUserSchoolActivationInviteSagaData sagaData = new EdxUserSchoolActivationInviteSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName(firstName);
    sagaData.setLastName(lastName);
    sagaData.setEmail(email);
    sagaData.setSchoolName("Test School");
    sagaData.setSchoolID(UUID.randomUUID());
    List<String> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleCode());
    sagaData.setEdxActivationRoleCodes(rolesList);
    sagaData.setCreateUser("Test");
    sagaData.setUpdateUser("Test");
    return sagaData;
  }

  private EdxUserSchoolActivationRelinkSagaData createUserSchoolActivationRelinkData(String firstName, String lastName, String email, String edxUserID, String edxUserSchoolID) {
    EdxUserSchoolActivationRelinkSagaData sagaData = new EdxUserSchoolActivationRelinkSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName(firstName);
    sagaData.setLastName(lastName);
    sagaData.setEmail(email);
    sagaData.setEdxUserId(edxUserID);
    sagaData.setEdxUserSchoolID(edxUserSchoolID);
    sagaData.setSchoolName("Test School");
    sagaData.setSchoolID(UUID.randomUUID());
    List<String> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleCode());
    sagaData.setEdxActivationRoleCodes(rolesList);
    sagaData.setCreateUser("Test");
    sagaData.setUpdateUser("Test");
    return sagaData;
  }

  private EdxUserDistrictActivationRelinkSagaData createUserDistrictActivationRelinkData(String firstName, String lastName, String email, String edxUserID, String edxUserDistrictID) {
    EdxUserDistrictActivationRelinkSagaData sagaData = new EdxUserDistrictActivationRelinkSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName(firstName);
    sagaData.setLastName(lastName);
    sagaData.setEmail(email);
    sagaData.setEdxUserId(edxUserID);
    sagaData.setEdxUserDistrictID(edxUserDistrictID);
    sagaData.setDistrictName("Test District");
    sagaData.setDistrictID(UUID.randomUUID());
    List<String> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleCode());
    sagaData.setEdxActivationRoleCodes(rolesList);
    sagaData.setCreateUser("Test");
    sagaData.setUpdateUser("Test");
    return sagaData;
  }

  private void createSagaEntity(String sagaDataStr, EdxUserSchoolActivationInviteSagaData sagaData) {
    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA), sagaData);
      this.sagaService.createSagaRecordInDB(sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }

  private void createSagaSchoolRelinkEntity(String sagaDataStr, EdxUserSchoolActivationInviteSagaData sagaData) {

    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.EDX_SCHOOL_USER_ACTIVATION_RELINK_SAGA), sagaData);
      this.sagaService.createSagaRecordInDB(sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }

  private void createSagaDistrictRelinkEntity(String sagaDataStr, EdxUserDistrictActivationInviteSagaData sagaData) {

    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.EDX_DISTRICT_USER_ACTIVATION_RELINK_SAGA), sagaData);
      this.sagaService.createSagaRecordInDB(sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }


  private EdxUserDistrictActivationInviteSagaData createDistrictUserActivationInviteData(String firstName, String lastName, String email) {

    EdxUserDistrictActivationInviteSagaData sagaData = new EdxUserDistrictActivationInviteSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName(firstName);
    sagaData.setLastName(lastName);
    sagaData.setEmail(email);
    sagaData.setDistrictID(UUID.randomUUID());
    sagaData.setDistrictName("Test District");
    List<String> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleCode());
    sagaData.setEdxActivationRoleCodes(rolesList);
    sagaData.setCreateUser("Test");
    sagaData.setUpdateUser("Test");
    return sagaData;
  }

  private SecureExchangeDocument getSampleDocument(final String documentId) {
    final SecureExchangeDocument secureExchangeDocument = new SecureExchangeDocument();
    secureExchangeDocument.setDocumentID(documentId);
    secureExchangeDocument.setDocumentData("TXkgY2FyZCE=");
    secureExchangeDocument.setDocumentTypeCode("BCSCPHOTO");
    secureExchangeDocument.setFileName("card.jpg");
    secureExchangeDocument.setFileExtension(".jpg");
    secureExchangeDocument.setFileSize(8);
    return secureExchangeDocument;
  }

  private SecureExchangeStudent getSampleStudent() {
    final SecureExchangeStudent secureExchangeStudent = new SecureExchangeStudent();
    secureExchangeStudent.setStudentId(UUID.randomUUID().toString());
    secureExchangeStudent.setEdxUserID(UUID.randomUUID().toString());
    return secureExchangeStudent;
  }
}
