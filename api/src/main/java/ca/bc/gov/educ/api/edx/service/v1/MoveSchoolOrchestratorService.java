package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserSchoolMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchool;
import ca.bc.gov.educ.api.edx.struct.v1.MoveSchoolData;
import java.util.List;
import java.util.Optional;
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

    private static final EdxUserMapper userMapper = EdxUserMapper.mapper;

    private static final EdxUserSchoolMapper USER_SCHOOL_MAPPER = EdxUserSchoolMapper.mapper;

    @Getter(AccessLevel.PRIVATE)
    private final EdxUsersService service;

    public MoveSchoolOrchestratorService(SagaService sagaService, EdxUsersService service) {
        this.sagaService = sagaService;
        this.service = service;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void moveUsersToNewSchool(MoveSchoolData moveSchoolData, SagaEntity saga) {
        List<EdxUser> userEntities =
                getService().findEdxUsers(Optional.empty(), Optional.of(UUID.fromString(moveSchoolData.getFromSchoolId())),
                        null, null, Optional.empty())
                        .stream()
                        .map(userMapper::toStructure).toList();

        log.info("Moving {} users to new school", userEntities.size());

        List<EdxUserSchool> userSchoolEntity = userEntities.stream().flatMap(edxUser -> edxUser.getEdxUserSchools().stream()).toList();
        List<EdxUserSchool> matchedSchoolEntity = userSchoolEntity.stream().filter(edxUserSchool -> edxUserSchool.getSchoolID().equals(UUID.fromString(moveSchoolData.getFromSchoolId()))).toList();


        for(EdxUserSchool schoolEntity: matchedSchoolEntity) {
            getService().moveEdxUsersToNewSchool(UUID.fromString(schoolEntity.getEdxUserID()), USER_SCHOOL_MAPPER.toModel(schoolEntity), UUID.fromString(moveSchoolData.getToSchool().getSchoolId()));
        }
    }
}
