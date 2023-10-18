package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolRoleEntity;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.struct.v1.MoveSchoolData;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class MoveSchoolOrchestratorService {

    protected final SagaService sagaService;

    @Getter(AccessLevel.PRIVATE)
    private final EdxUsersService service;

    private final EdxUserSchoolRepository edxUserSchoolsRepository;

    public MoveSchoolOrchestratorService(SagaService sagaService, EdxUsersService service, EdxUserSchoolRepository edxUserSchoolRepository) {
        this.sagaService = sagaService;
        this.service = service;
        this.edxUserSchoolsRepository = edxUserSchoolRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void copyUsersToNewSchool(MoveSchoolData moveSchoolData) {

        List<EdxUserSchoolEntity> edxUserSchoolEntityList =  edxUserSchoolsRepository.findAllBySchoolID(UUID.fromString(moveSchoolData.getFromSchoolId()));

        List<EdxUserSchoolEntity> edxUserSchoolEntityListToSave = new ArrayList<>();

        log.info("copying {} users to new school", edxUserSchoolEntityList.size());

        for (EdxUserSchoolEntity edxUserSchoolEntity : edxUserSchoolEntityList) {
            log.debug("copying from edxUserSchoolEntity :: {}", edxUserSchoolEntity);
            EdxUserSchoolEntity newEdxUserSchoolEntity = new EdxUserSchoolEntity();
            newEdxUserSchoolEntity.setEdxUserEntity(edxUserSchoolEntity.getEdxUserEntity());
            newEdxUserSchoolEntity.setSchoolID(UUID.fromString(moveSchoolData.getToSchool().getSchoolId()));
            newEdxUserSchoolEntity.setCreateUser(moveSchoolData.getCreateUser());
            newEdxUserSchoolEntity.setUpdateDate(LocalDateTime.now());
            newEdxUserSchoolEntity.setUpdateUser(moveSchoolData.getCreateUser());
            newEdxUserSchoolEntity.setCreateDate(LocalDateTime.now());
            newEdxUserSchoolEntity.setExpiryDate(edxUserSchoolEntity.getExpiryDate());

            Set<EdxUserSchoolRoleEntity> edxUserSchoolRoleEntitySet = new HashSet<>();

            for (EdxUserSchoolRoleEntity edxUserSchoolRole: edxUserSchoolEntity.getEdxUserSchoolRoleEntities()) {
                EdxUserSchoolRoleEntity newEdxUserSchoolRoleEntity = new EdxUserSchoolRoleEntity();
                newEdxUserSchoolRoleEntity.setEdxUserSchoolEntity(newEdxUserSchoolEntity);
                newEdxUserSchoolRoleEntity.setEdxRoleCode(edxUserSchoolRole.getEdxRoleCode());
                newEdxUserSchoolRoleEntity.setCreateUser(moveSchoolData.getCreateUser());
                newEdxUserSchoolRoleEntity.setUpdateDate(LocalDateTime.now());
                newEdxUserSchoolRoleEntity.setUpdateUser(moveSchoolData.getCreateUser());
                newEdxUserSchoolRoleEntity.setCreateDate(LocalDateTime.now());

                edxUserSchoolRoleEntitySet.add(newEdxUserSchoolRoleEntity);
            }

            newEdxUserSchoolEntity.setEdxUserSchoolRoleEntities(edxUserSchoolRoleEntitySet);

            edxUserSchoolEntityListToSave.add(newEdxUserSchoolEntity);
        }

        edxUserSchoolsRepository.saveAll(edxUserSchoolEntityListToSave);

    }
}
