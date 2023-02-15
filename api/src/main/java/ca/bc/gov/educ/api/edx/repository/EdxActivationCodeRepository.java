package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdxActivationCodeRepository extends JpaRepository<EdxActivationCodeEntity, UUID> {

  List<EdxActivationCodeEntity> findEdxActivationCodeByActivationCodeInAndSchoolID(List<String> activationCode, UUID schoolID);

  List<EdxActivationCodeEntity> findEdxActivationCodeByActivationCodeInAndDistrictID(List<String> activationCode, UUID districtID);

  List<EdxActivationCodeEntity> findEdxActivationCodeEntitiesByValidationCode(UUID userActivationValidationCode);

  Optional<EdxActivationCodeEntity> findEdxActivationCodeEntitiesBySchoolIDAndIsPrimaryTrue(UUID schoolID);

  Optional<EdxActivationCodeEntity> findEdxActivationCodeEntitiesByDistrictIDAndIsPrimaryTrue(UUID districtID);

}
