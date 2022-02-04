package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MinistryOwnershipTeamRepository extends JpaRepository<MinistryOwnershipTeamEntity, UUID> {

}
