package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecureExchangeRequestCommentRepository extends JpaRepository<SecureExchangeCommentEntity, UUID> {
  Optional<SecureExchangeCommentEntity> findByContent(String content);

  Optional<SecureExchangeCommentEntity> findByCommentTimestampAndCommentUserNameAndSecureExchangeEntity_SecureExchangeIDAndContent(LocalDateTime commentTimestamp,String commentUserName,UUID secureExchangeID,String content);
  List<SecureExchangeCommentEntity> findSecureExchangeCommentEntitiesBySecureExchangeEntitySecureExchangeID(UUID secureExchangeID);

  @Transactional
  @Modifying
  @Query(value = "delete from SECURE_EXCHANGE_COMMENT e where exists(select 1 from SECURE_EXCHANGE s where s.SECURE_EXCHANGE_ID = e.SECURE_EXCHANGE_ID and s.CREATE_DATE <= :createDate)", nativeQuery = true)
  void deleteByCreateDateBefore(LocalDateTime createDate);
}
