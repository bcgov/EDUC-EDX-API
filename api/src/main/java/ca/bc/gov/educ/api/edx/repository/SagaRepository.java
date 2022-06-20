package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * The interface Saga repository.
 */
@Repository
public interface SagaRepository extends JpaRepository<SagaEntity, UUID> {
  /**
   * Find all by status in list.
   *
   * @param statuses the statuses
   * @return the list
   */
  List<SagaEntity> findAllByStatusIn(List<String> statuses);

  /**
   * Find all by create date before list.
   *
   * @param createDate the create date
   * @return the list
   */
  List<SagaEntity> findAllByCreateDateBefore(LocalDateTime createDate);

  @Transactional
  @Modifying
  @Query("delete from SagaEntity where createDate <= :createDate")
  void deleteByCreateDateBefore(LocalDateTime createDate);
}
