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
    "                        AND ES.MINCODE = :mincode)", nativeQuery = true)
  Set<String> findEdxUserEmailByMincodeAndPermissionCode(String mincode, String permissionCode);
}
