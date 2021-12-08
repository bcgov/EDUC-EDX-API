package ca.bc.gov.educ.api.edx.constants;

/**
 * The enum Stats type.
 */
public enum StatsType {
  /**
   * query param to return number of completed GMP requests in last 12 months from the current month.
   * ex:- if current month is JANUARY 2021, it will start from JANUARY 2020.
   * JAN 20
   * FEB 30 ......
   */
  COMPLETIONS_LAST_12_MONTH,
  /**
   * query param to return Numbers for different statuses in last 12 month.
   * COMPLETED, 500
   * REJECTED, 20
   * DRAFT, 20
   * RETURNED, 50
   * ABANDONED, 20
   */
  ALL_STATUSES_LAST_12_MONTH,

  /**
   * query param to return Numbers for different statuses in last 6 month.
   * COMPLETED, 500
   * REJECTED, 20
   * DRAFT, 20
   * RETURNED, 50
   * ABANDONED, 20
   */
  ALL_STATUSES_LAST_6_MONTH,

  /**
   * query param to return Numbers for different statuses in last 1 month.
   * COMPLETED, 500
   * REJECTED, 20
   * DRAFT, 20
   * RETURNED, 50
   * ABANDONED, 20
   */
  ALL_STATUSES_LAST_1_MONTH,

  /**
   * query param to return Numbers for different statuses in last 1 week.
   * COMPLETED, 500
   * REJECTED, 20
   * DRAFT, 20
   * RETURNED, 50
   * ABANDONED, 20
   */
  ALL_STATUSES_LAST_1_WEEK,
  /**
   * query param to return number of completed GMP requests in last week from current day.
   * ex:- if current day is Wednesday -> it will show from Last Wednesday to Tuesday.
   * WED 20
   * THURS 30 ......
   */
  COMPLETIONS_LAST_WEEK,
  /**
   * Query to return average processing time to completion for GMP Requests.
   */
  AVERAGE_COMPLETION_TIME,

  /**
   * Percent gmp completion to last month stats type.
   */
  PERCENT_GMP_COMPLETION_TO_LAST_MONTH,

  /**
   * Percent gmp rejected to last month stats type.
   */
  PERCENT_GMP_REJECTED_TO_LAST_MONTH,
  /**
   * Percent gmp completed with documents to last month stats type.
   */
  PERCENT_GMP_COMPLETED_WITH_DOCUMENTS_TO_LAST_MONTH,
  /**
   * Percent gmp abandoned to last month stats type.
   */
  PERCENT_GMP_ABANDONED_TO_LAST_MONTH,
}
