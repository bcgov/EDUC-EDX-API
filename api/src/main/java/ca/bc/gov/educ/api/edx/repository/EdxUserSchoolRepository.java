package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdxUserSchoolRepository extends JpaRepository<EdxUserSchoolEntity, UUID> {
  @Query(value = " SELECT DISTINCT MINCODE\n" +
    "         FROM EDX_USER_SCHOOL userSchool inner join EDX_USER_SCHOOL_ROLE userSchoolRole on userSchool.EDX_USER_SCHOOL_ID = userSchoolRole.EDX_USER_SCHOOL_ID\n" +
    "         inner join EDX_ROLE role on role.EDX_ROLE_ID = userSchoolRole.EDX_ROLE_ID\n" +
    "         inner join EDX_ROLE_PERMISSION rolePermission on role.EDX_ROLE_ID = rolePermission.EDX_ROLE_ID\n" +
    "         inner join EDX_PERMISSION permission on rolePermission.EDX_PERMISSION_ID = permission.EDX_PERMISSION_ID\n" +
    "         WHERE permission.NAME = :permissionName\n" +
    "         ORDER BY MINCODE", nativeQuery = true)
  List<String> findSchoolsByPermission(String permissionName);

  Optional<EdxUserSchoolEntity> findEdxUserSchoolEntitiesByMincodeAndEdxUserEntity(String mincode, EdxUserEntity edxUserEntity);


}
