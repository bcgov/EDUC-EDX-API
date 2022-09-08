package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdxUserDistrictRepository extends JpaRepository<EdxUserDistrictEntity, UUID> {

    Optional<EdxUserDistrictEntity> findEdxUserDistrictEntitiesByDistrictIDAndEdxUserEntity(UUID districtID, EdxUserEntity edxUserEntity);

}
