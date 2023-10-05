package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class EdxUserScheduler {
  private final EdxUserSchoolRepository edxUserSchoolRepository;
  private final EdxUserDistrictRepository edxUserDistrictRepository;

  public EdxUserScheduler(
    EdxUserSchoolRepository edxUserSchoolRepository,
    EdxUserDistrictRepository edxUserDistrictRepository
  ) {
    this.edxUserSchoolRepository = edxUserSchoolRepository;
    this.edxUserDistrictRepository = edxUserDistrictRepository;
  }

  @Scheduled(cron = "${scheduled.jobs.purge.edx.users.cron}")
  @SchedulerLock(name = "PurgeEdxUsers", lockAtLeastFor = "PT4H", lockAtMostFor = "PT4H")
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void purgeExpiredEdxUsers() {
    LockAssert.assertLocked();
    final LocalDateTime now = LocalDateTime.now();
    List<EdxUserSchoolEntity> expiredSchoolUsers = this.edxUserSchoolRepository.findAllByExpiryDateBefore(now);
    List<EdxUserDistrictEntity> expiredDistrictUsers = this.edxUserDistrictRepository.findAllByExpiryDateBefore(now);

    this.edxUserSchoolRepository.deleteAll(expiredSchoolUsers);
    this.edxUserDistrictRepository.deleteAll(expiredDistrictUsers);
  }
}
