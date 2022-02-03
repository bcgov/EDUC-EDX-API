package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;

import java.util.List;
import java.util.UUID;

public interface SecureExchangeRepositoryCustom {

  List<SecureExchangeEntity> findSecureExchange(final UUID edxUserSchoolID, final UUID edxUserDistrictID, final UUID ministryOwnershipTeamID, final UUID ministryContactTeamID, final UUID edxUserID, final String status);
}
