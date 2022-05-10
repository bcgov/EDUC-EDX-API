package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

    @Getter(AccessLevel.PRIVATE)
    private final EdxUserSchoolRoleRepository edxUserSchoolRoleRepository;

    @Getter(AccessLevel.PRIVATE)
    private final EdxRoleRepository edxRoleRepository;

    @Autowired
    public EdxUsersService(final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository, final EdxUserSchoolRepository edxUserSchoolsRepository, final EdxUserRepository edxUserRepository, EdxUserSchoolRoleRepository edxUserSchoolRoleRepository, EdxRoleRepository edxRoleRepository) {
        this.ministryOwnershipTeamRepository = ministryOwnershipTeamRepository;
        this.edxUserSchoolsRepository = edxUserSchoolsRepository;
        this.edxUserRepository = edxUserRepository;
        this.edxUserSchoolRoleRepository = edxUserSchoolRoleRepository;
        this.edxRoleRepository = edxRoleRepository;
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

    public List<EdxUserEntity> findEdxUsers(UUID digitalId) {
        return this.getEdxUserRepository().findEdxUserEntitiesByDigitalIdentityID(digitalId);
    }

    public EdxUserEntity createEdxUser(EdxUserEntity edxUserEntity) {
        return this.getEdxUserRepository().save(edxUserEntity);
    }


    public EdxUserSchoolEntity createEdxUserSchool(UUID id, EdxUserSchoolEntity edxUserSchoolEntity) {
        val entityOptional = getEdxUserRepository().findById(id);
        val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, "edxUserID", id.toString()));

        entity.getEdxUserSchoolEntities().add(edxUserSchoolEntity);
        val updatedEntity = getEdxUserRepository().save(entity);
        //attached entity Hibernate will update the entity state.
        return edxUserSchoolEntity;
    }

    public EdxUserSchoolRoleEntity createEdxUserSchoolRole(UUID id, EdxUserSchoolRoleEntity edxUserSchoolRoleEntity) {
        val entityOptional = getEdxUserSchoolsRepository().findById(edxUserSchoolRoleEntity.getEdxUserSchoolEntity().getEdxUserSchoolID());
        val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserSchoolEntity.class, "edxUserSchoolID", id.toString()));
        if (entity.getEdxUserID().equals(id)) {
            entity.getEdxUserSchoolRoleEntities().add(edxUserSchoolRoleEntity);
        }

        return this.getEdxUserSchoolRoleRepository().save(edxUserSchoolRoleEntity);
    }

    public void deleteEdxUserById(UUID id) {
        val entityOptional = getEdxUserRepository().findById(id);
        val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, "edxUserID", id.toString()));
        this.getEdxUserRepository().delete(entity);

    }

    public void deleteEdxSchoolUserById(UUID id, UUID edxUserSchoolId) {
        val entityOptional = getEdxUserSchoolsRepository().findById(edxUserSchoolId);
        val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserSchoolEntity.class, "edxUserSchoolID", edxUserSchoolId.toString()));
        if (entity.getEdxUserID().equals(id)) {
            this.getEdxUserSchoolsRepository().delete(entity);
        } else {
            throw new EntityNotFoundException(EdxUserSchoolEntity.class, "edxUserID", id.toString());
        }
    }

    public void deleteEdxSchoolUserRoleById(UUID id, UUID edxUserSchoolRoleId) {
        val entityOptional = getEdxUserSchoolRoleRepository().findById(edxUserSchoolRoleId);
        val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserSchoolRoleEntity.class, "edxUserSchoolRoleID", edxUserSchoolRoleId.toString()));
        if (entity.getEdxUserSchoolEntity() != null && entity.getEdxUserSchoolEntity().getEdxUserID().equals(id)) {
            this.getEdxUserSchoolRoleRepository().delete(entity);
        } else {
            throw new EntityNotFoundException(EdxUserSchoolRoleEntity.class, "edxUserID", id.toString());
        }

    }

    public List<EdxRoleEntity> findAllEdxRoles() {
        return this.getEdxRoleRepository().findAll();
    }

  /*public EdxUserDistrictEntity createEdxUserDistrict(EdxUserDistrictEntity edxUserDistrictEntity) {
    return this.getEdx

  }*/
}
