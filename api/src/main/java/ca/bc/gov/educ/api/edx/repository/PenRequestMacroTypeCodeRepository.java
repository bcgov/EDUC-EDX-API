package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.PenRequestMacroTypeCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PenRequestMacroTypeCodeRepository extends JpaRepository<PenRequestMacroTypeCodeEntity, String> {
}
