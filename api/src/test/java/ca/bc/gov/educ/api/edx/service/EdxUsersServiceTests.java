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
    schoolEntity.setSchoolID(UUID.randomUUID());
    edxUserSchoolRepository.save(schoolEntity);

    var edxUserEntities = this.service.findEdxUsers(Optional.of(entity.getDigitalIdentityID()),Optional.empty(), null, null,Optional.empty());
    assertThat(edxUserEntities).isNotNull().hasSize(1);
  }

  @Test
  public void findEdxUserBySchoolID() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    var schoolEntity = getEdxUserSchoolEntity(entity);
    schoolEntity.setSchoolID(UUID.randomUUID());
    edxUserSchoolRepository.save(schoolEntity);

    var edxUserEntities = this.service.findEdxUsers(Optional.empty(), Optional.of(schoolEntity.getSchoolID()), null, null,Optional.empty());
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
    assertThat(edxUserSchoolRollEntity.getEdxRoleCode()).isEqualTo("EDX_SCHOOL_ADMIN");
  }

  @Test
  public void getEdxUserSchoolsByPermissionName() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);

    final List<String> edxUserSchoolEntities = this.service.getEdxUserSchoolsList("Exchange");
    assertThat(edxUserSchoolEntities).isNotNull().hasSize(1);
    assertThat(edxUserSchoolEntities.get(0)).isEqualTo(entity.getEdxUserSchoolEntities().iterator().next().getSchoolID().toString());
  }

  @Test
  public void findPrimaryEdxActivationCodeForSchool() {
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, UUID.randomUUID(), null));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, primaryToFind.getSchoolID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  public void findPrimaryEdxActivationCodeForDistrict() {
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, UUID.randomUUID()));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, primaryToFind.getDistrictID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  public void findPrimaryEdxActivationCodeOutOfManyForSchool() {
    UUID schoolID = UUID.randomUUID();
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, schoolID, null));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, schoolID, null));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, primaryToFind.getSchoolID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  public void findPrimaryEdxActivationCodeOutOfManyForDistrict() {
    UUID districtID = UUID.randomUUID();
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, districtID));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, districtID));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, primaryToFind.getDistrictID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test(expected = EntityNotFoundException.class)
  public void findPrimaryEdxActivationCodeOnlyReturnsPrimaryEdxActivationCodeForSchool() {
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, UUID.randomUUID(), null));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, secondaryEdxActivationCode.getSchoolID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
  }

  @Test(expected = EntityNotFoundException.class)
  public void findPrimaryEdxActivationCodeOnlyReturnsPrimaryEdxActivationCodeForDistrict() {
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, UUID.randomUUID()));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, secondaryEdxActivationCode.getDistrictID().toString());
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
    EdxPrimaryActivationCode toGenerate = this.createEdxPrimaryActivationCodeForSchool(UUID.randomUUID(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    EdxActivationCodeEntity generated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, toGenerate.getSchoolID().toString(), toGenerate);
    assertThat(generated.getSchoolID()).isNotNull().isEqualTo(toGenerate.getSchoolID());
    assertThat(generated.getDistrictID()).isNull();
    assertThat(generated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8);
    assertThat(generated.getIsPrimary()).isTrue();
    assertThat(generated.getCreateUser()).isEqualTo(toGenerate.getCreateUser());
    assertThat(generated.getUpdateUser()).isEqualTo(toGenerate.getUpdateUser());
  }

  @Test
  public void generatePrimaryEdxActivationCodeForDistrict() {
    EdxPrimaryActivationCode toGenerate = this.createEdxPrimaryActivationCodeForDistrict(UUID.randomUUID(), "EDX-API-UNIT-TEST", "EDX-API-UNIT-TEST");
    EdxActivationCodeEntity generated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, toGenerate.getDistrictID().toString(), toGenerate);
    assertThat(generated.getSchoolID()).isNull();
    assertThat(generated.getDistrictID()).isNotNull().isEqualTo(toGenerate.getDistrictID());
    assertThat(generated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8);
    assertThat(generated.getIsPrimary()).isTrue();
    assertThat(generated.getCreateUser()).isEqualTo(toGenerate.getCreateUser());
    assertThat(generated.getUpdateUser()).isEqualTo(toGenerate.getUpdateUser());
  }

  @Test
  public void generateEdxActivationCode() throws NoSuchAlgorithmException {
    var edxActivationCodeEntity = this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, UUID.randomUUID(), null);
    UUID user = UUID.randomUUID();
    edxActivationCodeEntity.setEdxUserId(user);
    EdxActivationCodeEntity activationCode = this.edxActivationCodeRepository.save(edxActivationCodeEntity);
    EdxActivationCodeEntity generated = this.service.createPersonalEdxActivationCode(activationCode);
    assertThat(generated.getSchoolID()).isNotNull().isEqualTo(edxActivationCodeEntity.getSchoolID());
    assertThat(generated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8);
    assertThat(generated.getIsPrimary()).isFalse();
    assertThat(generated.getEdxUserId()).isEqualTo(user);
    assertThat(generated.getCreateUser()).isEqualTo(edxActivationCodeEntity.getCreateUser());
    assertThat(generated.getUpdateUser()).isEqualTo(edxActivationCodeEntity.getUpdateUser());
  }

  @Test
  public void regeneratePrimaryEdxActivationCodeForSchool() {
    UUID schoolID = UUID.randomUUID();
    EdxActivationCodeEntity existing = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, schoolID, null));
    EdxPrimaryActivationCode toRegenerate = this.createEdxPrimaryActivationCodeForSchool(schoolID, "EDX-API-UNIT-TEST-UPDATE-USER", "EDX-API-UNIT-TEST-UPDATE-USER");
    EdxActivationCodeEntity regenerated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, schoolID.toString(), toRegenerate);
    assertThat(regenerated.getEdxActivationCodeId()).isNotNull().isEqualTo(existing.getEdxActivationCodeId());
    assertThat(regenerated.getSchoolID()).isNotNull().isEqualTo(toRegenerate.getSchoolID()).isEqualTo(existing.getSchoolID());
    assertThat(regenerated.getDistrictID()).isNull();
    assertThat(regenerated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8).isNotEqualTo(existing.getActivationCode());
    assertThat(regenerated.getIsPrimary()).isTrue();
    assertThat(regenerated.getCreateUser()).isEqualTo(existing.getCreateUser());
    assertThat(regenerated.getUpdateUser()).isEqualTo(toRegenerate.getUpdateUser());
  }

  @Test
  public void regeneratePrimaryEdxActivationCodeForDistrict() {
    UUID districtID = UUID.randomUUID();
    EdxActivationCodeEntity existing = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, districtID));
    EdxPrimaryActivationCode toRegenerate = this.createEdxPrimaryActivationCodeForDistrict(districtID, "EDX-API-UNIT-TEST-UPDATE-USER", "EDX-API-UNIT-TEST-UPDATE-USER");
    EdxActivationCodeEntity regenerated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, districtID.toString(), toRegenerate);
    assertThat(regenerated.getEdxActivationCodeId()).isNotNull().isEqualTo(existing.getEdxActivationCodeId());
    assertThat(regenerated.getSchoolID()).isNull();
    assertThat(regenerated.getDistrictID()).isNotNull().isEqualTo(toRegenerate.getDistrictID()).isEqualTo(existing.getDistrictID());
    assertThat(regenerated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8).isNotEqualTo(existing.getActivationCode());
    assertThat(regenerated.getIsPrimary()).isTrue();
    assertThat(regenerated.getCreateUser()).isEqualTo(existing.getCreateUser());
    assertThat(regenerated.getUpdateUser()).isEqualTo(toRegenerate.getUpdateUser());
  }

  @Test
  public void regeneratePrimaryEdxActivationCodeOutOfManyForSchool() {
    UUID schoolID = UUID.randomUUID();
    EdxActivationCodeEntity existingPrimaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, schoolID, null));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, schoolID, null));
    EdxPrimaryActivationCode toRegenerate = this.createEdxPrimaryActivationCodeForSchool(schoolID, "EDX-API-UNIT-TEST-UPDATE-USER", "EDX-API-UNIT-TEST-UPDATE-USER");
    EdxActivationCodeEntity regenerated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, schoolID.toString(), toRegenerate);
    assertThat(regenerated.getEdxActivationCodeId()).isNotNull().isEqualTo(existingPrimaryEdxActivationCode.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(regenerated.getSchoolID()).isNotNull().isEqualTo(toRegenerate.getSchoolID()).isEqualTo(existingPrimaryEdxActivationCode.getSchoolID()).isEqualTo(secondaryEdxActivationCode.getSchoolID());
    assertThat(regenerated.getDistrictID()).isNull();
    assertThat(regenerated.getActivationCode()).isNotNull().isNotEmpty().hasSize(8).isNotEqualTo(existingPrimaryEdxActivationCode.getActivationCode());
    assertThat(regenerated.getIsPrimary()).isTrue();
    assertThat(regenerated.getCreateUser()).isEqualTo(existingPrimaryEdxActivationCode.getCreateUser());
    assertThat(regenerated.getUpdateUser()).isEqualTo(toRegenerate.getUpdateUser());
  }

  @Test
  public void regeneratePrimaryEdxActivationCodeOutOfManyForDistrict() {
    UUID districtID = UUID.randomUUID();
    EdxActivationCodeEntity existingPrimaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, districtID));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, districtID));
    EdxPrimaryActivationCode toRegenerate = this.createEdxPrimaryActivationCodeForDistrict(districtID, "EDX-API-UNIT-TEST-UPDATE-USER", "EDX-API-UNIT-TEST-UPDATE-USER");
    EdxActivationCodeEntity regenerated = this.service.generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, districtID.toString(), toRegenerate);
    assertThat(regenerated.getEdxActivationCodeId()).isNotNull().isEqualTo(existingPrimaryEdxActivationCode.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(regenerated.getSchoolID()).isNull();
    assertThat(regenerated.getDistrictID()).isNotNull().isEqualTo(toRegenerate.getDistrictID()).isEqualTo(existingPrimaryEdxActivationCode.getDistrictID()).isEqualTo(secondaryEdxActivationCode.getDistrictID());
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
