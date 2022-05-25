package ca.bc.gov.educ.api.edx;

import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchool;
import ca.bc.gov.educ.api.edx.utils.SecureExchangeAPITestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EdxApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseSecureExchangeAPITest {
  protected final static ObjectMapper objectMapper = new ObjectMapper();
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

    var userSchoolEntity = getEdxUserSchoolEntity(entity);
    var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
    userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
    edxUserSchoolRepository.save(userSchoolEntity);

    var userDistrictEntity = getEdxUserDistrictEntity(entity);
    var userDistrictRoleEntity = getEdxUserDistrictRoleEntity(userDistrictEntity, roleEntity);
    userDistrictEntity.setEdxUserDistrictRoleEntities(Set.of(userDistrictRoleEntity));
    edxUserDistrictRepository.save(userDistrictEntity);

    return entity;
  }

  protected EdxUserEntity createUserEntityWithMultipleSchools(EdxUserRepository edxUserRepository, EdxPermissionRepository edxPermissionRepository, EdxRoleRepository edxRoleRepository, EdxUserSchoolRepository edxUserSchoolRepository, EdxUserDistrictRepository edxUserDistrictRepository, List<String> mincodes) {
    var entity = edxUserRepository.save(getEdxUserEntity());

    //creating and saving role/permission entities
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    edxRoleRepository.save(roleEntity);

    for (String mincode : mincodes) {
      var userSchoolEntity = getEdxUserSchoolEntity(entity, mincode);
      var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
      userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
      edxUserSchoolRepository.save(userSchoolEntity);
    }

    var userDistrictEntity = getEdxUserDistrictEntity(entity);
    var userDistrictRoleEntity = getEdxUserDistrictRoleEntity(userDistrictEntity, roleEntity);
    userDistrictEntity.setEdxUserDistrictRoleEntities(Set.of(userDistrictRoleEntity));
    edxUserDistrictRepository.save(userDistrictEntity);

    return edxUserRepository.getById(entity.getEdxUserID());
  }

  protected EdxUserEntity getEdxUserEntity() {
    EdxUserEntity entity = new EdxUserEntity();
    entity.setDigitalIdentityID(UUID.randomUUID());
    entity.setFirstName("Test");
    entity.setLastName("User");
    entity.setEmail("test@email.com");
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());

    return entity;
  }

  protected EdxUserSchoolEntity getEdxUserSchoolEntity(EdxUserEntity edxUserEntity) {
    EdxUserSchoolEntity entity = new EdxUserSchoolEntity();
    entity.setEdxUserEntity(edxUserEntity);
    entity.setMincode("12345678");
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxUserSchoolEntity getEdxUserSchoolEntity(EdxUserEntity edxUserEntity, String mincode) {
    EdxUserSchoolEntity entity = new EdxUserSchoolEntity();
    entity.setEdxUserEntity(edxUserEntity);
    entity.setMincode(mincode);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxUserDistrictEntity getEdxUserDistrictEntity(EdxUserEntity edxUserEntity) {
    EdxUserDistrictEntity entity = new EdxUserDistrictEntity();
    entity.setEdxUserEntity(edxUserEntity);
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

  protected EdxUser createEdxUser() {
    EdxUser edxUser = new EdxUser();
    edxUser.setFirstName("TestFirst");
    edxUser.setLastName("TestLast");
    edxUser.setDigitalIdentityID(UUID.randomUUID().toString());
    edxUser.setEmail("test@email.com");
    edxUser.setCreateUser("Test");
    return edxUser;
  }

  protected EdxUserSchool createEdxUserSchool(EdxUser edxUsr) {
    EdxUserSchool edxUserSchool = new EdxUserSchool();
    edxUserSchool.setEdxUserID(edxUsr.getEdxUserID());
    edxUserSchool.setMincode("123456");
    return edxUserSchool;
  }

  protected List<EdxActivationCodeEntity> createActivationCodeTableData(EdxActivationCodeRepository edxActivationCodeRepository, EdxPermissionRepository edxPermissionRepository, EdxRoleRepository edxRoleRepository, EdxActivationRoleRepository edxActivationRoleRepository, boolean isActive) {
    List<EdxActivationCodeEntity> edxActivationCodeEntityList = new ArrayList<>();
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    var savedRoleEntity = edxRoleRepository.save(roleEntity);

    var savedActivationCode = edxActivationCodeRepository.save(createEdxActivationCodeEntity("ABCDE", true, savedRoleEntity, isActive));

    var savedActivationCode1 = edxActivationCodeRepository.save(createEdxActivationCodeEntity("WXYZ", false, savedRoleEntity, isActive));
    edxActivationCodeEntityList.add(savedActivationCode);
    edxActivationCodeEntityList.add(savedActivationCode1);
    return edxActivationCodeEntityList;
  }

  private EdxActivationRoleEntity createEdxActivationRoleEntity(EdxActivationCodeEntity edxActivationCodeEntity, EdxRoleEntity savedRoleEntity) {
    EdxActivationRoleEntity edxActivationRoleEntity = new EdxActivationRoleEntity();
    edxActivationRoleEntity.setEdxActivationCodeEntity(edxActivationCodeEntity);
    edxActivationRoleEntity.setEdxRoleId(savedRoleEntity.getEdxRoleID());
    edxActivationCodeEntity.getEdxActivationRoleEntities().add(edxActivationRoleEntity);
    return edxActivationRoleEntity;
  }

  private EdxActivationCodeEntity createEdxActivationCodeEntity(String activationCode, boolean isPrimary, EdxRoleEntity savedRoleEntity, boolean isActive) {
    EdxActivationCodeEntity activationCodeEntity = new EdxActivationCodeEntity();
    activationCodeEntity.setMincode("1234567");
    activationCodeEntity.setActivationCode(activationCode);
    activationCodeEntity.setIsPrimary(isPrimary);
    if (isActive) {
      LocalDateTime tomorrowMidnight = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(1);

      activationCodeEntity.setExpiryDate(tomorrowMidnight);
    } else {
      activationCodeEntity.setExpiryDate(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).minusDays(1));
    }

    activationCodeEntity.setFirstName("FirstName");
    activationCodeEntity.setLastName("Lastname");
    activationCodeEntity.setEmail("test@test.com");

    activationCodeEntity.setCreateUser("test");
    activationCodeEntity.setCreateDate(LocalDateTime.now());
    activationCodeEntity.setUpdateUser("test");
    activationCodeEntity.setUpdateDate(LocalDateTime.now());

    EdxActivationRoleEntity edxActivationRoleEntity = new EdxActivationRoleEntity();
    edxActivationRoleEntity.setEdxActivationCodeEntity(activationCodeEntity);
    edxActivationRoleEntity.setEdxRoleId(savedRoleEntity.getEdxRoleID());
    edxActivationRoleEntity.setCreateUser("test");
    edxActivationRoleEntity.setCreateDate(LocalDateTime.now());
    edxActivationRoleEntity.setUpdateUser("test");
    edxActivationRoleEntity.setUpdateDate(LocalDateTime.now());

    activationCodeEntity.getEdxActivationRoleEntities().add(edxActivationRoleEntity);
    return activationCodeEntity;
  }

  protected String getJsonString(Object obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }
}
