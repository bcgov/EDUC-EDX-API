package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolRoleEntity;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.api.edx.struct.v1.EdxPrimaryActivationCode;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class EdxUsersServiceTests extends BaseEdxAPITest {

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
  private RestUtils restUtils;

  @Autowired
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;

  @Autowired
  private EdxActivationCodeRepository edxActivationCodeRepository;

  @AfterEach
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
  void getAllMinistryTeams() {
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("Test Team", "TEST_TEAM"));
    this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("New Team", "NEW_TEAM"));
    final List<MinistryOwnershipTeamEntity> teams = this.service.getMinistryTeamsList();
    assertThat(teams).isNotNull().hasSize(2);
    assertThat(teams.get(0).getDescription()).isEqualTo("Description");
  }

  @Test
  void getAllEdxUserSchools() {
    this.edxUserSchoolRepository.deleteAll();
    var entity = this.edxUserRepository.save(getEdxUserEntity());
    this.edxUserSchoolRepository.save(getEdxUserSchoolEntity(entity));
    final List<EdxUserSchoolEntity> edxUserSchoolEntities = this.service.getEdxUserSchoolsList();
    assertThat(edxUserSchoolEntities).isNotNull().hasSize(1);
  }

  @Test
  void findEdxUserByDigitalID() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    var schoolEntity = getEdxUserSchoolEntity(entity);
    schoolEntity.setSchoolID(UUID.randomUUID());
    edxUserSchoolRepository.save(schoolEntity);

    var edxUserEntities = this.service.findEdxUsers(Optional.of(entity.getDigitalIdentityID()),Optional.empty(), null, null,Optional.empty());
    assertThat(edxUserEntities).isNotNull().hasSize(1);
  }

  @Test
  void findAllEdxUsersByDistrictID() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    var schoolEntity = getEdxUserSchoolEntity(entity);
    schoolEntity.setSchoolID(UUID.randomUUID());
    EdxUserSchoolRoleEntity roleEntity = new EdxUserSchoolRoleEntity();
    roleEntity.setEdxUserSchoolEntity(schoolEntity);
    roleEntity.setCreateUser("ABC");
    roleEntity.setEdxRoleCode("EDX_SCHOOL_ADMIN");
    roleEntity.setCreateDate(LocalDateTime.now());
    schoolEntity.getEdxUserSchoolRoleEntities().add(roleEntity);
    edxUserSchoolRepository.save(schoolEntity);

    List<UUID> schoolIDList1 = new ArrayList<>();
    schoolIDList1.add(schoolEntity.getSchoolID());
    SchoolTombstone school1 = new SchoolTombstone();
    school1.setSchoolCategoryCode("PUBLIC");
    school1.setSchoolId(schoolIDList1.get(0).toString());

    var districtSchoolsMap = new HashMap<String, List<SchoolTombstone>>();
    var districtID = UUID.randomUUID();
    districtSchoolsMap.put(districtID.toString(), Arrays.asList(school1));
    when(this.restUtils.getDistrictSchoolsMap()).thenReturn(districtSchoolsMap);

    var edxSchools = this.service.findAllDistrictEdxUsers(districtID.toString());
    assertThat(edxSchools).isNotNull().hasSize(1);
  }

  @Test
  void findAllEdxUsersByDistrictIDNoIndependents() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    var schoolEntity = getEdxUserSchoolEntity(entity);
    schoolEntity.setSchoolID(UUID.randomUUID());
    EdxUserSchoolRoleEntity roleEntity = new EdxUserSchoolRoleEntity();
    roleEntity.setEdxUserSchoolEntity(schoolEntity);
    roleEntity.setCreateUser("ABC");
    roleEntity.setEdxRoleCode("EDX_SCHOOL_ADMIN");
    roleEntity.setCreateDate(LocalDateTime.now());
    schoolEntity.getEdxUserSchoolRoleEntities().add(roleEntity);
    edxUserSchoolRepository.save(schoolEntity);

    SchoolTombstone school1 = new SchoolTombstone();
    school1.setSchoolCategoryCode("PUBLIC");
    school1.setSchoolId(schoolEntity.getSchoolID().toString());

    SchoolTombstone school2 = new SchoolTombstone();
    school2.setSchoolCategoryCode("INDP_FNS");
    school2.setSchoolId(UUID.randomUUID().toString());

    SchoolTombstone school3 = new SchoolTombstone();
    school3.setSchoolCategoryCode("INDEPEND");
    school3.setSchoolId(UUID.randomUUID().toString());

    var districtSchoolsMap = new HashMap<String, List<SchoolTombstone>>();
    var districtID = UUID.randomUUID();
    districtSchoolsMap.put(districtID.toString(), Arrays.asList(school1, school2, school3));
    when(this.restUtils.getDistrictSchoolsMap()).thenReturn(districtSchoolsMap);

    var edxSchools = this.service.findAllDistrictEdxUsers(districtID.toString());
    assertThat(edxSchools).isNotNull().hasSize(1);
  }

  @Test
  void findAllEdxUsersByDistrictIDNoClosedSchools() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    List<UUID> schoolIDs = new ArrayList<UUID>();
    for (int i = 0; i < 6; i++){
      schoolIDs.add(UUID.randomUUID());
      var schoolEntity = getEdxUserSchoolEntity(entity);
      schoolEntity.setSchoolID(schoolIDs.get(i));
      EdxUserSchoolRoleEntity roleEntity1 = new EdxUserSchoolRoleEntity();
      roleEntity1.setEdxUserSchoolEntity(schoolEntity);
      roleEntity1.setCreateUser("ABC");
      roleEntity1.setEdxRoleCode("EDX_SCHOOL_ADMIN");
      roleEntity1.setCreateDate(LocalDateTime.now());
      schoolEntity.getEdxUserSchoolRoleEntities().add(roleEntity1);
      edxUserSchoolRepository.save(schoolEntity);
    }

    LocalDateTime currentDate = LocalDate.now().atStartOfDay();

    SchoolTombstone school1 = new SchoolTombstone();
    school1.setSchoolCategoryCode("PUBLIC");
    school1.setSchoolId(schoolIDs.get(0).toString());
    school1.setOpenedDate(currentDate.toString());
    school1.setClosedDate(null);

    SchoolTombstone school2 = new SchoolTombstone();
    school2.setSchoolCategoryCode("PUBLIC");
    school2.setSchoolId(schoolIDs.get(1).toString());
    school2.setOpenedDate("1900-01-01T00:00:00");
    school2.setClosedDate("1901-01-01T00:00:00");

    SchoolTombstone school3 = new SchoolTombstone();
    school3.setSchoolCategoryCode("PUBLIC");
    school3.setSchoolId(schoolIDs.get(2).toString());
    school3.setOpenedDate(currentDate.plusDays(1).toString());
    school3.setClosedDate(null);

    SchoolTombstone school4 = new SchoolTombstone();
    school4.setSchoolCategoryCode("PUBLIC");
    school4.setSchoolId(schoolIDs.get(3).toString());
    school4.setOpenedDate(currentDate.minusDays(2).toString());
    school4.setClosedDate(currentDate.minusDays(1).toString());

    SchoolTombstone school5 = new SchoolTombstone();
    school5.setSchoolCategoryCode("PUBLIC");
    school5.setSchoolId(schoolIDs.get(4).toString());
    school5.setOpenedDate(currentDate.minusDays(1).toString());
    school5.setClosedDate(currentDate.toString());

    SchoolTombstone school6 = new SchoolTombstone();
    school6.setSchoolCategoryCode("PUBLIC");
    school6.setSchoolId(schoolIDs.get(5).toString());
    school6.setOpenedDate(currentDate.toString());
    school6.setClosedDate(currentDate.plusDays(2).toString());

    var districtSchoolsMap = new HashMap<String, List<SchoolTombstone>>();
    var districtID = UUID.randomUUID();
    districtSchoolsMap.put(districtID.toString(), Arrays.asList(school1, school2, school3, school4, school5, school6));
    when(this.restUtils.getDistrictSchoolsMap()).thenReturn(districtSchoolsMap);

    var edxSchools = this.service.findAllDistrictEdxUsers(districtID.toString());
    assertThat(edxSchools).isNotNull().hasSize(3);
  }

  @Test
  void findEdxUserBySchoolID() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    var schoolEntity = getEdxUserSchoolEntity(entity);
    schoolEntity.setSchoolID(UUID.randomUUID());
    edxUserSchoolRepository.save(schoolEntity);

    var edxUserEntities = this.service.findEdxUsers(Optional.empty(), Optional.of(schoolEntity.getSchoolID()), null, null,Optional.empty());
    assertThat(edxUserEntities).isNotNull().hasSize(1);
  }

  @Test
  void retrieveEdxUser() {
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
  void getEdxUserSchoolsByPermissionName() {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);

    final List<String> edxUserSchoolEntities = this.service.getEdxUserSchoolsList("SECURE_EXCHANGE");
    assertThat(edxUserSchoolEntities).isNotNull().hasSize(1);
    assertThat(edxUserSchoolEntities.get(0)).isEqualTo(entity.getEdxUserSchoolEntities().iterator().next().getSchoolID().toString());
  }

  @Test
  void findPrimaryEdxActivationCodeForSchool() {
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, UUID.randomUUID(), null));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, primaryToFind.getSchoolID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  void findPrimaryEdxActivationCodeForDistrict() {
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, UUID.randomUUID()));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, primaryToFind.getDistrictID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  void findPrimaryEdxActivationCodeOutOfManyForSchool() {
    UUID schoolID = UUID.randomUUID();
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, schoolID, null));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, schoolID, null));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, primaryToFind.getSchoolID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  void findPrimaryEdxActivationCodeOutOfManyForDistrict() {
    UUID districtID = UUID.randomUUID();
    EdxActivationCodeEntity primaryToFind = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), true, true, UUID.randomUUID(), 0, null, districtID));
    EdxActivationCodeEntity secondaryEdxActivationCode = this.edxActivationCodeRepository.save(this.createEdxActivationCodeEntity(UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, districtID));
    EdxActivationCodeEntity found = this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, primaryToFind.getDistrictID().toString());
    assertThat(found.getEdxActivationCodeId()).isNotNull().isEqualTo(primaryToFind.getEdxActivationCodeId()).isNotEqualTo(secondaryEdxActivationCode.getEdxActivationCodeId());
    assertThat(found.getIsPrimary()).isTrue();
  }

  @Test
  void findPrimaryEdxActivationCodeOnlyReturnsPrimaryEdxActivationCodeForSchool() {
    EdxActivationCodeEntity mockActivationCode = this.createEdxActivationCodeEntity(
      UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, UUID.randomUUID(), null);
    EdxActivationCodeEntity secondaryActivationCode = this.edxActivationCodeRepository.save(mockActivationCode);

    String schoolId = secondaryActivationCode.getSchoolID().toString();
    assertThrows(EntityNotFoundException.class, () -> {
      this.service
        .findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, schoolId);
    });
  }

  @Test
  void findPrimaryEdxActivationCodeOnlyReturnsPrimaryEdxActivationCodeForDistrict() {
    EdxActivationCodeEntity mockActivationCode = this.createEdxActivationCodeEntity(
      UUID.randomUUID().toString(), false, true, UUID.randomUUID(), 0, null, UUID.randomUUID());
    EdxActivationCodeEntity secondaryActivationCode = this.edxActivationCodeRepository.save(mockActivationCode);

    String districtId = secondaryActivationCode.getDistrictID().toString();
    assertThrows(EntityNotFoundException.class, () -> {
      this.service
        .findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, districtId);
    });
  }

  @Test
  void findPrimaryEdxActivationCodeCannotFindNonExistingEdxActivationCodeForSchool() {
    String uuid = UUID.randomUUID().toString();
    assertThrows(EntityNotFoundException.class, () -> {
      this.service.findPrimaryEdxActivationCode(InstituteTypeCode.SCHOOL, uuid);
    });
  }

  @Test
  void findPrimaryEdxActivationCodeCannotFindNonExistingEdxActivationCodeForDistrict() {
    String uuid = UUID.randomUUID().toString();
    assertThrows(EntityNotFoundException.class, () -> {
      this.service.findPrimaryEdxActivationCode(InstituteTypeCode.DISTRICT, uuid);
    });
  }

  @Test
  void generatePrimaryEdxActivationCodeForSchool() {
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
  void generatePrimaryEdxActivationCodeForDistrict() {
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
  void generateEdxActivationCode() throws NoSuchAlgorithmException {
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
  void regeneratePrimaryEdxActivationCodeForSchool() {
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
  void regeneratePrimaryEdxActivationCodeForDistrict() {
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
  void regeneratePrimaryEdxActivationCodeOutOfManyForSchool() {
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
  void regeneratePrimaryEdxActivationCodeOutOfManyForDistrict() {
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

  @Test
  void testUpdateUserRolesForClosedSchools_givenSchoolWithTranscriptEligibleSetToFalse() {
    var school = createMockSchoolTombstone();
    school.setClosedDate(String.valueOf(LocalDateTime.now().minusDays(1)));

    var gradSchool = createMockGradSchool();
    gradSchool.setSchoolID(school.getSchoolId());
    gradSchool.setCanIssueTranscripts("Y");
    when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));

    when(this.restUtils.getSchools()).thenReturn(List.of(school));

    var userEntity = edxUserRepository.save(getEdxUserEntity());
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    roleEntity.setEdxRoleCode("EDX_SCHOOL_ADMIN");
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    edxRoleRepository.save(roleEntity);

    var userSchoolEntity = getEdxUserSchoolEntity(userEntity, UUID.fromString(school.getSchoolId()));
    var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
    userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
    userSchoolEntity.setExpiryDate(null);
    edxUserSchoolRepository.save(userSchoolEntity);

    service.updateUserRolesForClosedSchools();

    var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
    assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
    assertThat(userSchoolEntityAfterUpdate).hasSize(1);
    assertThat(userSchoolEntityAfterUpdate.get(0).getEdxUserSchoolRoleEntities()).isEmpty();
  }

  @Test
  void testUpdateUserRolesForClosedSchools_givenSchoolWithTranscriptEligibleSetToTrueAndIsPastClosedDateBy3Months() {
    var school = createMockSchoolTombstone();
    school.setClosedDate(String.valueOf(LocalDateTime.now().minusMonths(3).minusDays(1)));

    var gradSchool = createMockGradSchool();
    gradSchool.setSchoolID(school.getSchoolId());
    gradSchool.setCanIssueTranscripts("Y");
    when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));

    when(this.restUtils.getSchools()).thenReturn(List.of(school));

    var userEntity = edxUserRepository.save(getEdxUserEntity());
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    roleEntity.setEdxRoleCode("EDX_SCHOOL_ADMIN");
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    edxRoleRepository.save(roleEntity);

    var userSchoolEntity = getEdxUserSchoolEntity(userEntity, UUID.fromString(school.getSchoolId()));
    var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
    userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
    userSchoolEntity.setExpiryDate(null);
    edxUserSchoolRepository.save(userSchoolEntity);

    service.updateUserRolesForClosedSchools();

    var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
    assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
    assertThat(userSchoolEntityAfterUpdate).hasSize(1);
    assertThat(userSchoolEntityAfterUpdate.get(0).getEdxUserSchoolRoleEntities()).isEmpty();
  }

  @Test
  void testUpdateUserRolesForClosedSchools_givenSchoolWithTranscriptEligibleSetToYes_And_SchoolIsOpen() {
    var school = createMockSchoolTombstone();
    school.setClosedDate(null);

    var gradSchool = createMockGradSchool();
    gradSchool.setSchoolID(school.getSchoolId());
    gradSchool.setCanIssueTranscripts("Y");
    when(this.restUtils.getGradSchoolBySchoolID(any())).thenReturn(Optional.of(gradSchool));

    when(this.restUtils.getSchools()).thenReturn(List.of(school));

    var userEntity = edxUserRepository.save(getEdxUserEntity());
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    roleEntity.setEdxRoleCode("EDX_SCHOOL_ADMIN");
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    edxRoleRepository.save(roleEntity);

    var userSchoolEntity = getEdxUserSchoolEntity(userEntity, UUID.fromString(school.getSchoolId()));
    var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
    userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
    userSchoolEntity.setExpiryDate(null);
    edxUserSchoolRepository.save(userSchoolEntity);

    service.updateUserRolesForClosedSchools();

    var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
    assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
    assertThat(userSchoolEntityAfterUpdate).hasSize(1);
    assertThat(userSchoolEntityAfterUpdate.get(0).getEdxUserSchoolRoleEntities()).hasSize(1);
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
