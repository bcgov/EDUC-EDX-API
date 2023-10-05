package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EdxUserDistrictRepository extends JpaRepository<EdxUserDistrictEntity, UUID> {

    Optional<EdxUserDistrictEntity> findEdxUserDistrictEntitiesByDistrictIDAndEdxUserEntity(UUID districtID, EdxUserEntity edxUserEntity);

    @Query(value = """
            SELECT userDistrict.*
            FROM EDX_USER_DISTRICT userDistrict inner join EDX_USER_DISTRICT_ROLE userDistrictRole on userDistrict.EDX_USER_DISTRICT_ID = userDistrictRole.EDX_USER_DISTRICT_ID
            inner join EDX_ROLE role on role.EDX_ROLE_CODE = userDistrictRole.EDX_ROLE_CODE
            inner join EDX_ROLE_PERMISSION rolePermission on role.EDX_ROLE_CODE = rolePermission.EDX_ROLE_CODE
            inner join EDX_PERMISSION permission on rolePermission.EDX_PERMISSION_CODE = permission.EDX_PERMISSION_CODE
            WHERE permission.EDX_PERMISSION_CODE = :permissionCode"""
            , nativeQuery = true)
    List<EdxUserDistrictEntity> findDistrictsByPermission(String permissionCode);

    List<EdxUserDistrictEntity> findAllByExpiryDateBefore(LocalDateTime dateTime);
}
