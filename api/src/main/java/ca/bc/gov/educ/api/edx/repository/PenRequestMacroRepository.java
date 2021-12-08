package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.PenRequestMacroEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PenRequestMacroRepository extends JpaRepository<PenRequestMacroEntity, UUID> {

  List<PenRequestMacroEntity> findAllByMacroTypeCode(String macroTypeCode);
}
