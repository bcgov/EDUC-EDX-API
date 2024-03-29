package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EdxUserRepositoryCustom {
  List<EdxUserEntity> findEdxUsers(final Optional<UUID> digitalId, final Optional<UUID> schoolID, final String firstName, final String lastName, final Optional<UUID> districtID);
}
