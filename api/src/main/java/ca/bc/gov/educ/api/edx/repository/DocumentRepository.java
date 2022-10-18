package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<SecureExchangeDocumentEntity, UUID> {
  List<SecureExchangeDocumentEntity> findBySecureExchangeEntitySecureExchangeID(UUID secureExchangeId);

  // this query will only filter where document data is not null and file size greater than zero, so that system is not pulling a lot of records from db.
  List<SecureExchangeDocumentEntity> findAllBySecureExchangeEntitySecureExchangeStatusCodeInAndFileSizeGreaterThanAndDocumentDataIsNotNull(List<String> secureExchangeStatusCodes, int fileSize);

  @Transactional
  @Modifying
  @Query(value = "delete from SECURE_EXCHANGE_DOCUMENT e where exists(select 1 from SECURE_EXCHANGE s where s.SECURE_EXCHANGE_ID = e.SECURE_EXCHANGE_ID and s.SECURE_EXCHANGE_STATUS_CODE='CLOSED' and s.CREATE_DATE <= :createDate)", nativeQuery = true)
  void deleteByCreateDateBefore(LocalDateTime createDate);
}
