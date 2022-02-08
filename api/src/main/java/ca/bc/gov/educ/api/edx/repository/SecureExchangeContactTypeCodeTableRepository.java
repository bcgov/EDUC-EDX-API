package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeContactTypeCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Secure Exchange Contact Type Code Table Repository
 *
 * @author Marco Villeneuve
 */
@Repository
public interface SecureExchangeContactTypeCodeTableRepository extends JpaRepository<SecureExchangeContactTypeCodeEntity, String> {
}
