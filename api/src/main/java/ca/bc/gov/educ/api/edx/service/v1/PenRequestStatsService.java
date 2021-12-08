package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.PenRequestStatusCode;
import ca.bc.gov.educ.api.edx.constants.StatsType;
import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestStats;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class PenRequestStatsService {
  private final PenRequestRepository penRequestRepository;

  public PenRequestStatsService(PenRequestRepository penRequestRepository) {
    this.penRequestRepository = penRequestRepository;
  }

  public PenRequestStats getStats(final StatsType statsType) {
    Pair<Long, Double> currentMonthResultAndPercentile;
    val currentDateTime = LocalDateTime.now();
    val baseDateTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
    switch (statsType) {
      case COMPLETIONS_LAST_WEEK:
        return this.getPenRequestsCompletedLastWeek();
      case AVERAGE_COMPLETION_TIME:
        return this.getAverageGMPCompletionTime();
      case COMPLETIONS_LAST_12_MONTH:
        return this.getPenRequestsCompletedLastTwelveMonths();
      case PERCENT_GMP_REJECTED_TO_LAST_MONTH:
        currentMonthResultAndPercentile = this.getMonthlyPercentGMPBasedOnStatus(PenRequestStatusCode.REJECTED.toString());
        return PenRequestStats.builder().gmpRejectedInCurrentMonth(currentMonthResultAndPercentile.getLeft()).percentRejectedGmpToLastMonth(currentMonthResultAndPercentile.getRight()).build();
      case PERCENT_GMP_ABANDONED_TO_LAST_MONTH:
        currentMonthResultAndPercentile = this.getMonthlyPercentGMPBasedOnStatus(PenRequestStatusCode.ABANDONED.toString());
        return PenRequestStats.builder().gmpAbandonedInCurrentMonth(currentMonthResultAndPercentile.getLeft()).percentAbandonedGmpToLastMonth(currentMonthResultAndPercentile.getRight()).build();
      case PERCENT_GMP_COMPLETED_WITH_DOCUMENTS_TO_LAST_MONTH:
        currentMonthResultAndPercentile = this.getMonthlyPercentGMPWithDocsBasedOnStatus(PenRequestStatusCode.MANUAL.toString(), PenRequestStatusCode.AUTO.toString());
        return PenRequestStats.builder().gmpCompletedWithDocsInCurrentMonth(currentMonthResultAndPercentile.getLeft()).percentGmpCompletedWithDocumentsToLastMonth(currentMonthResultAndPercentile.getRight()).build();
      case PERCENT_GMP_COMPLETION_TO_LAST_MONTH:
        currentMonthResultAndPercentile = this.getMonthlyPercentGMPBasedOnStatus(PenRequestStatusCode.MANUAL.toString(), PenRequestStatusCode.AUTO.toString());
        return PenRequestStats.builder().gmpCompletedInCurrentMonth(currentMonthResultAndPercentile.getLeft()).percentCompletedGmpToLastMonth(currentMonthResultAndPercentile.getRight()).build();
      case ALL_STATUSES_LAST_12_MONTH:
        return PenRequestStats.builder().allStatsLastTwelveMonth(this.getAllStatusesBetweenDates(baseDateTime.withDayOfMonth(1).minusMonths(11), currentDateTime)).build();
      case ALL_STATUSES_LAST_6_MONTH:
        return PenRequestStats.builder().allStatsLastSixMonth(this.getAllStatusesBetweenDates(baseDateTime.withDayOfMonth(1).minusMonths(5), currentDateTime)).build();
      case ALL_STATUSES_LAST_1_MONTH:
        return PenRequestStats.builder().allStatsLastOneMonth(this.getAllStatusesBetweenDates(baseDateTime.minusDays(1).minusMonths(1), currentDateTime)).build();
      case ALL_STATUSES_LAST_1_WEEK:
        return PenRequestStats.builder().allStatsLastOneWeek(this.getAllStatusesBetweenDates(baseDateTime.minusDays(6), currentDateTime)).build();
      default:
        break;
    }
    return new PenRequestStats();
  }

  private Pair<Long, Double> getMonthlyPercentGMPBasedOnStatus(final String... statusCode) {
    val startDatePreviousMonth = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).minusDays(59);
    val endDatePreviousMonth = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999).minusDays(30);
    val startDateCurrentMonth = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).minusDays(29);
    val endDateCurrentMonth = LocalDateTime.now();

    val previousMonthResult = this.penRequestRepository.countByPenRequestStatusCodeInAndStatusUpdateDateBetween(List.of(statusCode), startDatePreviousMonth, endDatePreviousMonth);

    val currentMonthResult = this.penRequestRepository.countByPenRequestStatusCodeInAndStatusUpdateDateBetween(List.of(statusCode), startDateCurrentMonth, endDateCurrentMonth);
    return Pair.of(currentMonthResult, findPercentage(previousMonthResult, currentMonthResult));
  }

  private Pair<Long, Double> getMonthlyPercentGMPWithDocsBasedOnStatus(final String... statusCode) {

    val startDatePreviousMonth = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).minusDays(59);
    val endDatePreviousMonth = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59).withNano(999999999).minusDays(30);
    val startDateCurrentMonth = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0).minusDays(29);
    val endDateCurrentMonth = LocalDateTime.now();
    val previousMonthResult = this.penRequestRepository.findNumberOfPenRequestsWithDocumentsStatusCodeInAndStatusUpdateDateBetween(List.of(statusCode), startDatePreviousMonth, endDatePreviousMonth);


    val currentMonthResult = this.penRequestRepository.findNumberOfPenRequestsWithDocumentsStatusCodeInAndStatusUpdateDateBetween(List.of(statusCode), startDateCurrentMonth, endDateCurrentMonth);

    return Pair.of(currentMonthResult, findPercentage(previousMonthResult, currentMonthResult));
  }

  private double findPercentage(long previousMonthResult, long currentMonthResult) {
    final double percentVal;
    if (previousMonthResult == 0 && currentMonthResult != 0) {
      percentVal = currentMonthResult;
    } else if (currentMonthResult == 0 && previousMonthResult != 0) {
      percentVal = -previousMonthResult;
    } else if (currentMonthResult == 0) {
      percentVal = 0.0;
    } else {
      double increase = (double) (currentMonthResult - previousMonthResult) / previousMonthResult;
      percentVal = increase * 100;
    }
    return percentVal;
  }

  private Map<String, Long> getAllStatusesBetweenDates(LocalDateTime startDate, LocalDateTime endDate) {
    Map<String, Long> allStatusMap = new LinkedHashMap<>();
    for (val status : PenRequestStatusCode.values()) {
      val results = this.penRequestRepository.countByPenRequestStatusCodeInAndStatusUpdateDateBetween(List.of(status.toString()), startDate, endDate);
      allStatusMap.put(status.toString(), results);
    }
    return allStatusMap;
  }

  private PenRequestStats getPenRequestsCompletedLastTwelveMonths() {
    LocalDateTime currentDate = LocalDateTime.now();
    Map<String, Long> penReqCompletionsInLast12Months = new LinkedHashMap<>();
    for (int i = 11; i >= 0; i--) {
      LocalDateTime startDate = currentDate.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
      LocalDateTime endDate = currentDate.minusMonths(i).withDayOfMonth(currentDate.minusMonths(i).toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
      val gmpNumbers = this.penRequestRepository.countByPenRequestStatusCodeInAndStatusUpdateDateBetween(Arrays.asList("MANUAL", "AUTO"), startDate, endDate);
      penReqCompletionsInLast12Months.put(startDate.getMonth().toString(), gmpNumbers);
    }
    return PenRequestStats.builder().completionsInLastTwelveMonth(penReqCompletionsInLast12Months).build();
  }

  private PenRequestStats getAverageGMPCompletionTime() {
    val gmpStat = this.penRequestRepository.findCompletionProcessAverageTime();
    return PenRequestStats.builder().averageTimeToCompleteRequest(gmpStat.getAverageCompletionTime()).build();
  }

  private PenRequestStats getPenRequestsCompletedLastWeek() {
    Map<String, Long> penReqCompletionsInLastWeek = new LinkedHashMap<>();
    LocalDateTime currentDate = LocalDateTime.now();
    for (int i = 6; i >= 0; i--) {
      LocalDateTime startDate = currentDate.minusDays(i).withHour(0).withMinute(0).withSecond(0).withNano(0);
      LocalDateTime endDate = currentDate.minusDays(i).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
      val gmpNumbers = this.penRequestRepository.countByPenRequestStatusCodeInAndStatusUpdateDateBetween(Arrays.asList("MANUAL", "AUTO"), startDate, endDate);
      penReqCompletionsInLastWeek.put(startDate.getDayOfWeek().toString(), gmpNumbers);
    }
    return PenRequestStats.builder().completionsInLastWeek(penReqCompletionsInLastWeek).build();
  }

}
