package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface secureExchangeRequestRepository extends JpaRepository<SecureExchangeEntity, UUID>, SecureExchangeRepositoryCustom, JpaSpecificationExecutor<SecureExchangeEntity> {

 /* @Query(value = "select pen_retrieval_request_status_code as status, status_update_date as statusupdatedate from pen_retrieval_request where  pen_retrieval_request.status_update_date between :fromDate and :toDate and pen_retrieval_request_status_code in :statuses order by status_update_date", nativeQuery = true)
  List<GmpStats> findStatusAndStatusUpdateDatesBetweenForStatuses(LocalDateTime fromDate, LocalDateTime toDate, List<String> statuses);

  @Query(value = "select avg(STATUS_UPDATE_DATE-INITIAL_SUBMIT_DATE) as averageCompletionTime from pen_retrieval_request WHERE PEN_RETRIEVAL_REQUEST_STATUS_CODE IN ('MANUAL','AUTO')", nativeQuery = true)
  GmpStats findCompletionProcessAverageTime();*/

  @Query(value = "SELECT COUNT (PR_DISTINCT_ID) as col_0_0_ FROM(SELECT  DISTINCT (PRDOC.PEN_RETRIEVAL_REQUEST_ID) AS PR_DISTINCT_ID FROM PEN_RETRIEVAL_REQUEST_DOCUMENT PRDOC INNER JOIN PEN_RETRIEVAL_REQUEST PR ON PRDOC.PEN_RETRIEVAL_REQUEST_ID = PR.PEN_RETRIEVAL_REQUEST_ID WHERE PR.pen_retrieval_request_status_code in :statuses and PR.status_update_date between :startDate and :endDate)", nativeQuery = true)
  long findNumberOfPenRequestsWithDocumentsStatusCodeInAndStatusUpdateDateBetween(List<String> statuses, LocalDateTime startDate, LocalDateTime endDate);

  long countByPenRequestStatusCodeInAndStatusUpdateDateBetween(List<String> statuses, LocalDateTime startDate, LocalDateTime endDate);
}
