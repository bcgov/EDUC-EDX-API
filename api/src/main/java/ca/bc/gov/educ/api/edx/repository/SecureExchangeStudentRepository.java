package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SecureExchangeStudentRepository extends JpaRepository<SecureExchangeStudentEntity, UUID> {

    @Transactional
    @Modifying
    @Query(value = "delete from SECURE_EXCHANGE_STUDENT e where exists(select 1 from SECURE_EXCHANGE s where s.SECURE_EXCHANGE_ID = e.SECURE_EXCHANGE_ID and s.CREATE_DATE <= :createDate)", nativeQuery = true)
    void deleteByCreateDateBefore(LocalDateTime createDate);
}
