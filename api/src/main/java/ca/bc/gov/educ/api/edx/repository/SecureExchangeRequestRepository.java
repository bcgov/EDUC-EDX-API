package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.custom.ICountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute;
import ca.bc.gov.educ.api.edx.model.v1.custom.IStatsSecureExchangeCreatedWithInstitute;
import java.util.List;
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

    @Query(value = "SELECT date_trunc('month', create_date) AS localDateTimeMonth, " +
        "COUNT(*) AS total " +
        "FROM secure_exchange " +
        "WHERE create_date >= CURRENT_DATE - make_interval(months => :months) " +
        "AND secure_exchange_contact_type_code = :instituteType " +
        "GROUP BY localDateTimeMonth " +
        "ORDER BY localDateTimeMonth",
        nativeQuery = true)
    List<IStatsSecureExchangeCreatedWithInstitute> countSecureExchangesCreatedWithByMonth(String instituteType, Integer months);

    @Query(value = """
        SELECT contact_identifier AS contactIdentifier, count(*) AS total 
        FROM secure_exchange 
        WHERE secure_exchange_contact_type_code = :instituteType 
        GROUP BY contact_identifier;
        """, nativeQuery = true)
    List<ICountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute> countSecureExchangesCreatedWithInstituteTypeGroupedByInstitute(String instituteType);

}
