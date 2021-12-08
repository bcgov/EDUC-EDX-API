package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;

import java.util.List;
import java.util.UUID;

public interface PenRequestRepositoryCustom {
  /**
   * These parameters are optional, so if these values are not passed it will return all the pen requests.
   *
   * @param digitalID the digitalID for the rows to be filtered from DB. <b>OPTIONAL</b>
   * @param status    the status for the rows to be filtered.<b>OPTIONAL</b>
   * @param pen       the pen number of the student.
   * @return List of {@link PenRequestEntity}
   */
  List<PenRequestEntity> findPenRequests(UUID digitalID, String status, String pen);
}
