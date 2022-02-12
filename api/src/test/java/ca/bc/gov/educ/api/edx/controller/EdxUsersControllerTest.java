package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxUsersController;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EdxUsersControllerTest extends BaseSecureExchangeControllerTest {

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

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
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
