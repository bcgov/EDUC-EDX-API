package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class EdxUsersService {

  @Getter(AccessLevel.PRIVATE)
  private final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserRepository edxUserRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserSchoolRepository edxUserSchoolsRepository;

  @Autowired
  public EdxUsersService(final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository, final EdxUserSchoolRepository edxUserSchoolsRepository, final EdxUserRepository edxUserRepository) {
    this.ministryOwnershipTeamRepository = ministryOwnershipTeamRepository;
    this.edxUserSchoolsRepository = edxUserSchoolsRepository;
    this.edxUserRepository = edxUserRepository;
  }

  public List<MinistryOwnershipTeamEntity> getMinistryTeamsList() {
    return this.getMinistryOwnershipTeamRepository().findAll();
  }

  public List<EdxUserSchoolEntity> getEdxUserSchoolsList() {
    return this.getEdxUserSchoolsRepository().findAll();
  }

  public EdxUserEntity retrieveEdxUserByID(final UUID edxUserID) {
    var res = this.getEdxUserRepository().findById(edxUserID);
    if (res.isPresent()) {
      return res.get();
    } else {
      throw new EntityNotFoundException(EdxUser.class, "edxUserID", edxUserID.toString());
    }
  }

  public List<String> getEdxUserSchoolsList(String permissionName) {
    return this.getEdxUserSchoolsRepository().findSchoolsByPermission(permissionName);
  }

}
