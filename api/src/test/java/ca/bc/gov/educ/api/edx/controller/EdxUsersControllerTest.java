package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxUsersController;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxRoleMapper;
import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.EDXUserControllerTestUtils;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EdxUsersControllerTest extends BaseSecureExchangeControllerTest {

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

  @Autowired
  private EdxActivationCodeRepository edxActivationCodeRepository;

  @Autowired
  private EdxActivationRoleRepository edxActivationRoleRepository;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Autowired
  private EDXUserControllerTestUtils edxUserControllerTestUtils;

  @After
  public  void after() {
    this.edxUserControllerTestUtils.cleanDB();
  }

  @Test
  public void testRetrieveMinistryTeams_ShouldReturnOkStatus() throws Exception {
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipTeam());
    this.mockMvc.perform(get(URL.BASE_URL_USERS + URL.MINISTRY_TEAMS)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_MINISTRY_TEAMS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.[0].description", is("THISISDESCRIPTION")));
  }

  @Test
  public void testRetrieveUsers_GivenValidID_ShouldReturnOkStatus() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/" + entity.getEdxUserID().toString())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.edxUserSchools[0].edxUserSchoolRoles[0].edxRoleCode", is("EDX_SCHOOL_ADMIN")))
      .andExpect(jsonPath("$.edxUserDistricts[0].edxUserDistrictRoles[0].edxRoleCode", is("EDX_SCHOOL_ADMIN")));
  }

  @Test
  public void testFindAllEdxUserSchoolIDs_GivenValidPermissionName_ShouldReturnOkStatusAndSchoolIDs() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.mockMvc.perform(get(URL.BASE_URL_USERS + URL.USER_SCHOOLS)
        .param("permissionCode", "Exchange")
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$", hasSize(1)))
      .andExpect(jsonPath("$.[0]", is(entity.getEdxUserSchoolEntities().iterator().next().getSchoolID().toString())));
  }

  @Test
  public void testFindAllEdxUserDistrictIDs_GivenValidPermissionName_ShouldReturnOkStatusAndDistrictIDs() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.mockMvc.perform(get(URL.BASE_URL_USERS + URL.USER_DISTRICTS)
                    .param("permissionCode", "Exchange")
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
            .andDo(print()).andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$.[0]", is(entity.getEdxUserDistrictEntities().iterator().next().getDistrictID().toString())));
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
  public void testFindEdxUsers_GivenValidFirstNameAndLastName_ShouldReturnOkStatusWithResult() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);

    this.mockMvc.perform(get(URL.BASE_URL_USERS)
        .param("firstName",entity.getFirstName())
        .param("lastName", entity.getLastName())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$.[0].firstName", is(entity.getFirstName())));

  }
  @Test
  public void testFindEdxUsers_GivenSchoolIDAsInput_ShouldReturnOkStatusWithResults() throws Exception {
    List<UUID> schoolIDList = new ArrayList<>();
    schoolIDList.add(UUID.randomUUID());

    this.createUserEntityWithMultipleSchools(this.edxUserRepository, this.edxPermissionRepository,
        this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository, schoolIDList);

    //should return user with all their schools and districts access.
    this.mockMvc.perform(get(URL.BASE_URL_USERS)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS")))
            .param("schoolID", schoolIDList.get(0).toString()))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].edxUserSchools[0].schoolID", Matchers.is(schoolIDList.get(0).toString())))
        .andExpect(jsonPath("$.[0].edxUserDistricts[0].districtID", is(notNullValue())));
  }
  @Test
  public void testFindEdxUsers_GivenNoInput_ShouldReturnOkStatusWithResult() throws Exception {
    this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository,
        this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.mockMvc.perform(get(URL.BASE_URL_USERS)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$", Matchers.hasSize(1)));
  }

  @Test
  public void testFindEdxUsers_GivenInvalidSchoolIDAsInput_ShouldReturnOkStatusWithEmptyResult() throws Exception {
    this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository,
        this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.mockMvc.perform(get(URL.BASE_URL_USERS)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS")))
            .param("schoolID", UUID.randomUUID().toString()))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$", Matchers.hasSize(0)));
  }

  @Test
  public void testFindEdxUsers_GivenInValidDigitalIdentityID_ShouldReturnOkStatusWithEmptyResult() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
  public void testCreateEdxUsers_GivenUserWithDigitalIdentityIdExists_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    EdxUser edxUser = createEdxUser();
    edxUser.setDigitalIdentityID(entity.getDigitalIdentityID().toString());
    String json = getJsonString(edxUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))))
      .andDo(print()).andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.message", containsString("digitalIdentityId must be unique")));
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
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))))
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
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))))
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
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))))
      .andDo(print()).andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.subErrors[0].field", is("lastName")))
      .andExpect(jsonPath("$.subErrors[0].message", is("Last Name can have max 255 characters")));

  }

  @Test
  public void testCreateEdxUsers_GivenInValidData_IncorrectEmailId_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    edxUser.setEmail("abc@test@test.coms");
    String json = getJsonString(edxUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))))
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
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))))
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());
    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}", edxUsr.getEdxUserID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER"))))
      .andDo(print()).andExpect(status().isNoContent());

  }

  @Test
  public void testDeleteEdxUsers_GivenInValidData_AndReturnResultWithNotFound() throws Exception {

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}", UUID.randomUUID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER"))))
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    edxUserSchool.setEdxUserSchoolID(UUID.randomUUID().toString());
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())

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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    edxUserSchool.setEdxUserID(null);
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())

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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    edxUserSchool.setEdxUserID(UUID.randomUUID().toString());
    String jsonEdxUserSchool = getJsonString(edxUserSchool);

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())

        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonEdxUserSchool)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxUserID in path and payload edxUserId mismatch.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testUpdateEdxUserSchool_GivenValidRoleData_ShouldUpdateEntityWithRole_AndReturnResultWithOkStatus_GivenNoRoleData_ShouldUpdateEntityWithoutRoles_AndReturnResultWithOkStatus() throws Exception {
    //create and save EdxUser with a school
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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

    //create/save our role and permission to attach to the school we created
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    EdxUserSchoolRole edxUserSchoolRole = new EdxUserSchoolRole();
    edxUserSchoolRole.setEdxUserSchoolID(edxUserSchool.getEdxUserSchoolID());
    edxUserSchoolRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());

    //now we update our school with the new role data
    edxUsrSchool.getEdxUserSchoolRoles().add(edxUserSchoolRole);
    edxUsrSchool.setUpdateDate(null);
    edxUsrSchool.setCreateDate(null);
    String jsonEdxUsrSchool = getJsonString(edxUsrSchool);

    val resultActions2 = this.mockMvc.perform(put(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonEdxUsrSchool).accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));

    resultActions2.andExpect(jsonPath("$.edxUserSchoolRoles", hasSize(1))).andDo(print()).andExpect(status().isOk());

    //now we update our school without any roles and it should remove the role
    edxUsrSchool.getEdxUserSchoolRoles().clear();
    String jsonEdxUsrSchoolWithoutRole = getJsonString(edxUsrSchool);
    val resultActions3 = this.mockMvc.perform(put(URL.BASE_URL_USERS + "/{id}" + "/school", edxUsr.getEdxUserID())
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonEdxUsrSchoolWithoutRole).accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_SCHOOL"))));

    resultActions3.andExpect(jsonPath("$.edxUserSchoolRoles", hasSize(0))).andDo(print()).andExpect(status().isOk());
  }

