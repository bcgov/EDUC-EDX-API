package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;

import java.util.List;
import java.util.UUID;

public interface SecureExchangeRepositoryCustom {
  /**
   * These parameters are optional, so if these values are not passed it will return all the secure exchanges.
   *
   * @param digitalID the digitalID for the rows to be filtered from DB. <b>OPTIONAL</b>
   * @param status    the status for the rows to be filtered.<b>OPTIONAL</b>
   * @return List of {@link SecureExchangeEntity}
   */
  List<SecureExchangeEntity> findSecureExchange(UUID digitalID, String status);
}
