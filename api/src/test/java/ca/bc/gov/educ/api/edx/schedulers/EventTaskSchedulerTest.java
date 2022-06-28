package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.repository.EdxPermissionRepository;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import lombok.SneakyThrows;
import lombok.val;
import net.javacrumbs.shedlock.core.LockAssert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.IN_PROGRESS;
import static org.assertj.core.api.Assertions.assertThat;

public class EventTaskSchedulerTest extends BaseSecureExchangeAPITest {
  @Autowired
  EventTaskScheduler eventTaskScheduler;
  @Autowired
  private EdxRoleRepository edxRoleRepository;

  @Autowired
  private EdxPermissionRepository edxPermissionRepository;

  @Before
  public void setUp() throws Exception {
    LockAssert.TestHelper.makeAllAssertsPass(true);
  }

  @Test
  public void testFindAndProcessPendingSagaEvents_givenInProgressSagas_shouldBeRetried() {
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

  @SneakyThrows
  protected SagaEntity creatMockSaga() {
    EdxUserActivationInviteSagaData sagaData = new EdxUserActivationInviteSagaData();
    val edxRoleEntity = this.createRoleAndPermissionData(this.edxPermissionRepository, this.edxRoleRepository);
    sagaData.setFirstName("test");
    sagaData.setLastName("test");
    sagaData.setEmail("test@gmail.com");
    sagaData.setSchoolName("Test School");
    sagaData.setMincode("00899178");
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
