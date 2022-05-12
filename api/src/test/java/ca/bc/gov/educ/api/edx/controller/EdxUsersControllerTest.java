package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxUsersController;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxRoleMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserSchoolRoleMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxPermissionEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxRoleEntity;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.EdxRole;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchool;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class EdxUsersControllerTest extends BaseSecureExchangeControllerTest {

  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  EdxUsersController controller;

  @Autowired
  MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Autowired
  private EdxUserRepository edxUserRepository;

  @Autowired
  private EdxUserSchoolRepository edxUserSchoolRepository;

  @Autowired
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;

  @Autowired
  private EdxUserDistrictRepository edxUserDistrictRepository;


  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @AfterEach
  public void after() {
    this.ministryOwnershipTeamRepository.deleteAll();
    this.edxUserRepository.deleteAll();
    this.edxUserSchoolRepository.deleteAll();
    this.ministryOwnershipTeamRepository.deleteAll();
    this.edxRoleRepository.deleteAll();
    this.edxPermissionRepository.deleteAll();
    this.edxUserDistrictRepository.deleteAll();
  }

  @Test
  public void testRetrieveMinistryTeams_ShouldReturnOkStatus() throws Exception {
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipTeam());
    this.mockMvc.perform(get(URL.BASE_URL_USERS + URL.MINISTRY_TEAMS)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_MINISTRY_TEAMS"))))
      .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testRetrieveUsers_GivenValidID_ShouldReturnOkStatus() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/" + entity.getEdxUserID().toString())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.edxUserSchools[0].edxUserSchoolRoles[0].edxRole.roleName", is("Admin")))
      .andExpect(jsonPath("$.edxUserSchools[0].edxUserSchoolRoles[0].edxRole.edxRolePermissions[0].edxPermission.permissionName", is("Exchange")))
      .andExpect(jsonPath("$.edxUserDistricts[0].edxUserDistrictRoles[0].edxRole.roleName", is("Admin")))
      .andExpect(jsonPath("$.edxUserDistricts[0].edxUserDistrictRoles[0].edxRole.edxRolePermissions[0].edxPermission.permissionName", is("Exchange")));
  }

  @Test
  public void testFindAllEdxUserSchoolMincodes_GivenValidPermissionName_ShouldReturnOkStatusAndMincodes() throws Exception {
    this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.mockMvc.perform(get(URL.BASE_URL_USERS + URL.USER_SCHOOL_MINCODES)
        .param("permissionName", "Exchange")
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USER_SCHOOLS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$.[0]", is("12345678")));
  }

  @Test
  public void testFindEdxUsers_GivenValidDigitalIdentityID_ShouldReturnOkStatusWithResult() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    UUID digitalIdentityID = entity.getDigitalIdentityID();
    this.mockMvc.perform(get(URL.BASE_URL_USERS)
        .param("digitalId", String.valueOf(digitalIdentityID))
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.[0].digitalIdentityID", is(digitalIdentityID.toString())));

  }

  @Test
  public void testFindEdxUsers_GivenNoDigitalIdentityIDAsInput_ShouldReturnError() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    UUID digitalIdentityID = entity.getDigitalIdentityID();
    this.mockMvc.perform(get(URL.BASE_URL_USERS)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testFindEdxUsers_GivenInValidDigitalIdentityID_ShouldReturnOkStatusWithEmptyResult() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    UUID digitalIdentityID = entity.getDigitalIdentityID();
    this.mockMvc.perform(get(URL.BASE_URL_USERS)
        .param("digitalId", UUID.randomUUID().toString())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$", Matchers.hasSize(0)));

  }


  @Test
  public void testCreateEdxUsers_GivenValidData_ShouldCreateEntity_AndReturnResultWithOkStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testCreateEdxUsers_GivenInValidData_MissingRequiredData_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    edxUser.setFirstName(null);
    String json = getJsonString(edxUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testCreateEdxUsers_GivenInValidData_PKIdColumnHasValue_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    edxUser.setEdxUserID(UUID.randomUUID().toString());
    String json = getJsonString(edxUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))))
      .andDo(print()).andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.subErrors[0].message", is("edxUserID should be null for post operation.")));

  }

  @Test
  public void testCreateEdxUsers_GivenInValidData_FirstNameMoreThan255Characters_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    edxUser.setFirstName("VeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTest");
    String json = getJsonString(edxUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))))
      .andDo(print()).andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.subErrors[0].field", is("firstName")))
      .andExpect(jsonPath("$.subErrors[0].message", is("First Name can have max 255 characters")));

  }

  @Test
  public void testCreateEdxUsers_GivenInValidData_LastNameMoreThan255Characters_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    edxUser.setLastName("VeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTestVeryLongUserNameTest");
    String json = getJsonString(edxUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))))
      .andDo(print()).andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.subErrors[0].field", is("lastName")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Last Name can have max 255 characters")));

  }

  @Test
  void testCreateEdxUsers_GivenInValidData_IncorrectEmailId_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    edxUser.setEmail("abc@test@test.coms");
    String json = getJsonString(edxUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))))
      .andDo(print()).andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.subErrors[0].field", is("email")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Email address should be a valid email address")));

  }

  @Test
  public void testCreateEdxUsers_GivenInValidData_NoEmailId_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    edxUser.setEmail(null);
    String json = getJsonString(edxUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))))
      .andDo(print()).andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.subErrors[0].field", is("email")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Email cannot be null")));

  }


  @Test
  public void testDeleteEdxUsers_GivenValidData_ShouldDelete_AndReturnResultWithNoContentStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());
    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}", edxUsr.getEdxUserID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USERS"))))
      .andDo(print()).andExpect(status().isNoContent());

  }

  @Test
  public void testDeleteEdxUsers_GivenInValidData_AndReturnResultWithNotFound() throws Exception {

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}", UUID.randomUUID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USERS"))))
      .andDo(print()).andExpect(status().isNotFound());

  }


  @Test
  public void testCreateEdxUsersSchool_GivenValidData_ShouldCreateEntity_AndReturnResultWithOkStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())

      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testCreateEdxUsersSchool_GivenInValidData_PKColumnNotNull_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    edxUserSchool.setEdxUserSchoolID(UUID.randomUUID().toString());
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())

        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonEdxUserSchool)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxUserSchoolID should be null for post operation.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }


  @Test
  public void testCreateEdxUsersSchool_GivenInValidData_EntityAlreadyExists_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonEdxUserSchool)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))))
      .andExpect(jsonPath("$.message", is("EdxUser to EdxUserSchool association already exists")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersSchool_GivenInValidData_EdxUserIdIsNullEdxUserSchool_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    edxUserSchool.setEdxUserID(null);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())

        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonEdxUserSchool)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxUserID should not be null for post operation.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersSchool_GivenInValidData_EdxUserIdMismatch_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    edxUserSchool.setEdxUserID(UUID.randomUUID().toString());
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())

        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonEdxUserSchool)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxUserID in path and payload edxUserId mismatch.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }


  @Test
  public void testDeleteEdxSchoolUsers_GivenInValidData_AndReturnResultWithNotFound() throws Exception {

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}", UUID.randomUUID(), UUID.randomUUID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USERS_SCHOOL"))))
      .andDo(print()).andExpect(status().isNotFound());


  }

  @Test
  public void testDeleteEdxSchoolUsers_GivenInValidData_CorrectEdxUserIdWithIncorrectEdxUserSchoolId_AndReturnResultWithNotFound() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());
    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);
    UUID randomId = UUID.randomUUID();
    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}", edxUsr.getEdxUserID(), randomId)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USERS_SCHOOL"))))
      .andDo(print()).andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message", is("EdxUserSchoolEntity was not found for parameters {edxUserSchoolID=" + randomId + "}")));
  }


  @Test
  public void testDeleteEdxSchoolUsers_GivenInValidData_CorrectEdxUserSchoolIdWithIncorrectEdxUserId_AndReturnResultWithNotFound() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());
    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    UUID randomId = UUID.randomUUID();
    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}", randomId, edxUsrSchool.getEdxUserSchoolID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USERS_SCHOOL"))))
      .andDo(print()).andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message", is("EdxUserEntity was not found for parameters {edxUserID=" + randomId + "}")));
  }

  @Test
  public void testDeleteEdxSchoolUsers_GivenValidData_WillDeleteRecord_AndReturnResultNoContentStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());
    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    ResultActions resultActions2 = this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}", edxUsr.getEdxUserID(), edxUsrSchool.getEdxUserSchoolID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USERS_SCHOOL"))))
      .andDo(print()).andExpect(status().isNoContent());
  }


  @Test
  public void testCreateEdxUsersSchoolRole_GivenValidData_ShouldCreateEntity_AndReturnResultWithOkStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    EdxUserSchoolRole edxUserSchoolRole = new EdxUserSchoolRole();
    edxUserSchoolRole.setEdxUserSchoolID(edxUsrSchool.getEdxUserSchoolID());
    edxUserSchoolRole.setEdxRole(EdxRoleMapper.mapper.toStructure(savedRoleEntity));
    String jsonRole = getJsonString(edxUserSchoolRole);


    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}" + "/role", edxUsr.getEdxUserID(), edxUsrSchool.getEdxUserSchoolID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonRole)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL_ROLE"))));
    resultActions2.andExpect(jsonPath("$.edxUserSchoolRoleID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchoolID", is(edxUsrSchool.getEdxUserSchoolID())))
      .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testCreateEdxUsersSchoolRole_GivenInValidData_UserSchoolRoleIDNottNull_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);


    EdxUserSchoolRole edxUserSchoolRole = new EdxUserSchoolRole();
    edxUserSchoolRole.setEdxUserSchoolRoleID(UUID.randomUUID().toString());
    edxUserSchoolRole.setEdxUserSchoolID(edxUsrSchool.getEdxUserSchoolID());
    edxUserSchoolRole.setEdxRole(EdxRoleMapper.mapper.toStructure(savedRoleEntity));
    String jsonRole = getJsonString(edxUserSchoolRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}" + "/role", edxUsr.getEdxUserID(), edxUsrSchool.getEdxUserSchoolID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonRole)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL_ROLE"))));
    resultActions2.andExpect(jsonPath("$.subErrors[0].message", is("edxUserSchoolRoleID should be null for post operation.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersSchoolRole_GivenInValidData_UserSchoolIDMismatch_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);


    EdxUserSchoolRole edxUserSchoolRole = new EdxUserSchoolRole();
    edxUserSchoolRole.setEdxUserSchoolID(UUID.randomUUID().toString());
    edxUserSchoolRole.setEdxRole(EdxRoleMapper.mapper.toStructure(savedRoleEntity));
    String jsonRole = getJsonString(edxUserSchoolRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}" + "/role", edxUsr.getEdxUserID(), edxUsrSchool.getEdxUserSchoolID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonRole)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL_ROLE"))));
    resultActions2.andExpect(jsonPath("$.subErrors[0].message", is("edxUserSchoolId in path and payload mismatch.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersSchoolRole_GivenInValidData_EdxUserSchoolDoesNotExist_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    String guid = UUID.randomUUID().toString();
    EdxUserSchoolRole edxUserSchoolRole = new EdxUserSchoolRole();
    edxUserSchoolRole.setEdxUserSchoolID(guid);
    edxUserSchoolRole.setEdxRole(EdxRoleMapper.mapper.toStructure(savedRoleEntity));
    String jsonRole = getJsonString(edxUserSchoolRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}" + "/role", edxUsr.getEdxUserID(), guid)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonRole)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL_ROLE"))));
    resultActions2.andExpect(jsonPath("$.message", is("EdxUserSchoolEntity was not found for parameters {edxUserSchoolId=" + guid + "}")))
      .andDo(print()).andExpect(status().isNotFound());

  }

  @Test
  public void testCreateEdxUsersSchoolRole_GivenInValidData_EdxUserDoesNotExist_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    String guid = UUID.randomUUID().toString();
    EdxUserSchoolRole edxUserSchoolRole = new EdxUserSchoolRole();
    edxUserSchoolRole.setEdxUserSchoolID(edxUsrSchool.getEdxUserSchoolID());
    edxUserSchoolRole.setEdxRole(EdxRoleMapper.mapper.toStructure(savedRoleEntity));
    String jsonRole = getJsonString(edxUserSchoolRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}" + "/role", guid, edxUsrSchool.getEdxUserSchoolID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonRole)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL_ROLE"))));
    resultActions2.andExpect(jsonPath("$.message", is("This EdxSchoolRole cannot be added for this EdxUser " + guid)))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersSchoolRole_GivenInValidData_EdxUserSchoolRoleAlreadyExists_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USERS"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonEdxUserSchool)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));
    resultActions1.andExpect(jsonPath("$.edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsrSchool = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    String guid = UUID.randomUUID().toString();
    EdxUserSchoolRole edxUserSchoolRole = new EdxUserSchoolRole();
    edxUserSchoolRole.setEdxUserSchoolID(edxUsrSchool.getEdxUserSchoolID());
    edxUserSchoolRole.setEdxRole(EdxRoleMapper.mapper.toStructure(savedRoleEntity));
    String jsonRole = getJsonString(edxUserSchoolRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}" + "/role", edxUsr.getEdxUserID(), edxUsrSchool.getEdxUserSchoolID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonRole)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL_ROLE"))));
    resultActions2.andExpect(jsonPath("$.edxUserSchoolRoleID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchoolID", is(edxUsrSchool.getEdxUserSchoolID())))
      .andDo(print()).andExpect(status().isCreated());

    val resultActions3 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}" + "/role", edxUsr.getEdxUserID(), edxUsrSchool.getEdxUserSchoolID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonRole)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL_ROLE"))));
    resultActions3.andExpect(jsonPath("$.message", is("EdxUserSchoolRole to EdxUserSchool association already exists")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  private MinistryOwnershipTeamEntity getMinistryOwnershipTeam() {
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
