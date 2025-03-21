package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictLightEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface EdxUserDistrictLightRepository extends JpaRepository<EdxUserDistrictLightEntity, UUID> {
    List<EdxUserDistrictLightEntity> findAllByExpiryDateBefore(LocalDateTime dateTime);
}
