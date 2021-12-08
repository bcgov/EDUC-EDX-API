package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * place-holder struct to return all the stats with a single class with fields getting populated as per the query.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PenRequestStats {
  /**
   * contains number of completed GMP requests in last 12 months from the current month.
   * ex:- if current month is JANUARY 2021, it will start from JANUARY 2020.
   * JAN 20
   * FEB 30 ......
   */
  Map<String, Long> completionsInLastTwelveMonth;
  /**
   * contains number of completed GMP requests in last week from current day.
   * ex:- if current day is Wednesday -> it will show from Last Wednesday to Tuesday.
   * WED 20
   * THURS 30 ......
   */
  Map<String, Long> completionsInLastWeek;

  /**
   * Numbers for different statuses in last 12 month.
   * COMPLETED, 500
   * REJECTED, 20
   * DRAFT, 20
   * RETURNED, 50
   * ABANDONED, 20
   */
  Map<String, Long> allStatsLastTwelveMonth;

  /**
   * Numbers for different statuses in last 6 month.
   * COMPLETED, 500
   * REJECTED, 20
   * DRAFT, 20
   * RETURNED, 50
   * ABANDONED, 20
   */
  Map<String, Long> allStatsLastSixMonth;

  /**
   * Numbers for different statuses in last 1 month.
   * COMPLETED, 500
   * REJECTED, 20
   * DRAFT, 20
   * RETURNED, 50
   * ABANDONED, 20
   */
  Map<String, Long> allStatsLastOneMonth;

  /**
   * Numbers for different statuses in last 1 week.
   * COMPLETED, 500
   * REJECTED, 20
   * DRAFT, 20
   * RETURNED, 50
   * ABANDONED, 20
   */
  Map<String, Long> allStatsLastOneWeek;

  /**
   * The Average time to complete request.
   */
  Double averageTimeToCompleteRequest;

  /**
   * The Percent completed gmp to last month.
   */
  Double percentCompletedGmpToLastMonth;
  /**
   * The Percent rejected gmp to last month.
   */
  Double percentRejectedGmpToLastMonth;
  /**
   * The Percent abandoned gmp to last month.
   */
  Double percentAbandonedGmpToLastMonth;
  /**
   * The Percent gmp completed with documents to last month.
   */
  Double percentGmpCompletedWithDocumentsToLastMonth;
  /**
   * The Gmp completed in current month.
   */
  Long gmpCompletedInCurrentMonth;
  /**
   * The Gmp abandoned in current month.
   */
  Long gmpAbandonedInCurrentMonth;
  /**
   * The Gmp rejected in current month.
   */
  Long gmpRejectedInCurrentMonth;

  /**
   * The Gmp completed with docs in current month.
   */
  Long gmpCompletedWithDocsInCurrentMonth;
}
