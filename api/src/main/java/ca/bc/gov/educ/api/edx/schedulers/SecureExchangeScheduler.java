package ca.bc.gov.educ.api.edx.schedulers;

import ca.bc.gov.educ.api.edx.constants.SecureExchangeStatusCode;
import ca.bc.gov.educ.api.edx.repository.*;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.javacrumbs.shedlock.core.LockAssert;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@Component
@Slf4j
public class SecureExchangeScheduler {
  private final DocumentRepository documentRepository;

  private final SecureExchangeRequestRepository secureExchangeRepository;

  private final SecureExchangeStudentRepository exchangeStudentRepository;

  private final SecureExchangeRequestCommentRepository exchangeCommentRepository;

  private final SecureExchangeRequestNoteRepository exchangeNoteRepository;


  @Value("${purge.closed.message.after.days}")
  @Setter
  @Getter
  Integer numberOfDaysBeforeClosedMessagePurged;

  public SecureExchangeScheduler(final DocumentRepository documentRepository, SecureExchangeRequestRepository secureExchangeRepository, SecureExchangeStudentRepository exchangeStudentRepository, SecureExchangeRequestCommentRepository exchangeCommentRepository, SecureExchangeRequestNoteRepository exchangeNoteRepository) {
    this.documentRepository = documentRepository;
    this.secureExchangeRepository = secureExchangeRepository;
    this.exchangeStudentRepository = exchangeStudentRepository;
    this.exchangeCommentRepository = exchangeCommentRepository;
    this.exchangeNoteRepository = exchangeNoteRepository;
  }

  /**
   * run the job based on configured scheduler(a cron expression) and purge old records from DB.
   */
  @Scheduled(cron = "${scheduled.jobs.remove.blob.contents.document.cron}")
  @SchedulerLock(name = "RemoveBlobContentsFromUploadedDocuments",
    lockAtLeastFor = "PT4H", lockAtMostFor = "PT4H") //midnight job so lock for 4 hours
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void removeBlobContentsFromUploadedDocuments() {
    log.info("Starting blob contents purge");
    val dateTimeToCompare = LocalDateTime.now().minusHours(24);
    log.info("Date to compare is: " + dateTimeToCompare);
    LockAssert.assertLocked();
    val records = this.documentRepository.findAllBySecureExchangeEntitySecureExchangeStatusCodeInAndFileSizeGreaterThanAndDocumentDataIsNotNull(Arrays.asList(SecureExchangeStatusCode.CLOSED.toString()), 0);
    log.info("Number of records to possibly purge: " + records.size());
    if (!records.isEmpty()) {
      for (val document : records) {
        log.info("Checking secure exchange document ID: " + document.getDocumentID());
        log.info("Status date is: " + document.getSecureExchangeEntity().getStatusUpdateDate());
        if(document.getSecureExchangeEntity().getStatusUpdateDate().isBefore(dateTimeToCompare)){
          log.info("Setting values to null for document ID: " + document.getDocumentID());
          document.setDocumentData(null); // empty the document data.
          document.setFileSize(0);
        }
      }
      this.documentRepository.saveAll(records);
    }

  }


  @Scheduled(cron = "${scheduled.jobs.purge.closed.messages.cron}")
  @SchedulerLock(name = "PurgeClosedMessages",
          lockAtLeastFor = "PT4H", lockAtMostFor = "PT4H") //midnight job so lock for 4 hours
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void purgeClosedMessages() {
    LockAssert.assertLocked();
    final LocalDateTime createDate = this.calculateCreateDateBasedOnMessageAge();
    exchangeNoteRepository.deleteByCreateDateBefore(createDate);
    exchangeCommentRepository.deleteByCreateDateBefore(createDate);
    exchangeStudentRepository.deleteByCreateDateBefore(createDate);
    documentRepository.deleteByCreateDateBefore(createDate);
    secureExchangeRepository.deleteByCreateDateBefore(createDate);
    log.info("Purged closed messages scheduler");
  }

  private LocalDateTime calculateCreateDateBasedOnMessageAge() {
    return LocalDateTime.now().minusDays(this.getNumberOfDaysBeforeClosedMessagePurged());
  }
}
