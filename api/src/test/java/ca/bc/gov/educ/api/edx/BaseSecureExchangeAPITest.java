package ca.bc.gov.educ.api.edx;

import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.utils.SecureExchangeAPITestUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EdxApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseSecureExchangeAPITest {

  @Autowired
  protected SecureExchangeAPITestUtils secureExchangeAPITestUtils;

  @Before
  public void before() {
    this.secureExchangeAPITestUtils.cleanDB();
  }


  protected EdxUserEntity createUserEntity(EdxUserRepository edxUserRepository, EdxPermissionRepository edxPermissionRepository, EdxRoleRepository edxRoleRepository, EdxUserSchoolRepository edxUserSchoolRepository, EdxUserDistrictRepository edxUserDistrictRepository) {
    var entity = edxUserRepository.save(getEdxUserEntity());

    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    edxRoleRepository.save(roleEntity);

    var userSchoolEntity = getEdxUserSchoolEntity(entity.getEdxUserID());
    var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
    userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
    edxUserSchoolRepository.save(userSchoolEntity);

    var userDistrictEntity = getEdxUserDistrictEntity(entity.getEdxUserID());
    var userDistrictRoleEntity = getEdxUserDistrictRoleEntity(userDistrictEntity, roleEntity);
    userDistrictEntity.setEdxUserDistrictRoleEntities(Set.of(userDistrictRoleEntity));
    edxUserDistrictRepository.save(userDistrictEntity);

    return entity;
  }

  protected EdxUserEntity getEdxUserEntity(){
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

  protected EdxUserSchoolEntity getEdxUserSchoolEntity(UUID edxUserId){
    EdxUserSchoolEntity entity = new EdxUserSchoolEntity();
    entity.setEdxUserID(edxUserId);
    entity.setMincode("12345678");
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxUserDistrictEntity getEdxUserDistrictEntity(UUID edxUserId){
    EdxUserDistrictEntity entity = new EdxUserDistrictEntity();
    entity.setEdxUserID(edxUserId);
    entity.setDistrictCode("006");
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxRoleEntity getEdxRoleEntity() {
    EdxRoleEntity entity = new EdxRoleEntity();
    entity.setRoleName("Admin");
    entity.setIsDistrictRole(false);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxPermissionEntity getEdxPermissionEntity() {
    EdxPermissionEntity entity = new EdxPermissionEntity();
    entity.setPermissionName("Exchange");
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxRolePermissionEntity getEdxRolePermissionEntity(EdxRoleEntity edxRoleEntity, EdxPermissionEntity edxPermissionEntity) {
    EdxRolePermissionEntity entity = new EdxRolePermissionEntity();
    entity.setEdxRoleEntity(edxRoleEntity);
    entity.setEdxPermissionEntity(edxPermissionEntity);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxUserSchoolRoleEntity getEdxUserSchoolRoleEntity(EdxUserSchoolEntity edxUserSchoolEntity, EdxRoleEntity edxRoleEntity) {
    EdxUserSchoolRoleEntity entity = new EdxUserSchoolRoleEntity();
    entity.setEdxRoleEntity(edxRoleEntity);
    entity.setEdxUserSchoolEntity(edxUserSchoolEntity);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxUserDistrictRoleEntity getEdxUserDistrictRoleEntity(EdxUserDistrictEntity edxUserDistrictEntity, EdxRoleEntity edxRoleEntity) {
    EdxUserDistrictRoleEntity entity = new EdxUserDistrictRoleEntity();
    entity.setEdxRoleEntity(edxRoleEntity);
    entity.setEdxUserDistrictEntity(edxUserDistrictEntity);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }
}
