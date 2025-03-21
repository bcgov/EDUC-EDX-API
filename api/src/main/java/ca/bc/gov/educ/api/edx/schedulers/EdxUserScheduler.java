package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictLightEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolLightEntity;
import ca.bc.gov.educ.api.edx.repository.EdxUserDistrictLightRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolLightRepository;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
public class EdxUserScheduler {
    private final EdxUserSchoolLightRepository edxUserSchoolLightRepository;
    private final EdxUserDistrictLightRepository edxUserDistrictLightRepository;

    public EdxUserScheduler(EdxUserSchoolLightRepository edxUserSchoolLightRepository, EdxUserDistrictLightRepository edxUserDistrictLightRepository) {
        this.edxUserSchoolLightRepository = edxUserSchoolLightRepository;
        this.edxUserDistrictLightRepository = edxUserDistrictLightRepository;
    }

    @Scheduled(cron = "${scheduled.jobs.purge.edx.users.cron}")
    @SchedulerLock(name = "PurgeEdxUsers", lockAtLeastFor = "PT4H", lockAtMostFor = "PT4H")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void purgeExpiredEdxUsers() {
        LockAssert.assertLocked();

        final LocalDateTime now = LocalDateTime.now();
        List<EdxUserSchoolLightEntity> expiredSchoolUsers = this.edxUserSchoolLightRepository.findAllByExpiryDateBefore(now);
        List<EdxUserDistrictLightEntity> expiredDistrictUsers = this.edxUserDistrictLightRepository.findAllByExpiryDateBefore(now);

        log.info("Purging expired school users");
        expiredSchoolUsers.forEach(edxUserSchoolEntity -> log.info("Removing EDX user " + edxUserSchoolEntity.getEdxUserID() + " from school ID: " + edxUserSchoolEntity.getSchoolID()));
        this.edxUserSchoolLightRepository.deleteAll(expiredSchoolUsers);
        log.info("Purging expired district users");
        expiredDistrictUsers.forEach(edxUserDistrictEntity -> log.info("Removing EDX user " + edxUserDistrictEntity.getEdxUserID() + " from district ID: " + edxUserDistrictEntity.getDistrictID()));
        this.edxUserDistrictLightRepository.deleteAll(expiredDistrictUsers);
    }
}
