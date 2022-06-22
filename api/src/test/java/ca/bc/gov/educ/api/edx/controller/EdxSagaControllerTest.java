package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxSagaController;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
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

public class EdxSagaControllerTest extends BaseSecureExchangeControllerTest {

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
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    doNothing().when(this.restUtils).sendEmail(any(), any(), any(), any());
  }

  @After
  public void after() {
    sagaEventStateRepository.deleteAll();
    sagaRepository.deleteAll();
    edxActivationCodeRepository.deleteAll();
    edxRoleRepository.deleteAll();
    edxPermissionRepository.deleteAll();
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
    sagaData.getEdxActivationRoleIds().clear();
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
    sagaData.getEdxActivationRoleIds().add(UUID.randomUUID());
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

  private EdxUserActivationInviteSagaData createUserActivationInviteData(String firstName, String lastName, String email) {

    EdxUserActivationInviteSagaData sagaData = new EdxUserActivationInviteSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName(firstName);
    sagaData.setLastName(lastName);
    sagaData.setEmail(email);
    sagaData.setSchoolName("Test School");
    sagaData.setMincode("00899178");
    List<UUID> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleID());
    sagaData.setEdxActivationRoleIds(rolesList);
    return sagaData;
  }
private void createSagaEntity(String sagaDataStr,EdxUserActivationInviteSagaData sagaData){

    this.sagaService.createSagaRecordInDB(SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA.toString(),"TestEdx",sagaDataStr,null,null,sagaData.getMincode(),sagaData.getEmail());

}

}
