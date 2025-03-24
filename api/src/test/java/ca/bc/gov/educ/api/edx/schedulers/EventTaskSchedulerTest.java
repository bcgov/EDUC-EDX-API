package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.repository.EdxPermissionRepository;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import lombok.SneakyThrows;
import lombok.val;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class EventTaskSchedulerTest extends BaseEdxAPITest {
  @Autowired
  EventTaskScheduler eventTaskScheduler;
  @Autowired
  private EdxRoleRepository edxRoleRepository;
  @Autowired
  private RestUtils restUtils;
  @Autowired
  private EdxPermissionRepository edxPermissionRepository;
  @Autowired
  private EdxUserRepository edxUserRepository;
  @Autowired
  private EdxUserSchoolRepository edxUserSchoolRepository;

  @BeforeEach
  public void setUp() throws Exception {
    LockAssert.TestHelper.makeAllAssertsPass(true);
  }

  @AfterEach
  void cleanup(){
    edxUserSchoolRepository.deleteAll();
    edxUserRepository.deleteAll();
    edxPermissionRepository.deleteAll();
    edxRoleRepository.deleteAll();
  }

  @Test
  void testFindAndProcessPendingSagaEvents_givenInProgressSagas_shouldBeRetried() {
    val saga = this.secureExchangeAPITestUtils.getSagaRepository().save(this.creatMockSaga());
    this.eventTaskScheduler.findAndProcessUncompletedSagas();
    List<SagaEventStatesEntity> sagaEvents = this.secureExchangeAPITestUtils.getSagaEventStateRepository().findBySaga(saga);
    assertThat(sagaEvents).isNotEmpty().hasSize(1);
    assertThat(this.eventTaskScheduler.getStatusFilters()).isNotEmpty().hasSize(2);
    val updatedSaga= this.secureExchangeAPITestUtils.getSagaRepository().findById(saga.getSagaId());
    assertThat(updatedSaga).isNotEmpty();
    this.eventTaskScheduler.setRetryCountAndLog(updatedSaga.get());
    assertThat(updatedSaga.get().getRetryCount()).isPositive();
  }

  @Test
  void testUpdateUserRolesForClosedSchools_givenSchoolWithTranscriptEligibleSetToYes_And_SchoolIsClosed() {
    var school = createMockSchoolTombstone();
    school.setCanIssueTranscripts(true);
    school.setClosedDate(String.valueOf(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).minusDays(1)));

    when(this.restUtils.getSchools()).thenReturn(List.of(school));

    var userEntity = edxUserRepository.save(getEdxUserEntity());
    var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
    var roleEntity = getEdxRoleEntity();
    roleEntity.setEdxRoleCode("GRAD_SCH_ADMIN");
    var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
    roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
    edxRoleRepository.save(roleEntity);

    var userSchoolEntity = getEdxUserSchoolEntity(userEntity, UUID.fromString(school.getSchoolId()));
    var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
    userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
    userSchoolEntity.setExpiryDate(null);
    edxUserSchoolRepository.save(userSchoolEntity);

    eventTaskScheduler.updateUserRolesForClosedSchools();

    var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
    assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
    assertThat(userSchoolEntityAfterUpdate.size()).isEqualTo(1);
    assertThat(userSchoolEntityAfterUpdate.get(0).getExpiryDate()).isNull();
    assertThat(userSchoolEntityAfterUpdate.get(0).getEdxUserSchoolRoleEntities().size()).isEqualTo(1);
    var updateRoles =  userSchoolEntityAfterUpdate.get(0).getEdxUserSchoolRoleEntities();
    var anyMatchRole1 = updateRoles.stream().anyMatch(role -> role.getEdxRoleCode().equalsIgnoreCase("GRAD_SCH_ADMIN"));
    assertThat(anyMatchRole1).isTrue();
    var anyMatchRole2 = updateRoles.stream().anyMatch(role -> role.getEdxRoleCode().equalsIgnoreCase("SECURE_EXCHANGE_SCHOOL"));
    assertThat(anyMatchRole2).isFalse();
    var removedRole = updateRoles.stream().anyMatch(role -> role.getEdxRoleCode().equalsIgnoreCase("EDX_SCHOOL_ADMIN"));
    assertThat(removedRole).isFalse();
  }

  @SneakyThrows
  protected SagaEntity creatMockSaga() {
    EdxUserSchoolActivationInviteSagaData sagaData = new EdxUserSchoolActivationInviteSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName("test");
    sagaData.setLastName("test");
    sagaData.setEmail("test@gmail.com");
    sagaData.setSchoolName("Test School");
    sagaData.setSchoolID(UUID.randomUUID());
    List<String> rolesList = new ArrayList<>();
    rolesList.add(edxRoleEntity.getEdxRoleCode());
    sagaData.setEdxActivationRoleCodes(rolesList);
    return SagaEntity.builder()
      .sagaId(UUID.randomUUID())
      .updateDate(LocalDateTime.now().minusMinutes(15))
      .createUser("test")
      .updateUser("test")
      .createDate(LocalDateTime.now().minusMinutes(15))
      .sagaName(SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA.toString())
      .status(IN_PROGRESS.toString())
      .sagaState(INITIATED.toString())
      .payload(JsonUtil.getJsonStringFromObject(sagaData))
      .build();
  }
}
