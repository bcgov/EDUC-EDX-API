package ca.bc.gov.educ.api.edx;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.SecureExchangeAPITestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.platform.commons.util.StringUtils;
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

    return edxUserRepository.findById(entity.getEdxUserID()).get();
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
    entity.setDistrictId(UUID.randomUUID());
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxRoleEntity getEdxRoleEntity() {
    EdxRoleEntity entity = new EdxRoleEntity();
    entity.setEdxRoleCode("Admin");
    entity.setLabel("Admin");
    entity.setIsDistrictRole(false);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxPermissionEntity getEdxPermissionEntity() {
    EdxPermissionEntity entity = new EdxPermissionEntity();
    entity.setEdxPermissionCode("Exchange");
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxRolePermissionEntity getEdxRolePermissionEntity(EdxRoleEntity edxRoleEntity, EdxPermissionEntity edxPermissionEntity) {
    EdxRolePermissionEntity entity = new EdxRolePermissionEntity();
    entity.setEdxRoleCode(edxRoleEntity.getEdxRoleCode());
    entity.setEdxPermissionCode(edxPermissionEntity.getEdxPermissionCode());
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxUserSchoolRoleEntity getEdxUserSchoolRoleEntity(EdxUserSchoolEntity edxUserSchoolEntity, EdxRoleEntity edxRoleEntity) {
    EdxUserSchoolRoleEntity entity = new EdxUserSchoolRoleEntity();
    entity.setEdxRoleCode(edxRoleEntity.getEdxRoleCode());
    entity.setEdxUserSchoolEntity(edxUserSchoolEntity);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected EdxUserDistrictRoleEntity getEdxUserDistrictRoleEntity(EdxUserDistrictEntity edxUserDistrictEntity, EdxRoleEntity edxRoleEntity) {
    EdxUserDistrictRoleEntity entity = new EdxUserDistrictRoleEntity();
    entity.setEdxRoleCode(edxRoleEntity.getEdxRoleCode());
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

  protected List<EdxActivationCodeEntity> createActivationCodeTableDataForSchoolUser(EdxActivationCodeRepository edxActivationCodeRepository, EdxPermissionRepository edxPermissionRepository, EdxRoleRepository edxRoleRepository, EdxActivationRoleRepository edxActivationRoleRepository, boolean isActive, UUID validationCode, boolean isURLClicked, String mincode) {
    List<EdxActivationCodeEntity> edxActivationCodeEntityList = new ArrayList<>();
    EdxRoleEntity savedRoleEntity = createRoleAndPermissionData(edxPermissionRepository, edxRoleRepository);

    var savedActivationCode = edxActivationCodeRepository.save(createEdxActivationCodeEntity("ABCDE", true, savedRoleEntity, isActive,validationCode,isURLClicked,mincode));

    var savedActivationCode1 = edxActivationCodeRepository.save(createEdxActivationCodeEntity("WXYZ", false, savedRoleEntity, isActive,validationCode,isURLClicked, mincode));
    edxActivationCodeEntityList.add(savedActivationCode);
    edxActivationCodeEntityList.add(savedActivationCode1);
    return edxActivationCodeEntityList;
  }

  protected List<EdxActivationCodeEntity> createActivationCodeTableDataForDistrictUser(EdxActivationCodeRepository edxActivationCodeRepository, EdxPermissionRepository edxPermissionRepository, EdxRoleRepository edxRoleRepository, EdxActivationRoleRepository edxActivationRoleRepository, boolean isActive, UUID validationCode, boolean isURLClicked, UUID districtID) {
    List<EdxActivationCodeEntity> edxActivationCodeEntityList = new ArrayList<>();
    EdxRoleEntity savedRoleEntity = createRoleAndPermissionData(edxPermissionRepository, edxRoleRepository);

    val primaryDistrictActivationCode = createEdxActivationCodeEntity("ABCDE", true, savedRoleEntity, isActive,validationCode,isURLClicked,null);
    primaryDistrictActivationCode.setDistrictId(districtID);
    var savedActivationCode = edxActivationCodeRepository.save(primaryDistrictActivationCode);
    val personalDistrictActivationCode = createEdxActivationCodeEntity("WXYZ", false, savedRoleEntity, isActive,validationCode,isURLClicked, null);
    personalDistrictActivationCode.setDistrictId(districtID);
    var savedActivationCode1 = edxActivationCodeRepository.save(personalDistrictActivationCode);
    edxActivationCodeEntityList.add(savedActivationCode);
    edxActivationCodeEntityList.add(savedActivationCode1);
    return edxActivationCodeEntityList;
  }

  protected EdxRoleEntity createRoleAndPermissionData(EdxPermissionRepository edxPermissionRepository, EdxRoleRepository edxRoleRepository) {
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    return edxRoleRepository.save(roleEntity);
  }

  protected EdxActivationRoleEntity createEdxActivationRoleEntity(EdxActivationCodeEntity edxActivationCodeEntity, EdxRoleEntity savedRoleEntity) {
    EdxActivationRoleEntity edxActivationRoleEntity = new EdxActivationRoleEntity();
    edxActivationRoleEntity.setEdxActivationCodeEntity(edxActivationCodeEntity);
    edxActivationRoleEntity.setEdxRoleCode(savedRoleEntity.getEdxRoleCode());
    edxActivationCodeEntity.getEdxActivationRoleEntities().add(edxActivationRoleEntity);
    return edxActivationRoleEntity;
  }

  protected EdxPrimaryActivationCode createEdxPrimaryActivationCodeForSchool(String mincode, String createUser, String updateUser) {
    EdxPrimaryActivationCode toReturn = this.createEdxPrimaryActivationCode(createUser, updateUser);
    toReturn.setMincode(mincode);
    toReturn.setDistrictId(null);
    return toReturn;
  }

  protected EdxPrimaryActivationCode createEdxPrimaryActivationCodeForDistrict(String districtCode, String createUser, String updateUser) {
    EdxPrimaryActivationCode toReturn = this.createEdxPrimaryActivationCode(createUser, updateUser);
    toReturn.setMincode(null);
    toReturn.setDistrictId(districtCode);
    return toReturn;
  }

  protected EdxPrimaryActivationCode createEdxPrimaryActivationCode(String createUser, String updateUser) {
    String currentTime = LocalDateTime.now().toString();
    EdxPrimaryActivationCode toReturn = new EdxPrimaryActivationCode();
    toReturn.setCreateUser(createUser);
    toReturn.setUpdateUser(updateUser);
    toReturn.setCreateDate(currentTime);
    toReturn.setUpdateDate(currentTime);
    return toReturn;
  }

  protected EdxActivationCodeEntity createEdxActivationCodeEntity(String activationCode, boolean isPrimary, boolean isActive, UUID validationCode, boolean isURLClicked, String mincode) {
    EdxActivationCodeEntity activationCodeEntity = new EdxActivationCodeEntity();
    activationCodeEntity.setMincode(mincode);
    activationCodeEntity.setActivationCode(activationCode);
    activationCodeEntity.setIsPrimary(isPrimary);
    activationCodeEntity.setValidationCode(validationCode);
    activationCodeEntity.setIsUrlClicked(isURLClicked);

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

    return activationCodeEntity;
  }

  protected EdxPrimaryActivationCode createEdxPrimaryActivationCode(String mincode, String createUser, String updateUser) {
    String currentTime = LocalDateTime.now().toString();
    EdxPrimaryActivationCode toReturn = new EdxPrimaryActivationCode();
    toReturn.setMincode(mincode);
    toReturn.setCreateUser(createUser);
    toReturn.setUpdateUser(updateUser);
    toReturn.setCreateDate(currentTime);
    toReturn.setUpdateDate(currentTime);
    return toReturn;
  }

  protected EdxActivationCodeEntity createEdxActivationCodeEntity(String activationCode, boolean isPrimary, EdxRoleEntity savedRoleEntity, boolean isActive, UUID validationCode, boolean isURLClicked, String mincode) {
    EdxActivationCodeEntity toReturn = createEdxActivationCodeEntity(activationCode, isPrimary, isActive, validationCode, isURLClicked, mincode);
    EdxActivationRoleEntity edxActivationRoleEntity = new EdxActivationRoleEntity();
    edxActivationRoleEntity.setEdxActivationCodeEntity(toReturn);
    edxActivationRoleEntity.setEdxRoleCode(savedRoleEntity.getEdxRoleCode());
    edxActivationRoleEntity.setCreateUser("test");
    edxActivationRoleEntity.setCreateDate(LocalDateTime.now());
    edxActivationRoleEntity.setUpdateUser("test");
    edxActivationRoleEntity.setUpdateDate(LocalDateTime.now());
    toReturn.getEdxActivationRoleEntities().add(edxActivationRoleEntity);
    return toReturn;
  }
  protected EdxActivationCodeEntity createEdxActivationCodeEntity(String activationCode, boolean isPrimary, boolean isActive, UUID validationCode, boolean isURLClicked, String mincode, String districtId) {
    EdxActivationCodeEntity activationCodeEntity = new EdxActivationCodeEntity();
    activationCodeEntity.setMincode(mincode);
    if(StringUtils.isNotBlank(districtId)){
      activationCodeEntity.setDistrictId(UUID.fromString(districtId));
    }
    activationCodeEntity.setActivationCode(activationCode);
    activationCodeEntity.setIsPrimary(isPrimary);
    activationCodeEntity.setValidationCode(validationCode);
    activationCodeEntity.setIsUrlClicked(isURLClicked);
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
    return activationCodeEntity;
  }

  protected EdxActivationCode createActivationCodeDetails(UUID validationCode, EdxRoleEntity edxRoleEntity,String activationCode, boolean isPrimary) {
    EdxActivationCode edxActivationCode = new EdxActivationCode();
    edxActivationCode.setValidationCode(validationCode.toString());
    edxActivationCode.setEmail("test@test.com");
    edxActivationCode.setFirstName("FirstName");
    edxActivationCode.setFirstName("LastName");
    edxActivationCode.setActivationCode(activationCode);
    edxActivationCode.setIsPrimary(String.valueOf(isPrimary));
    edxActivationCode.setExpiryDate(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(1).toString());
    edxActivationCode.setIsUrlClicked(String.valueOf(false));
    edxActivationCode.setMincode("12345678");

    EdxActivationRole edxActivationRole = new EdxActivationRole();
    edxActivationRole.setEdxRoleCode(edxRoleEntity.getEdxRoleCode());
    List<EdxActivationRole>activationRoles = new ArrayList<>();
    activationRoles.add(edxActivationRole);
    edxActivationCode.setEdxActivationRoles(activationRoles);
    return edxActivationCode;
  }

  /**
   * Creates 3 Roles and their associated Permissions and Role Permissions
   * @param edxRoleRepository
   * @param edxPermissionRepository
   */
  protected void createEdxRoleForSchoolAndDistrict(EdxRoleRepository edxRoleRepository, EdxPermissionRepository edxPermissionRepository) {

    EdxPermissionEntity secureExchangePermissionEntity =  edxPermissionRepository.save(createEdxPermissionForSchoolAndDistrict("SECURE_EXCHANGE"));
    EdxPermissionEntity adminPermissionEntity =  edxPermissionRepository.save(createEdxPermissionForSchoolAndDistrict("EDX_USER_ADMIN"));

    EdxRoleEntity secureExchangeRole = createEdxRoleForSchoolAndDistrict("SECURE_EXCHANGE","Secure Exchange",false);

    var secureExchangeRolePermissionEntity = getEdxRolePermissionEntity(secureExchangeRole, secureExchangePermissionEntity);
    secureExchangeRole.setEdxRolePermissionEntities(Set.of(secureExchangeRolePermissionEntity));
    edxRoleRepository.save(secureExchangeRole);

    EdxRoleEntity schoolAdminRole = createEdxRoleForSchoolAndDistrict("EDX_SCHOOL_ADMIN","Edx School Administrator",false);
    var schoolAdminSecureExchangeRolePermissionEntity = getEdxRolePermissionEntity(schoolAdminRole, secureExchangePermissionEntity);
    var schoolAdminAdminPermissionRolePermissionEntity = getEdxRolePermissionEntity(schoolAdminRole, adminPermissionEntity);
    schoolAdminRole.setEdxRolePermissionEntities(Set.of(schoolAdminSecureExchangeRolePermissionEntity,schoolAdminAdminPermissionRolePermissionEntity));
    edxRoleRepository.save(schoolAdminRole);

    EdxRoleEntity districtAdminRole = createEdxRoleForSchoolAndDistrict("EDX_DISTRICT_ADMIN","Edx District Administrator",true);
    var districtAdminSecureExchangeRolePermissionEntity = getEdxRolePermissionEntity(districtAdminRole, secureExchangePermissionEntity);
    var districtAdminAdminRolePermissionEntity = getEdxRolePermissionEntity(districtAdminRole, adminPermissionEntity);
    districtAdminRole.setEdxRolePermissionEntities(Set.of(districtAdminSecureExchangeRolePermissionEntity,districtAdminAdminRolePermissionEntity));
    edxRoleRepository.save(districtAdminRole);

  }

  private  EdxRoleEntity createEdxRoleForSchoolAndDistrict(String roleCode, String label,boolean isDistrict) {
    EdxRoleEntity entity = new EdxRoleEntity();
    entity.setEdxRoleCode(roleCode);
    entity.setLabel(label);
    entity.setIsDistrictRole(isDistrict);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  private  EdxPermissionEntity createEdxPermissionForSchoolAndDistrict(String permissionCode){
    EdxPermissionEntity entity = new EdxPermissionEntity();
    entity.setEdxPermissionCode(permissionCode);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  protected String getJsonString(Object obj) throws JsonProcessingException {
    return objectMapper.writeValueAsString(obj);
  }
}
