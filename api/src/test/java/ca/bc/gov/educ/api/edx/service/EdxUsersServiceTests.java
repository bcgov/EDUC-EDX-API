package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxPrimaryActivationCode;
import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

  @Autowired
  private EdxUserDistrictRepository edxUserDistrictRepository;

  @Autowired
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;

  @Autowired
  private EdxActivationCodeRepository edxActivationCodeRepository;

  @After
  public void tearDown() {
    this.edxUserSchoolRepository.deleteAll();
    this.edxUserRepository.deleteAll();
    this.ministryOwnershipTeamRepository.deleteAll();
    this.edxRoleRepository.deleteAll();
    this.edxPermissionRepository.deleteAll();
    this.edxUserDistrictRepository.deleteAll();
    this.edxActivationCodeRepository.deleteAll();
  }

  @Test
  public void getAllMinistryTeams() {
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("Test Team", "TEST_TEAM"));
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("New Team", "NEW_TEAM"));
    final List<MinistryOwnershipTeamEntity> teams = this.service.getMinistryTeamsList();
    assertThat(teams).isNotNull().hasSize(2);
    assertThat(teams.get(0).getDescription()).isEqualTo("Description");
  }

  @Test
  public void getAllEdxUserSchools() {
    this.edxUserSchoolRepository.deleteAll();
    var entity = this.edxUserRepository.save(getEdxUserEntity());
    this.edxUserSchoolRepository.save(getEdxUserSchoolEntity(entity));
    final List<EdxUserSchoolEntity> edxUserSchoolEntities = this.service.getEdxUserSchoolsList();
    assertThat(edxUserSchoolEntities).isNotNull().hasSize(1);
  }

  @Test
  public void findEdxUserByDigitalID() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    var schoolEntity = getEdxUserSchoolEntity(entity);
    schoolEntity.setMincode("98765432");
    edxUserSchoolRepository.save(schoolEntity);

    var edxUserEntities = this.service.findEdxUsers(Optional.of(entity.getDigitalIdentityID()),null, null, null);
    assertThat(edxUserEntities).isNotNull().hasSize(1);
  }

  @Test
  public void findEdxUserByMincodeID() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    var schoolEntity = getEdxUserSchoolEntity(entity);
    schoolEntity.setMincode("98765432");
    edxUserSchoolRepository.save(schoolEntity);

    var edxUserEntities = this.service.findEdxUsers(Optional.ofNullable(null),schoolEntity.getMincode(), null, null);
    assertThat(edxUserEntities).isNotNull().hasSize(1);
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
    assertThat(edxUserSchoolRollEntity.getEdxRoleCode()).isNotNull();
    assertThat(edxUserSchoolRollEntity.getEdxRoleCode()).isEqualTo("Admin");
  }

  @Test
  public void getEdxUserSchoolsByPermissionName() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);

    final List<String> edxUserSchoolEntities = this.service.getEdxUserSchoolsList("Exchange");
    assertThat(edxUserSchoolEntities).isNotNull().hasSize(1);
    assertThat(edxUserSchoolEntities.get(0)).isEqualTo("12345678");
  }

  @Test
  public void findPrimaryEdxActivationCodeForSchool() {
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, UUID.randomUUID().toString(), null));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, primaryToFind.getMincode());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  public void findPrimaryEdxActivationCodeForDistrict() {
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, null, UUID.randomUUID().toString()));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, primaryToFind.getDistrictCode());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  public void findPrimaryEdxActivationCodeOutOfManyForSchool() {
    String mincode = UUID.randomUUID().toString();
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, mincode, null));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), false, mincode, null));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, primaryToFind.getMincode());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  public void findPrimaryEdxActivationCodeOutOfManyForDistrict() {
    String districtCode = UUID.randomUUID().toString();
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, null, districtCode));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), false, null, districtCode));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, primaryToFind.getDistrictCode());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test(expected = EntityNotFoundException.class)
  public void findPrimaryEdxActivationCodeOnlyReturnsPrimaryEdxActivationCodeForSchool() {
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), false, UUID.randomUUID().toString(), null));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, secondaryEdxActivationCode.getMincode());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
  }

  @Test(expected = EntityNotFoundException.class)
  public void findPrimaryEdxActivationCodeOnlyReturnsPrimaryEdxActivationCodeForDistrict() {
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), false, null, UUID.randomUUID().toString()));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, secondaryEdxActivationCode.getDistrictCode());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
  }

  @Test(expected = EntityNotFoundException.class)
  public void findPrimaryEdxActivationCodeCannotFindNonExistingEdxActivationCodeForSchool() {
    this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, UUID.randomUUID().toString());
  }

  @Test(expected = EntityNotFoundException.class)
  public void findPrimaryEdxActivationCodeCannotFindNonExistingEdxActivationCodeForDistrict() {
    this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, UUID.randomUUID().toString());
  }

  @Test
  public void generatePrimaryEdxActivationCodeForSchool() {
    EdxPrimaryActivationCode toGenerate = this.createEdxPrimaryActivationCodeForSchool(UUID.randomUUID().toString(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    EdxActivationCodeEntity generated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, toGenerate.getMincode(), toGenerate);
    assertThat(generated.getMincode()).isNotNull().isNotEmpty().isEqualTo(toGenerate.getMincode());
    assertThat(generated.getDistrictCode()).isNull();
    assertThat(generated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8);
    assertThat(generated.getIsPrimary()).isTrue();
    assertThat(generated.getCreateUser()).isEqualTo(toGenerate.getCreateUser());
    assertThat(generated.getUpdateUser()).isEqualTo(toGenerate.getUpdateUser());
  }

  @Test
  public void generatePrimaryEdxActivationCodeForDistrict() {
    EdxPrimaryActivationCode toGenerate = this.createEdxPrimaryActivationCodeForDistrict(UUID.randomUUID().toString(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    EdxActivationCodeEntity generated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, toGenerate.getDistrictCode(), toGenerate);
    assertThat(generated.getMincode()).isNull();
    assertThat(generated.getDistrictCode()).isNotNull().isNotEmpty().isEqualTo(toGenerate.getDistrictCode());
    assertThat(generated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8);
    assertThat(generated.getIsPrimary()).isTrue();
    assertThat(generated.getCreateUser()).isEqualTo(toGenerate.getCreateUser());
    assertThat(generated.getUpdateUser()).isEqualTo(toGenerate.getUpdateUser());
  }

  @Test
  public void generateEdxActivationCode() throws NoSuchAlgorithmException {
    var edxActivationCodeEntity = this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, UUID.randomUUID().toString(), null);
    UUID user = UUID.randomUUID();
    edxActivationCodeEntity.setEdxUserId(user);
    EdxActivationCodeEntity activationCode = this.edxActivationCodeRepository.save(edxActivationCodeEntity);
    EdxActivationCodeEntity generated = this.service.createPersonalEdxActivationCode(activationCode);
    assertThat(generated.getMincode()).isNotNull().isNotEmpty().isEqualTo(edxActivationCodeEntity.getMincode());
    assertThat(generated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8);
    assertThat(generated.getIsPrimary()).isFalse();
    assertThat(generated.getEdxUserId()).isEqualTo(user);
    assertThat(generated.getCreateUser()).isEqualTo(edxActivationCodeEntity.getCreateUser());
    assertThat(generated.getUpdateUser()).isEqualTo(edxActivationCodeEntity.getUpdateUser());
  }

  @Test
  public void regeneratePrimaryEdxActivationCodeForSchool() {
    String mincode = UUID.randomUUID().toString();
    EdxActivationCodeEntity existing = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, mincode, null));
    EdxPrimaryActivationCode toRegenerate = this.createEdxPrimaryActivationCodeForSchool(mincode, "EDX-API-UNIT-TEST-UPDATE-USER", "EDX-API-UNIT-TEST-UPDATE-USER");
    EdxActivationCodeEntity regenerated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, mincode, toRegenerate);
    assertThat(regenerated.getEdxActivationCodeId()).isNotNull().isEqualTo(existing.getEdxActivationCodeId());
    assertThat(regenerated.getMincode()).isNotNull().isNotEmpty().isEqualTo(toRegenerate.getMincode()).isEqualTo(existing.getMincode());
    assertThat(regenerated.getDistrictCode()).isNull();
    assertThat(regenerated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8).isNotEqualTo(existing.getActivationCode());
    assertThat(regenerated.getIsPrimary()).isTrue();
    assertThat(regenerated.getCreateUser()).isEqualTo(existing.getCreateUser());
    assertThat(regenerated.getUpdateUser()).isEqualTo(toRegenerate.getUpdateUser());
  }

  @Test
  public void regeneratePrimaryEdxActivationCodeForDistrict() {
    String districtCode = UUID.randomUUID().toString();
    EdxActivationCodeEntity existing = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, null, districtCode));
    EdxPrimaryActivationCode toRegenerate = this.createEdxPrimaryActivationCodeForDistrict(districtCode, "EDX-API-UNIT-TEST-UPDATE-USER", "EDX-API-UNIT-TEST-UPDATE-USER");
    EdxActivationCodeEntity regenerated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, districtCode, toRegenerate);
    assertThat(regenerated.getEdxActivationCodeId()).isNotNull().isEqualTo(existing.getEdxActivationCodeId());
    assertThat(regenerated.getMincode()).isNull();
    assertThat(regenerated.getDistrictCode()).isNotNull().isNotEmpty().isEqualTo(toRegenerate.getDistrictCode()).isEqualTo(existing.getDistrictCode());
    assertThat(regenerated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8).isNotEqualTo(existing.getActivationCode());
    assertThat(regenerated.getIsPrimary()).isTrue();
    assertThat(regenerated.getCreateUser()).isEqualTo(existing.getCreateUser());
    assertThat(regenerated.getUpdateUser()).isEqualTo(toRegenerate.getUpdateUser());
  }

  @Test
  public void regeneratePrimaryEdxActivationCodeOutOfManyForSchool() {
    String mincode = UUID.randomUUID().toString();
    EdxActivationCodeEntity existingPrimaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, mincode, null));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), false, mincode, null));
    EdxPrimaryActivationCode toRegenerate = this.createEdxPrimaryActivationCodeForSchool(mincode, "EDX-API-UNIT-TEST-UPDATE-USER", "EDX-API-UNIT-TEST-UPDATE-USER");
    EdxActivationCodeEntity regenerated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, mincode, toRegenerate);
    assertThat(regenerated.getEdxActivationCodeId()).isNotNull().isEqualTo(existingPrimaryEdxActivationCode.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(regenerated.getMincode()).isNotNull().isNotEmpty().isEqualTo(toRegenerate.getMincode()).isEqualTo(existingPrimaryEdxActivationCode.getMincode()).isEqualTo(secondaryEdxActivationCode.getMincode());
    assertThat(regenerated.getDistrictCode()).isNull();
    assertThat(regenerated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8).isNotEqualTo(existingPrimaryEdxActivationCode.getActivationCode());
    assertThat(regenerated.getIsPrimary()).isTrue();
    assertThat(regenerated.getCreateUser()).isEqualTo(existingPrimaryEdxActivationCode.getCreateUser());
    assertThat(regenerated.getUpdateUser()).isEqualTo(toRegenerate.getUpdateUser());
  }

  @Test
  public void regeneratePrimaryEdxActivationCodeOutOfManyForDistrict() {
    String districtCode = UUID.randomUUID().toString();
    EdxActivationCodeEntity existingPrimaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), false, null, districtCode));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), false, null, districtCode));
    EdxPrimaryActivationCode toRegenerate = this.createEdxPrimaryActivationCodeForDistrict(districtCode, "EDX-API-UNIT-TEST-UPDATE-USER", "EDX-API-UNIT-TEST-UPDATE-USER");
    EdxActivationCodeEntity regenerated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, districtCode, toRegenerate);
    assertThat(regenerated.getEdxActivationCodeId()).isNotNull().isEqualTo(existingPrimaryEdxActivationCode.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(regenerated.getMincode()).isNull();
    assertThat(regenerated.getDistrictCode()).isNotNull().isNotEmpty().isEqualTo(toRegenerate.getDistrictCode()).isEqualTo(existingPrimaryEdxActivationCode.getDistrictCode()).isEqualTo(secondaryEdxActivationCode.getDistrictCode());
    assertThat(regenerated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8).isNotEqualTo(existingPrimaryEdxActivationCode.getActivationCode());
    assertThat(regenerated.getIsPrimary()).isTrue();
    assertThat(regenerated.getCreateUser()).isEqualTo(existingPrimaryEdxActivationCode.getCreateUser());
    assertThat(regenerated.getUpdateUser()).isEqualTo(toRegenerate.getUpdateUser());
  }

  private MinistryOwnershipTeamEntity getMinistryOwnershipEntity(String teamName, String groupRoleIdentifier) {
    MinistryOwnershipTeamEntity entity = new MinistryOwnershipTeamEntity();
    entity.setTeamName(teamName);
    entity.setGroupRoleIdentifier(groupRoleIdentifier);
    entity.setCreateUser("test");
    entity.setDescription("Description");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }
}
