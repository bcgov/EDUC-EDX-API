package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.edx.helpers.LogHelper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.edx.repository.SagaRepository;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

/**
 * The type Event task scheduler.
 */
@Slf4j
@Component
public class EventTaskScheduler {
  /**
   * The Saga orchestrators.
   */
  @Getter(PRIVATE)
  private final Map<String, Orchestrator> sagaOrchestrators = new HashMap<>();
  /**
   * The Saga repository.
   */
  @Getter(PRIVATE)
  private final SagaRepository sagaRepository;

  private final EdxUsersService edxUsersService;
  /**
   * The Status filters.
   */
  @Setter
  private List<String> statusFilters;

  /**
   * Instantiates a new Event task scheduler.
   *
   * @param sagaRepository the saga repository
   * @param orchestrators  the orchestrators
   */
  public EventTaskScheduler(final SagaRepository sagaRepository, final List<Orchestrator> orchestrators, EdxUsersService edxUsersService) {
    this.sagaRepository = sagaRepository;
    this.edxUsersService = edxUsersService;
    orchestrators.forEach(orchestrator -> this.sagaOrchestrators.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", this.sagaOrchestrators.keySet()));
  }

  @Scheduled(cron = "${scheduled.jobs.extract.uncompleted.sagas.cron}") // 1 * * * * *
  @SchedulerLock(name = "EXTRACT_UNCOMPLETED_SAGAS",
    lockAtLeastFor = "${scheduled.jobs.extract.uncompleted.sagas.cron.lockAtLeastFor}", lockAtMostFor = "${scheduled.jobs.extract.uncompleted.sagas.cron.lockAtMostFor}")
  public void findAndProcessUncompletedSagas() {
    final List<SagaEntity> sagas = this.getSagaRepository().findTop100ByStatusInOrderByCreateDate(this.getStatusFilters());
    if (!sagas.isEmpty()) {
      this.processUncompletedSagas(sagas);
    }
  }

  @Scheduled(cron = "${scheduled.jobs.update.user.role.for.closed.school.cron}")
  @SchedulerLock(name = "UPDATE_USER_ROLE_FOR_CLOSED_SCHOOLS", lockAtLeastFor = "${scheduled.jobs.update.user.role.for.closed.school.cron.lockAtLeastFor}", lockAtMostFor = "${scheduled.jobs.update.user.role.for.closed.school.cron.lockAtMostFor}")
  public void updateUserRolesForClosedSchools() {
    LockAssert.assertLocked();
    log.info("Starting updateUserRolesForClosedSchools");
    edxUsersService.updateUserRolesForClosedSchools();
  }

  /**
   * Process uncompleted sagas.
   *
   * @param sagas the sagas
   */
  private void processUncompletedSagas(final List<SagaEntity> sagas) {
    for (val saga : sagas) {
      if (saga.getCreateDate().isBefore(LocalDateTime.now().minusMinutes(1))
          && this.getSagaOrchestrators().containsKey(saga.getSagaName())) {
        try {
          this.setRetryCountAndLog(saga);
          this.getSagaOrchestrators().get(saga.getSagaName()).replaySaga(saga);
        } catch (final InterruptedException ex) {
          Thread.currentThread().interrupt();
          log.error("InterruptedException while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, ex);
        } catch (final Exception e) {
          log.error("Exception while findAndProcessPendingSagaEvents :: for saga :: {} :: {}", saga, e);
        }
      }
    }
  }

  /**
   * Gets status filters.
   *
   * @return the status filters
   */
  protected List<String> getStatusFilters() {
    if (this.statusFilters != null && !this.statusFilters.isEmpty()) {
      return this.statusFilters;
    } else {
      final var statuses = new ArrayList<String>();
      statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
      statuses.add(SagaStatusEnum.STARTED.toString());
      return statuses;
    }
  }

  protected void setRetryCountAndLog(final SagaEntity saga) {
    Integer retryCount = saga.getRetryCount();
    if (retryCount == null || retryCount == 0) {
      retryCount = 1;
    } else {
      retryCount += 1;
    }
    saga.setRetryCount(retryCount);
    this.getSagaRepository().save(saga);
    LogHelper.logSagaRetry(saga);
  }
}