@Test
  public void testUpdateEdxUserSchool_GivenInvalidRoleData_ShouldBeBadRequest() throws Exception {
    //create and save EdxUser with a school
    EdxUser edxUser = createEdxUser();
    EdxUserSchool userSchool = createEdxUserSchool(edxUser);
    userSchool.setEdxUserSchoolRoles(new ArrayList<>());
    EdxUserSchoolRole role = new EdxUserSchoolRole();
    role.setEdxRoleCode("ABC");
    userSchool.getEdxUserSchoolRoles().add(role);
    edxUser.setEdxUserSchools(new ArrayList<>());
    edxUser.getEdxUserSchools().add(userSchool);
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andDo(print()).andExpect(status().isBadRequest());

    objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);
  }

  @Test
  public void testDeleteEdxSchoolUsers_GivenInValidData_AndReturnResultWithNotFound() throws Exception {

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}", UUID.randomUUID(), UUID.randomUUID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_SCHOOL"))))
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
    objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);
    UUID randomId = UUID.randomUUID();
    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}", edxUsr.getEdxUserID(), randomId)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_SCHOOL"))))
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_SCHOOL"))))
      .andDo(print()).andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message", is("EdxUserEntity was not found for parameters {edxUserID=" + randomId + "}")));
  }

  @Test
  public void testDeleteEdxSchoolUsers_GivenValidData_WillDeleteRecordAndChildrenUsingPreRemove_AndReturnResultNoContentStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserSchool edxUserSchool = createEdxUserSchool(edxUsr);
    var role = new EdxUserSchoolRole();
    role.setEdxRoleCode("EDX_SCHOOL_ADMIN");
    edxUserSchool.setEdxUserSchoolRoles(new ArrayList<>());
    edxUserSchool.getEdxUserSchoolRoles().add(role);
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

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/school/" + "{edxUserSchoolId}", edxUsr.getEdxUserID(), edxUsrSchool.getEdxUserSchoolID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_SCHOOL"))))
      .andDo(print()).andExpect(status().isNoContent());

    ResultActions response = this.mockMvc.perform(get(URL.BASE_URL_USERS + "/{id}", edxUsr.getEdxUserID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk());

    val edxUsrResponse = objectMapper.readValue(response.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);
    Assertions.assertTrue(edxUsrResponse.getEdxUserSchools().isEmpty());
  }


  @Test
  public void testCreateEdxUsersSchoolRole_GivenValidData_ShouldCreateEntity_AndReturnResultWithOkStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
    edxUserSchoolRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
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
  public void testCreateEdxUsersSchoolRole_GivenInvalidData_UserSchoolRoleIDNotNull_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
      .contentType(MediaType.APPLICATION_JSON)
      .content(json)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
    edxUserSchoolRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
    edxUserSchoolRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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

    objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserSchool.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    String guid = UUID.randomUUID().toString();
    EdxUserSchoolRole edxUserSchoolRole = new EdxUserSchoolRole();
    edxUserSchoolRole.setEdxUserSchoolID(guid);
    edxUserSchoolRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
    edxUserSchoolRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
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
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
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
    edxUserSchoolRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
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

  @Test
  public void testEdxActivateUsers_GivenValidInput_UserIsCreated_WithOkStatusResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    UUID schoolID = UUID.randomUUID();
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode, 2, schoolID);
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setSchoolID(schoolID);
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(UUID.randomUUID().toString());
    String activateUserJson = getJsonString(edxActivateUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolRoles", hasSize(1)))
      .andExpect(jsonPath("$.edxUserSchools[0].edxUserSchoolRoles[0].edxUserSchoolRoleID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testEdxActivateUsers_GivenInValidInput_ActivationCodeIsExpired_UserIsNotCreated_BadRequestResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    UUID schoolID = UUID.randomUUID();
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, false,validationCode, 2, schoolID);
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setSchoolID(schoolID);
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(UUID.randomUUID().toString());
    String activateUserJson = getJsonString(edxActivateUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.message", is("This Activation Code has expired")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testEdxActivateUsers_GivenInValidInput_NoActivationCodeDataPresentInDB_UserIsNotCreated_IsNotFoundResponseStatus() throws Exception {
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setSchoolID(UUID.randomUUID());
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(UUID.randomUUID().toString());
    String activateUserJson = getJsonString(edxActivateUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.message", is("EdxActivationCode was not found for parameters {edxActivationCodeId=" + edxActivateUser.getPrimaryEdxCode() + "}")))
      .andDo(print()).andExpect(status().isNotFound());

  }


  @Test
  public void testDeleteActivationCodeByUserId_GivenValidInput_WillReturnNoContentResponse()
    throws Exception {
    UUID validationCode = UUID.randomUUID();
    UUID edxUserID = UUID.randomUUID();
    UUID schoolID = UUID.randomUUID();

    this.createActivationCodeTableDataForSchoolUser(
      this.edxActivationCodeRepository,
      true,
      validationCode,
      2,
      schoolID,
      edxUserID
    );

    this.mockMvc.perform(
        delete(URL.BASE_URL_USERS + "/activation-code/user/" + edxUserID
          )
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isNoContent());
  }
  @Test
  public void testEdxActivateUsers_GivenValidInput_UserIsUpdated_WithAdditionalUseSchoolAndSchoolRole_WithOkStatusResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    UUID schoolID = UUID.randomUUID();
    EdxUserEntity userEntity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode, 2, schoolID);
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setSchoolID(schoolID);
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(userEntity.getDigitalIdentityID().toString());
    edxActivateUser.setUpdateUser("ABC");
    String activateUserJson = getJsonString(edxActivateUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andExpect(jsonPath("$.updateUser", is("ABC")))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolRoles", hasSize(1)))
      .andExpect(jsonPath("$.edxUserSchools[0].edxUserSchoolRoles[0].edxUserSchoolRoleID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchools.[1].edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchools.[1].edxUserSchoolRoles", hasSize(1)))
      .andExpect(jsonPath("$.edxUserSchools[1].edxUserSchoolRoles[0].edxUserSchoolRoleID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testEdxActivateUsers_GivenValidInput_UserIsUpdated_WithAdditionalUserDistrictAndDistrictRoles_WithOkStatusResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    EdxUserEntity userEntity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    UUID districtID = UUID.randomUUID();
    this.createActivationCodeTableDataForDistrictUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode, 2, districtID);
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setDistrictID(districtID);
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(userEntity.getDigitalIdentityID().toString());
    edxActivateUser.setUpdateUser("ABC");
    String activateUserJson = getJsonString(edxActivateUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
      .andExpect(jsonPath("$.updateUser", is("ABC")))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolRoles", hasSize(1)))
      .andExpect(jsonPath("$.edxUserSchools[0].edxUserSchoolRoles[0].edxUserSchoolRoleID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserDistricts.[0].edxUserDistrictID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserDistricts.[1].edxUserDistrictID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserDistricts.[1].edxUserDistrictRoles", hasSize(1)))
      .andExpect(jsonPath("$.edxUserDistricts[1].edxUserDistrictRoles[0].edxUserDistrictRoleID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testEdxActivateUsers_GivenInValidInput_WhereSchoolIDAndDistrictIdBothMissing_WithErrorResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    EdxUserEntity userEntity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    UUID districtID = UUID.randomUUID();
    this.createActivationCodeTableDataForDistrictUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode, 2, districtID);
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(userEntity.getDigitalIdentityID().toString());
    edxActivateUser.setUpdateUser("ABC");
    String activateUserJson = getJsonString(edxActivateUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))));
    resultActions
      .andExpect(jsonPath("$.subErrors[0].message", is("SchoolID or DistrictID Information is required for User Activation")))
            .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testEdxActivateUsers_GivenValidInput_EdxUserIdPresentInRequest_EdxUserIsUpdated_WithoutAnyAdditionalUseSchoolAndSchoolRoles_WithOkStatusResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    UUID schoolID = UUID.randomUUID();
    EdxUserEntity userEntity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode, 2, schoolID);
    String edxUserId = userEntity.getEdxUserID().toString();
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setSchoolID(schoolID);
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(userEntity.getDigitalIdentityID().toString());
    edxActivateUser.setEdxUserId(edxUserId);
    edxActivateUser.setUpdateUser("ABC");
    String activateUserJson = getJsonString(edxActivateUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))

      .andExpect(jsonPath("$.updateUser", is("ABC")))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolRoles", hasSize(1)))
      .andExpect(jsonPath("$.edxUserSchools[0].edxUserSchoolRoles[0].edxUserSchoolRoleID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testEdxActivateUsers_GivenValidInput_PresentInRequest_EdxUserIsUpdated_WithoutAnyAdditionalUseSchoolAndSchoolRoles_WithOkStatusResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    UUID schoolID = UUID.randomUUID();
    EdxUserEntity userEntity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode, 2, schoolID);
    String edxUserId = userEntity.getEdxUserID().toString();
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setSchoolID(schoolID);
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(UUID.randomUUID().toString());
    edxActivateUser.setEdxUserId(edxUserId);
    edxActivateUser.setUpdateUser("ABC");
    String activateUserJson = getJsonString(edxActivateUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))

      .andExpect(jsonPath("$.updateUser", is("ABC")))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolID", is(notNullValue())))
      .andExpect(jsonPath("$.edxUserSchools.[0].edxUserSchoolRoles", hasSize(1)))
      .andExpect(jsonPath("$.edxUserSchools[0].edxUserSchoolRoles[0].edxUserSchoolRoleID", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testEdxActivateUsers_GivenInValidInput_EdxUserAlreadyExistsWithTheSchoolIDInDB_EdxUserIsNotUpdated_WithConflictErrorResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    EdxUserEntity userEntity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true, validationCode, 2, userEntity.getEdxUserSchoolEntities().iterator().next().getSchoolID());
    EdxActivateUser edxActivateUser = new EdxActivateUser();
    edxActivateUser.setSchoolID(userEntity.getEdxUserSchoolEntities().iterator().next().getSchoolID());
    edxActivateUser.setPersonalActivationCode("WXYZ");
    edxActivateUser.setPrimaryEdxCode("ABCDE");
    edxActivateUser.setDigitalId(userEntity.getDigitalIdentityID().toString());
    String activateUserJson = getJsonString(edxActivateUser);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation")
      .contentType(MediaType.APPLICATION_JSON)
      .content(activateUserJson)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "ACTIVATE_EDX_USER"))))
      .andExpect(jsonPath("$.message", is("This user is already associated to the school")))
      .andDo(print()).andExpect(status().isConflict());
  }

  @Test
  public void testUpdateIsUrlClicked_GivenValidInput_EdxActivationCodeDataIsUpdatedAndReturn_WithOkStatusResponse() throws Exception {
  UUID validationCode = UUID.randomUUID();
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode, 0, UUID.randomUUID());
    EdxActivationCode edxActivationCode = new EdxActivationCode();
    edxActivationCode.setValidationCode(validationCode.toString());
    String jsonString = getJsonString(edxActivationCode);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/url")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(content().string("\"SCHOOL\""));


  }

  @Test
  public void testUpdatessssIsUrlClicked_GivenValidInput_EdxActivationCodeDataIsUpdatedAndReturn_WithOkStatusResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true, validationCode, 0, UUID.randomUUID());
    EdxActivationCode edxActivationCode = new EdxActivationCode();
    edxActivationCode.setValidationCode(validationCode.toString());
    String jsonString = getJsonString(edxActivationCode);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/url")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(content().string("\"SCHOOL\""));

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/url")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(content().string("\"SCHOOL\""));

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/url")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isGone());
  }

  @Test
  public void testUpdateIsUrlClicked_GivenValidationLinkIsAlreadyClicked_WillReturnErrorResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode,2, UUID.randomUUID());
    EdxActivationCode edxActivationCode = new EdxActivationCode();
    edxActivationCode.setValidationCode(validationCode.toString());
    String jsonString = getJsonString(edxActivationCode);

    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/url")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isGone());

      resultActions.andExpect(jsonPath("$.message", is("This User Activation Link has already expired")));



  }

  @Test
  public void testUpdateIsUrlClicked_GivenValidationLinkHasAlreadyExpired_WillReturnErrorResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, false,validationCode,2, UUID.randomUUID());
    EdxActivationCode edxActivationCode = new EdxActivationCode();
    edxActivationCode.setValidationCode(validationCode.toString());
    String jsonString = getJsonString(edxActivationCode);

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/url")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.message", is("This User Activation Link has already expired")))
      .andDo(print()).andExpect(status().isGone());

  }

  @Test
  public void testUpdateIsUrlClicked_GivenValidationLinkDoesNotExist_WillReturnErrorResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode,2, UUID.randomUUID());
    EdxActivationCode edxActivationCode = new EdxActivationCode();
    edxActivationCode.setValidationCode(UUID.randomUUID().toString());
    String jsonString = getJsonString(edxActivationCode);

    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/url")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());

    resultActions.andExpect(jsonPath("$.message", is("Invalid Link Provided")));

  }

  @Test
  public void testCreateActivationCode_GivenValidInput_ActivationCodeDataIsCreated_WithCreatedStatusResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    val edxRoleEntity  = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    EdxActivationCode edxActivationCode = createActivationCodeDetails(validationCode, edxRoleEntity,"ABCDE",true);
    String jsonString = getJsonString(edxActivationCode);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", is(notNullValue())))
      .andExpect(jsonPath("$.edxActivationRoles.[0].edxActivationRoleId", is(notNullValue())))
      .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testCreateActivationCode_GivenInValidInput_ActivationCodeDataContainsId_BadRequestInResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    val edxRoleEntity  = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    EdxActivationCode edxActivationCode = createActivationCodeDetails(validationCode, edxRoleEntity,"ABCDE",true);
    edxActivationCode.setEdxActivationCodeId(validationCode.toString());
    String jsonString = getJsonString(edxActivationCode);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxActivationCodeId should be null for post operation.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }
  @Test
  public void testCreateActivationCode_GivenInValidInput_ActivationRoleDataContainsId_BadRequestInResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    val edxRoleEntity  = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    EdxActivationCode edxActivationCode = createActivationCodeDetails(validationCode, edxRoleEntity,"ABCDE",true);
    val activationRole = edxActivationCode.getEdxActivationRoles().get(0);
    activationRole.setEdxActivationCodeId(validationCode.toString());
    activationRole.setEdxActivationRoleId(validationCode.toString());
    activationRole.setEdxRoleCode(null);
    String jsonString = getJsonString(edxActivationCode);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxActivationCodeId should be null for post operation.")))
      .andExpect(jsonPath("$.subErrors[1].message", is("edxActivationRoleId should be null for post operation.")))
      .andExpect(jsonPath("$.subErrors[2].message", is("edxRoleCode should not be null for post operation.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateActivationCode_GivenInValidInput_ContainsInvalidRoleId_BadRequestInResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    val edxRoleEntity  = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    EdxActivationCode edxActivationCode = createActivationCodeDetails(validationCode, edxRoleEntity,"ABCDE",true);
    val activationRole = edxActivationCode.getEdxActivationRoles().get(0);
    activationRole.setEdxRoleCode(validationCode.toString());
    String jsonString = getJsonString(edxActivationCode);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateActivationCode_GivenInValidInput_ActivationCodeHasNoActivationRolesData_BadRequestInResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    val edxRoleEntity  = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    EdxActivationCode edxActivationCode = createActivationCodeDetails(validationCode, edxRoleEntity,"ABCDE",true);
    edxActivationCode.getEdxActivationRoles().clear();
    String jsonString = getJsonString(edxActivationCode);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code")
        .contentType(MediaType.APPLICATION_JSON)
        .content(jsonString)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.message", is("Payload contains invalid data.")))
      .andExpect(jsonPath("$.subErrors[0].message", is("edxActivationRoles should be null for post operation.")))
      .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testDeleteActivationCode_GivenValidInput_WillReturnNoContentResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    val entityList  = this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode,2, UUID.randomUUID());

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/activation-code/"+entityList.get(0).getEdxActivationCodeId())
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isNoContent());

  }

  @Test
  public void testDeleteActivationCode_GivenInValidInput_WillReturnNotFoundErrorResponse() throws Exception {
    UUID validationCode = UUID.randomUUID();
    this.createActivationCodeTableDataForSchoolUser(this.edxActivationCodeRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxActivationRoleRepository, true,validationCode,2, UUID.randomUUID());

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/activation-code/"+validationCode)
        .contentType(MediaType.APPLICATION_JSON)
        .accept(MediaType.APPLICATION_JSON)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isNotFound())
      .andExpect(jsonPath("$.message", is("EdxActivationCodeEntity was not found for parameters {edxActivationCodeId="+validationCode+"}")));

  }

  @Test
  public void testFindPrimaryEdxActivationCode_ForSchool_GivenValidInput_WillReturnSuccess() throws Exception {
    EdxActivationCodeEntity primaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, UUID.randomUUID(), null));
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + primaryActivationCode.getSchoolID()).contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.schoolID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.schoolID", equalTo(primaryActivationCode.getSchoolID().toString())))
      .andExpect(jsonPath("$.districtID", is(nullValue())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testFindPrimaryEdxActivationCode_ForDistrict_GivenValidInput_WillReturnSuccess() throws Exception {
    EdxActivationCodeEntity primaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, UUID.randomUUID()));
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + primaryActivationCode.getDistrictID()).contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.schoolID", is(nullValue())))
      .andExpect(jsonPath("$.districtID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.districtID", equalTo(primaryActivationCode.getDistrictID().toString())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testFindPrimaryEdxActivationCode_ForSchool_OnlyReturnsPrimaryActivationCodes_WillReturnSuccess() throws Exception {
    UUID schoolID = UUID.randomUUID();
    EdxActivationCodeEntity primaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, schoolID));
    EdxActivationCodeEntity secondaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, schoolID));
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + secondaryActivationCode.getSchoolID()).contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", not(emptyOrNullString())))
      .andExpect(jsonPath("$.edxActivationCodeId", equalTo(primaryActivationCode.getEdxActivationCodeId().toString())))
      .andExpect(jsonPath("$.schoolID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.schoolID", equalTo(primaryActivationCode.getSchoolID().toString())))
      .andExpect(jsonPath("$.districtID", is(nullValue())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testFindPrimaryEdxActivationCode_ForDistrict_OnlyReturnsPrimaryActivationCodes_WillReturnSuccess() throws Exception {
    UUID districtID = UUID.randomUUID();
    EdxActivationCodeEntity primaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, districtID));
    EdxActivationCodeEntity secondaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, districtID));
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + secondaryActivationCode.getDistrictID()).contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", not(emptyOrNullString())))
      .andExpect(jsonPath("$.edxActivationCodeId", equalTo(primaryActivationCode.getEdxActivationCodeId().toString())))
      .andExpect(jsonPath("$.schoolID", is(nullValue())))
      .andExpect(jsonPath("$.districtID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.districtID", equalTo(primaryActivationCode.getDistrictID().toString())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testFindPrimaryEdxActivationCode_ForSchool_GivenInvalidInput_WillReturnNotFound() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testFindPrimaryEdxActivationCode_ForDistrict_GivenInvalidInput_WillReturnNotFound() throws Exception {
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + UUID.randomUUID()).contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testFindPrimaryEdxActivationCode_ForSchool_OnlyReturnsPrimaryActivationCodes_WillReturnNotFound() throws Exception {
    EdxActivationCodeEntity nonPrimaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, UUID.randomUUID(), null));
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + nonPrimaryActivationCode.getSchoolID()).contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testFindPrimaryEdxActivationCode_ForDistrict_OnlyReturnsPrimaryActivationCodes_WillReturnNotFound() throws Exception {
    EdxActivationCodeEntity nonPrimaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, UUID.randomUUID()));
    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + nonPrimaryActivationCode.getDistrictID()).contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isNotFound());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForSchool_GivenValidInput_SetsNewRandomActivationCode_WillReturnCreated() throws Exception {
    UUID schoolID = UUID.randomUUID();
    EdxActivationCodeEntity primaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, schoolID));
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForSchool(primaryActivationCode.getSchoolID(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + primaryActivationCode.getSchoolID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.activationCode", not(equalTo(primaryActivationCode.getActivationCode()))))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForDistrict_GivenValidInput_SetsNewRandomActivationCode_WillReturnCreated() throws Exception {
    EdxActivationCodeEntity primaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, UUID.randomUUID()));
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForDistrict(primaryActivationCode.getDistrictID(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + primaryActivationCode.getDistrictID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.activationCode", not(equalTo(primaryActivationCode.getActivationCode()))))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForSchool_GivenNewSchoolID_CreatesNewEdxActivationCode_WillReturnCreated() throws Exception {
    UUID schoolID = UUID.randomUUID();
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForSchool(schoolID, "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + schoolID)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", not(emptyOrNullString())))
      .andExpect(jsonPath("$.schoolID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.schoolID", equalTo(schoolID.toString())))
      .andExpect(jsonPath("$.districtID", is(nullValue())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForDistrict_GivenNewDistrictID_CreatesNewEdxActivationCode_WillReturnCreated() throws Exception {
    UUID districtID = UUID.randomUUID();
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForDistrict(districtID, "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + districtID)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", not(emptyOrNullString())))
      .andExpect(jsonPath("$.schoolID", is(nullValue())))
      .andExpect(jsonPath("$.districtID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.districtID", equalTo(districtID.toString())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForSchool_GivenExistingSchoolID_SetsNewRandomActivationCodeOfPrimary_WillReturnCreated() throws Exception {
    UUID schoolID = UUID.randomUUID();
    EdxActivationCodeEntity primaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, schoolID, null));
    EdxActivationCodeEntity secondaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, schoolID, null));
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForSchool(schoolID, "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + schoolID)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", not(emptyOrNullString())))
      .andExpect(jsonPath("$.edxActivationCodeId", equalTo(primaryActivationCode.getEdxActivationCodeId().toString())))
      .andExpect(jsonPath("$.edxActivationCodeId", not(equalTo(secondaryActivationCode.getEdxActivationCodeId().toString()))))
      .andExpect(jsonPath("$.schoolID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.schoolID", equalTo(schoolID.toString())))
      .andExpect(jsonPath("$.districtID", is(nullValue())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.activationCode", not(equalTo(primaryActivationCode.getActivationCode()))))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForDistrict_GivenExistingDistrictID_SetsNewRandomActivationCodeOfPrimary_WillReturnCreated() throws Exception {
    UUID districtID = UUID.randomUUID();
    EdxActivationCodeEntity primaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, districtID));
    EdxActivationCodeEntity secondaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, districtID));
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForDistrict(districtID, "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + districtID)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", not(emptyOrNullString())))
      .andExpect(jsonPath("$.edxActivationCodeId", equalTo(primaryActivationCode.getEdxActivationCodeId().toString())))
      .andExpect(jsonPath("$.edxActivationCodeId", not(equalTo(secondaryActivationCode.getEdxActivationCodeId().toString()))))
      .andExpect(jsonPath("$.schoolID", is(nullValue())))
      .andExpect(jsonPath("$.districtID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.districtID", equalTo(districtID.toString())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.activationCode", not(equalTo(primaryActivationCode.getActivationCode()))))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForSchool_GivenExistingSchoolID_WillOnlyGenerateActivationCodeOfPrimary_CreatesNewEdxActivationCode_WillReturnCreated() throws Exception {
    UUID schoolID = UUID.randomUUID();
    EdxActivationCodeEntity secondaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, schoolID, null));
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForSchool(schoolID, "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + schoolID)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", not(emptyOrNullString())))
      .andExpect(jsonPath("$.edxActivationCodeId", not(equalTo(secondaryActivationCode.getEdxActivationCodeId().toString()))))
      .andExpect(jsonPath("$.schoolID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.schoolID", equalTo(schoolID.toString())))
      .andExpect(jsonPath("$.districtID", is(nullValue())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.activationCode", not(equalTo(secondaryActivationCode.getActivationCode()))))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForDistrict_GivenExistingDistrictID_WillOnlyGenerateActivationCodeOfPrimary_CreatesNewEdxActivationCode_WillReturnCreated() throws Exception {
    UUID districtID = UUID.randomUUID();
    EdxActivationCodeEntity secondaryActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, districtID));
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForDistrict(districtID, "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + districtID)
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andExpect(jsonPath("$.edxActivationCodeId", not(emptyOrNullString())))
      .andExpect(jsonPath("$.edxActivationCodeId", not(equalTo(secondaryActivationCode.getEdxActivationCodeId().toString()))))
      .andExpect(jsonPath("$.schoolID", is(nullValue())))
      .andExpect(jsonPath("$.districtID", not(emptyOrNullString())))
      .andExpect(jsonPath("$.districtID", equalTo(districtID.toString())))
      .andExpect(jsonPath("$.activationCode", not(emptyOrNullString())))
      .andExpect(jsonPath("$.activationCode", not(equalTo(secondaryActivationCode.getActivationCode()))))
      .andExpect(jsonPath("$.isPrimary", equalTo("true")))
      .andDo(print()).andExpect(status().isCreated());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForSchool_GivenInvalidInput_WillReturnBadRequest() throws Exception {
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForSchool(UUID.randomUUID(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    toJsonStringify.setDistrictID(UUID.randomUUID());
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + toJsonStringify.getSchoolID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForDistrict_GivenInvalidInput_WillReturnBadRequest() throws Exception {
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForDistrict(UUID.randomUUID(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    toJsonStringify.setSchoolID(UUID.randomUUID());
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + toJsonStringify.getDistrictID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForSchool_GivenNullInput_WillReturnBadRequest() throws Exception {
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCode("EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForDistrict_GivenNullInput_WillReturnBadRequest() throws Exception {
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCode("EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    toJsonStringify.setSchoolID(UUID.randomUUID());
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForSchool_GivenDistrictIDInput_WillReturnBadRequest() throws Exception {
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCode("EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    toJsonStringify.setSchoolID(null);
    toJsonStringify.setDistrictID(UUID.randomUUID());
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForDistrict_GivenSchoolIDInput_WillReturnBadRequest() throws Exception {
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCode("EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    toJsonStringify.setSchoolID(UUID.randomUUID());
    toJsonStringify.setDistrictID(null);
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForSchool_GivenMismatchedSchoolIDInput_WillReturnBadRequest() throws Exception {
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForSchool(UUID.randomUUID(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/SCHOOL/" + UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testGenerateOrRegeneratePrimaryEdxActivationCode_ForDistrict_GivenMismatchedDistrictIDInput_WillReturnBadRequest() throws Exception {
    EdxPrimaryActivationCode toJsonStringify = this.createEdxPrimaryActivationCodeForDistrict(UUID.randomUUID(),"EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    String jsonString = getJsonString(toJsonStringify);
    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/activation-code/primary/DISTRICT/" + UUID.randomUUID())
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonString)
      .accept(MediaType.APPLICATION_JSON)
      .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_PRIMARY_ACTIVATION_CODE"))))
      .andDo(print()).andExpect(status().isBadRequest());
  }

  @Test
  public void testFindEdxRoles_GivenNoTypeCode_ShouldReturnOkStatusWithResult() throws Exception {
    this.createEdxRoleForSchoolAndDistrict(this.edxRoleRepository, this.edxPermissionRepository);

    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/roles")
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$",  hasSize(3)))
      .andExpect(jsonPath("$.[0].edxRoleCode", is("SECURE_EXCHANGE_SCHOOL")))
      .andExpect(jsonPath("$.[0].edxRolePermissions.[0].edxPermissionCode", is("SECURE_EXCHANGE")))
      .andExpect(jsonPath("$.[1].edxRoleCode", is("EDX_SCHOOL_ADMIN")))
      .andExpect(jsonPath("$.[1].edxRolePermissions", hasSize(2)))
      .andExpect(jsonPath("$.[2].edxRoleCode", is("EDX_DISTRICT_ADMIN")))
      .andExpect(jsonPath("$.[2].edxRolePermissions", hasSize(2)))   ;
  }


  @Test
  public void testFindEdxRoles_ForSchoolTypeInstitutionCode_ShouldReturnOkStatusWithResult() throws Exception {
    this.createEdxRoleForSchoolAndDistrict(this.edxRoleRepository, this.edxPermissionRepository);

    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/roles")
        .param("instituteType",InstituteTypeCode.SCHOOL.toString())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$",  hasSize(2)))
      .andExpect(jsonPath("$.[0].edxRoleCode", is("SECURE_EXCHANGE_SCHOOL")))
      .andExpect(jsonPath("$.[0].edxRolePermissions", hasSize(1)))
      .andExpect(jsonPath("$.[1].edxRoleCode", is("EDX_SCHOOL_ADMIN")))
      .andExpect(jsonPath("$.[1].edxRolePermissions",  hasSize(2)));

  }

    @Test
  public void testFindEdxRoles_ForSchoolTypeInstitutionCodeAllowList_ShouldReturnOkStatusWithResult() throws Exception {
    this.createEdxRoleForSchoolAndDistrict(this.edxRoleRepository, this.edxPermissionRepository);
    EdxPermissionEntity secureExchangePermissionEntity =  edxPermissionRepository.save(createEdxPermissionForSchoolAndDistrict("ABC_TEST"));
    EdxRoleEntity secureExchangeRole = createEdxRoleForSchoolAndDistrict("ABC_TEST","ABC Test",false);

    var secureExchangeRolePermissionEntity = getEdxRolePermissionEntity(secureExchangeRole, secureExchangePermissionEntity);
    secureExchangeRole.setEdxRolePermissionEntities(Set.of(secureExchangeRolePermissionEntity));
    edxRoleRepository.save(secureExchangeRole);

    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/roles")
        .param("instituteType",InstituteTypeCode.SCHOOL.toString())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$",  hasSize(2)))
      .andExpect(jsonPath("$.[0].edxRoleCode", is("SECURE_EXCHANGE_SCHOOL")))
      .andExpect(jsonPath("$.[0].edxRolePermissions", hasSize(1)))
      .andExpect(jsonPath("$.[1].edxRoleCode", is("EDX_SCHOOL_ADMIN")))
      .andExpect(jsonPath("$.[1].edxRolePermissions",  hasSize(2)));

  }

  @Test
  public void testFindEdxRoles_ForDistrictTypeInstitutionCode_ShouldReturnOkStatusWithResult() throws Exception {
    this.createEdxRoleForSchoolAndDistrict(this.edxRoleRepository, this.edxPermissionRepository);

    this.mockMvc.perform(get(URL.BASE_URL_USERS + "/roles")
        .param("instituteType", InstituteTypeCode.DISTRICT.toString())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
      .andDo(print()).andExpect(status().isOk())
      .andExpect(jsonPath("$",  hasSize(1)))
      .andExpect(jsonPath("$.[0].edxRoleCode", is("EDX_DISTRICT_ADMIN")))
      .andExpect(jsonPath("$.[0].edxRolePermissions",  hasSize(2)));

  }

  @Test
  public void testCreateEdxUserDistrict_GivenValidData_ShouldCreateEntity_AndReturnResultWithOkStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())

            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testCreateEdxUserDistrict_GivenInValidData_PKColumnNotNull_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    edxUserDistrict.setEdxUserDistrictID(UUID.randomUUID().toString());
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);


    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())

                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonEdxUserDistrict)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))))
            .andExpect(jsonPath("$.subErrors[0].message", is("edxUserDistrictID should be null for post operation.")))
            .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersDistrict_GivenInValidData_EntityAlreadyExists_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonEdxUserDistrict)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))))
            .andExpect(jsonPath("$.message", is("EdxUser to EdxUserDistrict association already exists")))
            .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersDistrict_GivenInValidData_EdxUserIdIsNullEdxUserSchool_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    edxUserDistrict.setEdxUserID(null);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())

                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonEdxUserDistrict)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))))
            .andExpect(jsonPath("$.subErrors[0].message", is("edxUserID should not be null for post operation.")))
            .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersDistrict_GivenInValidData_EdxUserIdMismatch_ShouldNotCreateEntity_AndReturnResultWithBadRequestStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    edxUserDistrict.setEdxUserID(UUID.randomUUID().toString());
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);

    this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())

                    .contentType(MediaType.APPLICATION_JSON)
                    .content(jsonEdxUserDistrict)
                    .accept(MediaType.APPLICATION_JSON)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))))
            .andExpect(jsonPath("$.subErrors[0].message", is("edxUserID in path and payload edxUserId mismatch.")))
            .andDo(print()).andExpect(status().isBadRequest());

  }
  @Test
  public void testUpdateEdxUserDistrict_GivenValidRoleData_ShouldUpdateEntityWithRole_AndReturnResultWithOkStatus_GivenNoRoleData_ShouldUpdateEntityWithoutRoles_AndReturnResultWithOkStatus() throws Exception {
    //create and save EdxUser with a district
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())

            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));

    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    //create/save our role and permission to attach to the district we created
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    EdxUserDistrictRole edxUserDistrictRole = new EdxUserDistrictRole();
    edxUserDistrictRole.setEdxUserDistrictID(userDistrict.getEdxUserDistrictID());
    edxUserDistrictRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());

    //now we update our district with the new role data
    userDistrict.getEdxUserDistrictRoles().add(edxUserDistrictRole);
    userDistrict.setUpdateDate(null);
    userDistrict.setCreateDate(null);
    String jsonEdxUsrDistrict = getJsonString(userDistrict);

    val resultActions2 = this.mockMvc.perform(put(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUsrDistrict).accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));

    resultActions2.andExpect(jsonPath("$.edxUserDistrictRoles", hasSize(1))).andDo(print()).andExpect(status().isOk());

    //now we update our district without any roles and it should remove the role
    userDistrict.getEdxUserDistrictRoles().clear();
    String jsonEdxUsrDistrictWithoutRole = getJsonString(userDistrict);
    val resultActions3 = this.mockMvc.perform(put(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUsrDistrictWithoutRole).accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));

    resultActions3.andExpect(jsonPath("$.edxUserDistrictRoles", hasSize(0))).andDo(print()).andExpect(status().isOk());
  }

  @Test
  public void testDeleteEdxUserDistrict_GivenInValidData_AndReturnResultWithNotFound() throws Exception {

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}", UUID.randomUUID(), UUID.randomUUID())
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_DISTRICT"))))
            .andDo(print()).andExpect(status().isNotFound());


  }

  @Test
  public void testDeleteEdxUserDistrict_GivenInValidData_CorrectEdxUserIdWithIncorrectEdxUserDistrictId_AndReturnResultWithNotFound() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());
    objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);
    UUID randomId = UUID.randomUUID();
    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}", edxUsr.getEdxUserID(), randomId)
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_DISTRICT"))))
            .andDo(print()).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("EdxUserDistrictEntity was not found for parameters {edxUserDistrictID=" + randomId + "}")));
  }

  @Test
  public void testDeleteEdxUserDistrict_GivenInValidData_CorrectEdxUserDistrictIdWithIncorrectEdxUserId_AndReturnResultWithNotFound() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());
    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    UUID randomId = UUID.randomUUID();
    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}", randomId, userDistrict.getEdxUserDistrictID())
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_DISTRICT"))))
            .andDo(print()).andExpect(status().isNotFound())
            .andExpect(jsonPath("$.message", is("EdxUserEntity was not found for parameters {edxUserID=" + randomId + "}")));
  }

  @Test
  public void testDeleteEdxUserDistrict_GivenValidData_WillDeleteRecordAndChildrenUsingPreRemove_AndReturnResultNoContentStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    var role = new EdxUserDistrictRole();
    role.setEdxRoleCode("EDX_ADMIN");
    edxUserDistrict.setEdxUserDistrictRoles(new ArrayList<>());
    edxUserDistrict.getEdxUserDistrictRoles().add(role);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);

    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());
    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}", edxUsr.getEdxUserID(), userDistrict.getEdxUserDistrictID())
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_DISTRICT"))))
            .andDo(print()).andExpect(status().isNoContent());

    ResultActions response = this.mockMvc.perform(get(URL.BASE_URL_USERS + "/{id}", edxUsr.getEdxUserID())
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
            .andDo(print()).andExpect(status().isOk());

    val edxUsrResponse = objectMapper.readValue(response.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);
    Assertions.assertTrue(edxUsrResponse.getEdxUserDistricts().isEmpty());
  }

  @Test
  public void testCreateEdxUsersDistrictRole_GivenValidData_ShouldCreateEntity_AndReturnResultWithOkStatus() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    EdxUserDistrictRole edxUserDistrictRole = new EdxUserDistrictRole();
    edxUserDistrictRole.setEdxUserDistrictID(userDistrict.getEdxUserDistrictID());
    edxUserDistrictRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
    String jsonRole = getJsonString(edxUserDistrictRole);


    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}" + "/role", edxUsr.getEdxUserID(), userDistrict.getEdxUserDistrictID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRole)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT_ROLE"))));
    resultActions2.andExpect(jsonPath("$.edxUserDistrictRoleID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserDistrictID", is(userDistrict.getEdxUserDistrictID())))
            .andDo(print()).andExpect(status().isCreated());

  }

  @Test
  public void testCreateEdxUsersDistrictRole_GivenInValidData_UserDistrictRoleIDNottNull_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);


    EdxUserDistrictRole EdxUserDistrictRole = new EdxUserDistrictRole();
    EdxUserDistrictRole.setEdxUserDistrictRoleID(UUID.randomUUID().toString());
    EdxUserDistrictRole.setEdxUserDistrictID(userDistrict.getEdxUserDistrictID());
    EdxUserDistrictRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
    String jsonRole = getJsonString(EdxUserDistrictRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}" + "/role", edxUsr.getEdxUserID(), userDistrict.getEdxUserDistrictID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRole)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT_ROLE"))));
    resultActions2.andExpect(jsonPath("$.subErrors[0].message", is("edxUserDistrictRoleID should be null for post operation.")))
            .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersDistrictRole_GivenInValidData_UserDistrictIDMismatch_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);


    EdxUserDistrictRole EdxUserDistrictRole = new EdxUserDistrictRole();
    EdxUserDistrictRole.setEdxUserDistrictID(UUID.randomUUID().toString());
    EdxUserDistrictRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
    String jsonRole = getJsonString(EdxUserDistrictRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}" + "/role", edxUsr.getEdxUserID(), userDistrict.getEdxUserDistrictID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRole)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT_ROLE"))));
    resultActions2.andExpect(jsonPath("$.subErrors[0].message", is("edxUserDistrictId in path and payload mismatch.")))
            .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersDistrictRole_GivenInValidData_EdxUserDistrictDoesNotExist_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    String guid = UUID.randomUUID().toString();
    EdxUserDistrictRole EdxUserDistrictRole = new EdxUserDistrictRole();
    EdxUserDistrictRole.setEdxUserDistrictID(guid);
    EdxUserDistrictRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
    String jsonRole = getJsonString(EdxUserDistrictRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}" + "/role", edxUsr.getEdxUserID(), guid)
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRole)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT_ROLE"))));
    resultActions2.andExpect(jsonPath("$.message", is("EdxUserDistrictEntity was not found for parameters {edxUserDistrictID=" + guid + "}")))
            .andDo(print()).andExpect(status().isNotFound());

  }


  @Test
  public void testCreateEdxUsersDistrictRole_GivenInValidData_EdxUserDoesNotExist_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    String guid = UUID.randomUUID().toString();
    EdxUserDistrictRole EdxUserDistrictRole = new EdxUserDistrictRole();
    EdxUserDistrictRole.setEdxUserDistrictID(userDistrict.getEdxUserDistrictID());
    EdxUserDistrictRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
    String jsonRole = getJsonString(EdxUserDistrictRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}" + "/role", guid, userDistrict.getEdxUserDistrictID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRole)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT_ROLE"))));
    resultActions2.andExpect(jsonPath("$.message", is("This EdxUserDistrictRole cannot be added for this EdxUser " + guid)))
            .andDo(print()).andExpect(status().isBadRequest());

  }

  @Test
  public void testCreateEdxUsersDistrictRole_GivenInValidData_EdxUserDistrictRoleAlreadyExists_ShouldNotCreateEntity_AndReturnResultWithBadRequest() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    EdxUserDistrictRole EdxUserDistrictRole = new EdxUserDistrictRole();
    EdxUserDistrictRole.setEdxUserDistrictID(userDistrict.getEdxUserDistrictID());
    EdxUserDistrictRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
    String jsonRole = getJsonString(EdxUserDistrictRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}" + "/role", edxUsr.getEdxUserID(), userDistrict.getEdxUserDistrictID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRole)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT_ROLE"))));
    resultActions2.andExpect(jsonPath("$.edxUserDistrictRoleID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserDistrictID", is(userDistrict.getEdxUserDistrictID())))
            .andDo(print()).andExpect(status().isCreated());

    val resultActions3 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}" + "/role", edxUsr.getEdxUserID(), userDistrict.getEdxUserDistrictID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRole)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT_ROLE"))));
    resultActions3.andExpect(jsonPath("$.message", is("EdxUserDistrictRole to EdxUserDistrict association already exists")))
            .andDo(print()).andExpect(status().isBadRequest());

  }


  @Test
  public void testDeleteEdxUsersDistrictRole_GivenValidData_EdxUserDistrictRoleIsDeleted() throws Exception {
    EdxUser edxUser = createEdxUser();
    String json = getJsonString(edxUser);
    val resultActions = this.mockMvc.perform(post(URL.BASE_URL_USERS)
            .contentType(MediaType.APPLICATION_JSON)
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER"))));
    resultActions.andExpect(jsonPath("$.edxUserID", is(notNullValue())))
            .andDo(print()).andExpect(status().isCreated());

    val edxUsr = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);

    EdxUserDistrict edxUserDistrict = createEdxUserDistrict(edxUsr);
    String jsonEdxUserDistrict = getJsonString(edxUserDistrict);
    val resultActions1 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district", edxUsr.getEdxUserID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonEdxUserDistrict)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT"))));
    resultActions1.andExpect(jsonPath("$.edxUserDistrictID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserID", is(edxUsr.getEdxUserID())))
            .andDo(print()).andExpect(status().isCreated());

    val userDistrict = objectMapper.readValue(resultActions1.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrict.class);

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    EdxUserDistrictRole EdxUserDistrictRole = new EdxUserDistrictRole();
    EdxUserDistrictRole.setEdxUserDistrictID(userDistrict.getEdxUserDistrictID());
    EdxUserDistrictRole.setEdxRoleCode(EdxRoleMapper.mapper.toStructure(savedRoleEntity).getEdxRoleCode());
    String jsonRole = getJsonString(EdxUserDistrictRole);

    val resultActions2 = this.mockMvc.perform(post(URL.BASE_URL_USERS + "/{id}" + "/district/" + "{edxUserDistrictID}" + "/role", edxUsr.getEdxUserID(), userDistrict.getEdxUserDistrictID())
            .contentType(MediaType.APPLICATION_JSON)
            .content(jsonRole)
            .accept(MediaType.APPLICATION_JSON)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_EDX_USER_DISTRICT_ROLE"))));
    resultActions2.andExpect(jsonPath("$.edxUserDistrictRoleID", is(notNullValue())))
            .andExpect(jsonPath("$.edxUserDistrictID", is(userDistrict.getEdxUserDistrictID())))
            .andDo(print()).andExpect(status().isCreated());

    val userDistrictRole = objectMapper.readValue(resultActions2.andReturn().getResponse().getContentAsByteArray(), EdxUserDistrictRole.class);

   this.mockMvc.perform(delete(URL.BASE_URL_USERS + "/{id}" + "/district/role/" + "{edxUserDistrictRoleID}", edxUsr.getEdxUserID(), userDistrictRole.getEdxUserDistrictRoleID())
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_EDX_USER_DISTRICT_ROLE"))))
            .andDo(print()).andExpect(status().isNoContent());

    ResultActions response = this.mockMvc.perform(get(URL.BASE_URL_USERS + "/{id}", edxUsr.getEdxUserID())
                    .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))))
            .andDo(print()).andExpect(status().isOk());
    val edxUsrResponse = objectMapper.readValue(response.andReturn().getResponse().getContentAsByteArray(), EdxUser.class);
    Assertions.assertFalse(edxUsrResponse.getEdxUserDistricts().isEmpty());
    Assertions.assertTrue(edxUsrResponse.getEdxUserDistricts().get(0).getEdxUserDistrictRoles().isEmpty());

  }

  private MinistryOwnershipTeamEntity getMinistryOwnershipTeam() {
    MinistryOwnershipTeamEntity entity = new MinistryOwnershipTeamEntity();
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    entity.setUpdateUser("JACK");
    entity.setCreateUser("JACK");
    entity.setUpdateDate(LocalDateTime.now());
    entity.setTeamName("JOHN");
    entity.setDescription("THISISDESCRIPTION");
    entity.setGroupRoleIdentifier("ABC");
    return entity;
  }


}
