package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface SecureExchangeRequestRepository extends JpaRepository<SecureExchangeEntity, UUID>, SecureExchangeRepositoryCustom, JpaSpecificationExecutor<SecureExchangeEntity> {

    @Transactional
    @Modifying
    @Query("delete from SecureExchangeEntity where createDate <= :createDate and secureExchangeStatusCode='CLOSED'")
    void deleteByCreateDateBefore(LocalDateTime createDate);

}
