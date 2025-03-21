package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EdxUserSchoolLightRepository extends JpaRepository<EdxUserSchoolLightEntity, UUID> {
  List<EdxUserSchoolLightEntity> findAllByExpiryDateBefore(LocalDateTime dateTime);
}
