package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface EdxUserRepository extends JpaRepository<EdxUserEntity, UUID>, EdxUserRepositoryCustom {

  List<EdxUserEntity> findEdxUserEntitiesByDigitalIdentityID(UUID digitalIdentityID);

  boolean existsByDigitalIdentityID(UUID digitalIdentityID);

  @Query(value = " SELECT DISTINCT EMAIL\n" +
    "FROM EDX_USER\n" +
    "WHERE EDX_USER_ID IN (SELECT ES.EDX_USER_ID\n" +
    "                      FROM EDX_USER_SCHOOL ES,\n" +
    "                           EDX_USER_SCHOOL_ROLE ESR,\n" +
    "                           EDX_ROLE ER,\n" +
    "                           EDX_ROLE_PERMISSION ERP\n" +
    "                      WHERE ESR.EDX_ROLE_CODE = ER.EDX_ROLE_CODE\n" +
    "                        AND ES.EDX_USER_SCHOOL_ID = ESR.EDX_USER_SCHOOL_ID\n" +
    "                        AND ER.EDX_ROLE_CODE = ERP.EDX_ROLE_CODE\n" +
    "                        AND ERP.EDX_PERMISSION_CODE=:permissionCode\n" +
    "                        AND ES.SCHOOL_ID = :schoolID)", nativeQuery = true)
  Set<String> findEdxUserEmailBySchoolIDAndPermissionCode(UUID schoolID, String permissionCode);

  @Query(value = " SELECT DISTINCT EMAIL\n" +
          "FROM EDX_USER\n" +
          "WHERE EDX_USER_ID IN (SELECT EUD.EDX_USER_ID\n" +
          "                      FROM EDX_USER_DISTRICT EUD,\n" +
          "                           EDX_USER_DISTRICT_ROLE EUDR,\n" +
          "                           EDX_ROLE ER,\n" +
          "                           EDX_ROLE_PERMISSION ERP\n" +
          "                      WHERE EUDR.EDX_ROLE_CODE = ER.EDX_ROLE_CODE\n" +
          "                        AND EUD.EDX_USER_DISTRICT_ID = EUDR.EDX_USER_DISTRICT_ID\n" +
          "                        AND ER.EDX_ROLE_CODE = ERP.EDX_ROLE_CODE\n" +
          "                        AND ERP.EDX_PERMISSION_CODE=:permissionCode\n" +
          "                        AND EUD.DISTRICT_ID = :districtID)", nativeQuery = true)
  Set<String> findEdxUserEmailByDistrictIDAndPermissionCode(UUID districtID, String permissionCode);

  @Query(value = " SELECT concat_ws(' ', FIRST_NAME, LAST_NAME)\n" +
    "FROM EDX_USER\n" +
    "WHERE EDX_USER_ID IN (SELECT ES.EDX_USER_ID\n" +
    "                      FROM EDX_USER_SCHOOL ES,\n" +
    "                           EDX_USER_SCHOOL_ROLE ESR,\n" +
    "                           EDX_ROLE ER,\n" +
    "                           EDX_ROLE_PERMISSION ERP\n" +
    "                      WHERE ESR.EDX_ROLE_CODE = ER.EDX_ROLE_CODE\n" +
    "                        AND ES.EDX_USER_SCHOOL_ID = ESR.EDX_USER_SCHOOL_ID\n" +
    "                        AND ER.EDX_ROLE_CODE = ERP.EDX_ROLE_CODE\n" +
    "                        AND ERP.EDX_PERMISSION_CODE=:permissionCode\n" +
    "                        AND ES.SCHOOL_ID = :schoolID)", nativeQuery = true)
  Set<String> findEdxUserNamesBySchoolIDAndPermissionCode(UUID schoolID, String permissionCode);

  @Query(value = " SELECT concat_ws(' ', FIRST_NAME, LAST_NAME)\n" +
    "FROM EDX_USER\n" +
    "WHERE EDX_USER_ID IN (SELECT ES.EDX_USER_ID\n" +
    "                      FROM EDX_USER_DISTRICT ES,\n" +
    "                           EDX_USER_DISTRICT_ROLE ESR,\n" +
    "                           EDX_ROLE ER,\n" +
    "                           EDX_ROLE_PERMISSION ERP\n" +
    "                      WHERE ESR.EDX_ROLE_CODE = ER.EDX_ROLE_CODE\n" +
    "                        AND ES.EDX_USER_DISTRICT_ID = ESR.EDX_USER_DISTRICT_ID\n" +
    "                        AND ER.EDX_ROLE_CODE = ERP.EDX_ROLE_CODE\n" +
    "                        AND ERP.EDX_PERMISSION_CODE=:permissionCode\n" +
    "                        AND ES.DISTRICT_ID = :districtID)", nativeQuery = true)
  Set<String> findEdxUserNamesByDistrictIDAndPermissionCode(UUID districtID, String permissionCode);

}
