package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EdxUsersServiceTests extends BaseSecureExchangeAPITest {

  @Autowired
  EdxUsersService service;

  @Autowired
  private MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Autowired
  private EdxUserRepository edxUserRepository;

  @Autowired
  private EdxUserSchoolRepository edxUserSchoolRepository;

  @Autowired
  private EdxUserDistrictRepository edxUserDistrictRepository;

  @Autowired
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;

  @After
  public void tearDown() {
    this.edxUserRepository.deleteAll();
    this.edxUserSchoolRepository.deleteAll();
    this.ministryOwnershipTeamRepository.deleteAll();
    this.edxRoleRepository.deleteAll();
    this.edxPermissionRepository.deleteAll();
    this.edxUserDistrictRepository.deleteAll();
  }

  @Test
  public void getAllMinistryTeams() {
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("Test Team", "TEST_TEAM"));
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("New Team", "NEW_TEAM"));
    final List<MinistryOwnershipTeamEntity> teams = this.service.getMinistryTeamsList();
    assertThat(teams).isNotNull().hasSize(2);
  }

  @Test
  public void getAllEdxUserSchools() {
    var entity = this.edxUserRepository.save(getEdxUserEntity());
    this.edxUserSchoolRepository.save(getEdxUserSchoolEntity(entity));
    final List<EdxUserSchoolEntity> edxUserSchoolEntities = this.service.getEdxUserSchoolsList();
    assertThat(edxUserSchoolEntities).isNotNull().hasSize(1);
  }

  @Test
  public void retrieveEdxUser() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);

    var edxUserSchoolEntities = this.service.retrieveEdxUserByID(entity.getEdxUserID());
    assertThat(edxUserSchoolEntities).isNotNull();
    assertThat(edxUserSchoolEntities.getEdxUserID()).isEqualTo(entity.getEdxUserID());
    assertThat(edxUserSchoolEntities.getEdxUserSchoolEntities()).isNotNull();
    assertThat(edxUserSchoolEntities.getEdxUserSchoolEntities()).hasSize(1);
    var edxUserSchoolEntity = List.copyOf(edxUserSchoolEntities.getEdxUserSchoolEntities()).get(0);
    assertThat(edxUserSchoolEntity).isNotNull();
    assertThat(edxUserSchoolEntity.getEdxUserSchoolRoleEntities()).hasSize(1);
    var edxUserSchoolRollEntity = List.copyOf(edxUserSchoolEntity.getEdxUserSchoolRoleEntities()).get(0);
    assertThat(edxUserSchoolRollEntity).isNotNull();
    assertThat(edxUserSchoolRollEntity.getEdxRoleEntity()).isNotNull();
    assertThat(edxUserSchoolRollEntity.getEdxRoleEntity().getRoleName()).isEqualTo("Admin");
  }

  @Test
  public void getEdxUserSchoolsByPermissionName() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);

    final List<String> edxUserSchoolEntities = this.service.getEdxUserSchoolsList("Exchange");
    assertThat(edxUserSchoolEntities).isNotNull().hasSize(1);
    assertThat(edxUserSchoolEntities.get(0)).isEqualTo("12345678");
  }

  private MinistryOwnershipTeamEntity getMinistryOwnershipEntity(String teamName, String groupRoleIdentifier) {
    MinistryOwnershipTeamEntity entity = new MinistryOwnershipTeamEntity();
    entity.setTeamName(teamName);
    entity.setGroupRoleIdentifier(groupRoleIdentifier);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

}
