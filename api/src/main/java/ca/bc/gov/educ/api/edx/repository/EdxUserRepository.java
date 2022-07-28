package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EdxUserRepository extends JpaRepository<EdxUserEntity, UUID>, EdxUserRepositoryCustom {

  List<EdxUserEntity> findEdxUserEntitiesByDigitalIdentityID(UUID digitalIdentityID);

  @Modifying
  @Query(
    value = "DELETE FROM EDX_USER_SCHOOL WHERE EDX_USER_SCHOOL_ID = (:edxUserSchoolID)\\:\\:uuid",
    nativeQuery = true
  )
  void deleteEdxUserSchool(@Param("edxUserSchoolID") String school);
}
