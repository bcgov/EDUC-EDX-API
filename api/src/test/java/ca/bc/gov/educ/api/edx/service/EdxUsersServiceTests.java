package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

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

  @Before
  public void setUp() {
    var entity = this.edxUserRepository.save(getEdxUserEntity());
    this.edxUserSchoolRepository.save(getEdxUserSchoolEntity(entity.getEdxUserID()));
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("Test Team", "TEST_TEAM"));
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("New Team", "NEW_TEAM"));
  }

  @After
  public void tearDown() {
    this.edxUserRepository.deleteAll();
    this.edxUserSchoolRepository.deleteAll();
    this.ministryOwnershipTeamRepository.deleteAll();
  }

  @Test
  public void getAllMinistryTeams() {
    final List<MinistryOwnershipTeamEntity> teams = this.service.getMinistryTeamsList();
    assertThat(teams).isNotNull();
    assertThat(teams.size()).isEqualTo(2);
  }

  @Test
  public void getAllEdxUserSchools() {
    final List<EdxUserSchoolEntity> edxUserSchoolEntities = this.service.getEdxUserSchoolsList();
    assertThat(edxUserSchoolEntities).isNotNull();
    assertThat(edxUserSchoolEntities.size()).isEqualTo(1);
  }

  @Test
  public void retrieveEdxUser() {
    var entity = this.edxUserRepository.save(getEdxUserEntity());
    this.edxUserSchoolRepository.save(getEdxUserSchoolEntity(entity.getEdxUserID()));
    var edxUserSchoolEntities = this.service.retrieveEdxUserByID(entity.getEdxUserID());
    assertThat(edxUserSchoolEntities).isNotNull();
    assertThat(edxUserSchoolEntities.getEdxUserID()).isEqualTo(entity.getEdxUserID());
  }

  private EdxUserEntity getEdxUserEntity(){
    EdxUserEntity entity = new EdxUserEntity();
    entity.setDigitalIdentityID(UUID.randomUUID());
    entity.setFirstName("Test");
    entity.setLastName("User");
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  private EdxUserSchoolEntity getEdxUserSchoolEntity(UUID edxUserId){
    EdxUserSchoolEntity entity = new EdxUserSchoolEntity();
    entity.setEdxUserID(edxUserId);
    entity.setMincode("12345678");
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
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
