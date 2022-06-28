package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdxUserSchoolRoleRepository extends JpaRepository<EdxUserSchoolRoleEntity, UUID> {

    Optional<EdxUserSchoolRoleEntity> findEdxUserSchoolRoleEntityByEdxUserSchoolEntity_EdxUserSchoolIDAndEdxRoleCode(UUID exdUserSchoolId , String edxRoleCode);

}
