package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Secure Exchange Status Code Table Repository
 *
 * @author Marco Villeneuve
 */
@Repository
public interface SecureExchangeStatusCodeTableRepository extends JpaRepository<SecureExchangeStatusCodeEntity, String> {
}
