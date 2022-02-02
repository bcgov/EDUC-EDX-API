package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SecureExchangeRequestRepository extends JpaRepository<SecureExchangeEntity, UUID>, SecureExchangeRepositoryCustom, JpaSpecificationExecutor<SecureExchangeEntity> {

}
