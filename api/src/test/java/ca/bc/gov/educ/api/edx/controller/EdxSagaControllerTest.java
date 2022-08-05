package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxSagaController;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComment;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreate;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationRelinkSagaData;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EdxSagaControllerTest extends BaseSagaControllerTest {

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
  SecureExchangeRequestCommentRepository secureExchangeRequestCommentRepository;

  private static final SecureExchangeEntityMapper SECURE_EXCHANGE_ENTITY_MAPPER = SecureExchangeEntityMapper.mapper;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.secureExchangeContactTypeCodeTableRepository.save(createContactType());
    ministryOwnershipTeamEntity = getMinistryOwnershipTeam();
    this.ministryOwnershipTeamRepository.save(ministryOwnershipTeamEntity);
    this.secureExchangeStatusCodeTableRepo.save(createNewStatus());
    doNothing().when(this.restUtils).sendEmail(any(), any(), any(), any());
  }

  @After
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
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenInputWithMissingLastNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setLastName(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Last Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenInputWithMissingFirstNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setFirstName(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("First Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenInputWithMissingEmailRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setEmail(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Email cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenInputWithMissingSchoolNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setSchoolName(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenInputWithMissingMincodeRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.setMincode(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Mincode cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenInputWithMissingRoleIdsRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.getEdxActivationRoleCodes().clear();
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Activation Roles cannot be null or empty")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenInputWithInvalidRoleIdsRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    sagaData.getEdxActivationRoleCodes().add("ABCD");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Invalid Edx Roles in the payload")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenInputWithSagaAlreadyInProgress_ShouldReturnStatusConflict() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    String jsonString = getJsonString(sagaData);
    createSagaEntity(jsonString,sagaData);

    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
          .andDo(print()).andExpect(status().isConflict());
  }

  @Test
  public void testEdxSchoolUserActivationInvite_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationInviteData("firstName", "lastName", "test@bcgov.ca");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-invite-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenInputWithMissingLastNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationRelinkSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setLastName(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Last Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenInputWithMissingFirstNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setFirstName(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("First Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenInputWithMissingEmailRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setEmail(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Email cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenInputWithMissingSchoolNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setSchoolName(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenInputWithMissingMincodeRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.setMincode(null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Mincode cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenInputWithMissingRoleIdsRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.getEdxActivationRoleCodes().clear();
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Activation Roles cannot be null or empty")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenInputWithInvalidRoleIdsRequiredField_ShouldReturnStatusBadRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    sagaData.getEdxActivationRoleCodes().add("ABCD");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Invalid Edx Roles in the payload")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenInputWithSagaAlreadyInProgress_ShouldReturnStatusConflict() throws Exception {
    EdxUserActivationRelinkSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    String jsonString = getJsonString(sagaData);
    createSagaRelinkEntity(jsonString, sagaData);

    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isConflict());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {
    EdxUserActivationInviteSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", UUID.randomUUID().toString(), UUID.randomUUID().toString());
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  public void testEdxSchoolUserActivationRelink_GivenValidInputAndValidSchool_ShouldReturnStatusAcceptedRequest() throws Exception {
    List<String> mincodes = new ArrayList<>();
    mincodes.add("0889966");
    var edxUser = createUserEntityWithMultipleSchools(edxUserRepository, edxPermissionRepository, edxRoleRepository, edxUserSchoolRepository, edxUserDistrictRepository, mincodes);
    EdxUserActivationInviteSagaData sagaData = createUserActivationRelinkData("firstName", "lastName", "test@bcgov.ca", edxUser.getEdxUserID().toString(), edxUser.getEdxUserSchoolEntities().iterator().next().getEdxUserSchoolID().toString());
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/school-user-activation-relink-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "SCHOOL_USER_ACTIVATION_INVITE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  public void testCreateNewSecureExchange_GivenInputWithMissingMincodeRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJsonWithMinAndComment(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, null, "schoolName", "MinTeam");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Mincode cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateNewSecureExchange_GivenInputWithMissingSchoolNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJsonWithMinAndComment(this.ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, "123456789", null, "MinTeam");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateNewSecureExchange_GivenInputWithMissingTeamNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJsonWithMinAndComment(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, "123456789", "WildFlower", null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Ministry Team Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateNewSecureExchange_GivenInputWithMissingSecureExchangeRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createSecureExchangeCreateSagaData(null, "123456789", "WildFlower", "ABC Team");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("SecureExchange cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }


  @Test
  public void testCreateNewSecureExchange_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {

    SecureExchangeCreate secureExchangeCreate = objectMapper.readValue(secureExchangeCreateJsonWithMinAndComment(ministryOwnershipTeamEntity.getMinistryOwnershipTeamId().toString()), SecureExchangeCreate.class);
    val sagaData = createSecureExchangeCreateSagaData(secureExchangeCreate, "123456789", "WildFlower", "Min Team");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/new-secure-exchange-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }

  @Test
  public void testCreateSecureExchangeComment_GivenInputWithMissingMincodeRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, null, "WildFlower", "ABC Team", UUID.randomUUID(),"10");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Mincode cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }
  @Test
  public void testCreateSecureExchangeComment_GivenInputWithMissingSchoolNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, "123456789", null, "ABC Team", UUID.randomUUID(),"10");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("School Name cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateSecureExchangeComment_GivenInputWithMissingMinistryTeamNameRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, "123456789", "WildFlower", null, UUID.randomUUID(),"10");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("MinistryTeamName cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateSecureExchangeComment_GivenInputWithMissingSecureExchangeIdRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, "123456789", "WildFlower", "ABC Team", null,"10");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("SecureExchangeId cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateSecureExchangeComment_GivenInputWithMissingSequenceNumberRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(UUID.randomUUID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, "123456789", "WildFlower", "ABC Team", UUID.randomUUID(),null);
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Sequence Number cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateSecureExchangeComment_GivenInputWithMissingCommentRequiredField_ShouldReturnStatusBadRequest() throws Exception {

    val sagaData = createSecureExchangeCommentSagaData(null, "123456789", "WildFlower", "ABC Team", UUID.randomUUID(),"12");
    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andExpect(jsonPath("$.message", is("Validation error")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Secure Exchange Comment cannot be null")))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateSecureExchangeComment_GivenValidInput_ShouldReturnStatusAcceptedRequest() throws Exception {
    final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(SECURE_EXCHANGE_ENTITY_MAPPER.toModel(this.getSecureExchangeEntityFromJsonString()));
    SecureExchangeComment secureExchangeComment = objectMapper.readValue(secureExchangeCommentJson(entity.getSecureExchangeID().toString()), SecureExchangeComment.class);
    val sagaData = createSecureExchangeCommentSagaData(secureExchangeComment, "123456789", "WildFlower", "ABC Team", entity.getSecureExchangeID(),"10");

    String jsonString = getJsonString(sagaData);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE + "/secure-exchange-comment-saga")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "CREATE_SECURE_EXCHANGE_COMMENT_SAGA"))))
      .andDo(print()).andExpect(status().isAccepted());
  }



  private EdxUserActivationInviteSagaData createUserActivationInviteData(String firstName, String lastName, String email) {

    EdxUserActivationInviteSagaData sagaData = new EdxUserActivationInviteSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName(firstName);
    sagaData.setLastName(lastName);
    sagaData.setEmail(email);
    sagaData.setSchoolName("Test School");
    sagaData.setMincode("00899178");
    List<String> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleCode());
    sagaData.setEdxActivationRoleCodes(rolesList);
    return sagaData;
  }

  private EdxUserActivationRelinkSagaData createUserActivationRelinkData(String firstName, String lastName, String email, String edxUserID, String edxUserSchoolID) {
    EdxUserActivationRelinkSagaData sagaData = new EdxUserActivationRelinkSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName(firstName);
    sagaData.setLastName(lastName);
    sagaData.setEmail(email);
    sagaData.setEdxUserId(edxUserID);
    sagaData.setEdxUserSchoolID(edxUserSchoolID);
    sagaData.setSchoolName("Test School");
    sagaData.setMincode("00899178");
    List<String> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleCode());
    sagaData.setEdxActivationRoleCodes(rolesList);
    return sagaData;
  }

  private void createSagaEntity(String sagaDataStr, EdxUserActivationInviteSagaData sagaData) {
    this.sagaService.createSagaRecordInDB(SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA.toString(), "TestEdx", sagaDataStr, null, null, sagaData.getMincode(), sagaData.getEmail());
  }

  private void createSagaRelinkEntity(String sagaDataStr, EdxUserActivationInviteSagaData sagaData) {
    this.sagaService.createSagaRecordInDB(SagaEnum.EDX_SCHOOL_USER_ACTIVATION_RELINK_SAGA.toString(), "TestEdx", sagaDataStr, null, null, sagaData.getMincode(), sagaData.getEmail());
  }

}
