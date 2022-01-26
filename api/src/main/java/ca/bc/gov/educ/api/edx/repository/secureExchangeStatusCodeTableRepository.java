package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Pen Request Status Code Table Repository
 *
 * @author Marco Villeneuve
 */
@Repository
public interface secureExchangeStatusCodeTableRepository extends JpaRepository<SecureExchangeStatusCodeEntity, Long> {
}
