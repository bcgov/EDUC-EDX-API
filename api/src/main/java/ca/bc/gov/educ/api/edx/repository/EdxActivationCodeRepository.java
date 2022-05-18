package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EdxActivationCodeRepository extends JpaRepository<EdxActivationCodeEntity, UUID> {

  List<EdxActivationCodeEntity> findEdxActivationCodeByActivationCodeInAndMincode(List<String> activationCode, String mincode);
}
