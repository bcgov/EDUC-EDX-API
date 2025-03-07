package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdxUserSchoolRepository extends JpaRepository<EdxUserSchoolEntity, UUID> {
  @Query(value = " SELECT userSchool.*\n" +
    "         FROM EDX_USER_SCHOOL userSchool inner join EDX_USER_SCHOOL_ROLE userSchoolRole on userSchool.EDX_USER_SCHOOL_ID = userSchoolRole.EDX_USER_SCHOOL_ID\n" +
    "         inner join EDX_ROLE role on role.EDX_ROLE_CODE = userSchoolRole.EDX_ROLE_CODE\n" +
    "         inner join EDX_ROLE_PERMISSION rolePermission on role.EDX_ROLE_CODE = rolePermission.EDX_ROLE_CODE\n" +
    "         inner join EDX_PERMISSION permission on rolePermission.EDX_PERMISSION_CODE = permission.EDX_PERMISSION_CODE\n" +
    "         WHERE permission.EDX_PERMISSION_CODE = :permissionCode\n", nativeQuery = true)
  List<EdxUserSchoolEntity> findSchoolsByPermission(String permissionCode);

  Optional<EdxUserSchoolEntity> findEdxUserSchoolEntitiesBySchoolIDAndEdxUserEntity(UUID schoolID, EdxUserEntity edxUserEntity);

  List<EdxUserSchoolEntity> findAllBySchoolID(UUID schoolID);

  List<EdxUserSchoolEntity> findAllBySchoolIDIn(List<UUID> schoolIDs);

  List<EdxUserSchoolEntity> findAllByExpiryDateBefore(LocalDateTime dateTime);

  List<EdxUserSchoolEntity> findAllBySchoolIDAndExpiryDateIsNull(UUID schoolID);

}
