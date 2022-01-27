package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<SecureExchangeDocumentEntity, UUID> {
  List<SecureExchangeDocumentEntity> findBySecureExchangeSecureExchangeID(UUID penRequestId);

  // this query will only filter where document data is not null and file size greater than zero, so that system is not pulling a lot of records from db.
  List<SecureExchangeDocumentEntity> findAllBySecureExchangeSecureExchangeStatusCodeInAndFileSizeGreaterThanAndDocumentDataIsNotNull(List<String> penRequestStatusCodes, int fileSize);
}
