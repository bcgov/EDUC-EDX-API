package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserSchoolMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MoveSchoolOrchestratorService {

    private final RestUtils restUtils;

    protected final SagaService sagaService;

    private static final EdxUserMapper userMapper = EdxUserMapper.mapper;

    private static final EdxUserSchoolMapper USER_SCHOOL_MAPPER = EdxUserSchoolMapper.mapper;

    @Getter(AccessLevel.PRIVATE)
    private final EdxUsersService service;

    public MoveSchoolOrchestratorService(RestUtils restUtils, SagaService sagaService, EdxUsersService service) {
        this.restUtils = restUtils;
        this.sagaService = sagaService;
        this.service = service;
    }

    public boolean findSchoolNumberInDistrict(String schoolNumber, String districtId)  {
         List<School> schools = restUtils.getSchoolNumberInDistrict(UUID.randomUUID(), schoolNumber, districtId);
         return !schools.isEmpty();
    }

    public School createNewSchool(MoveSchoolSagaData moveSchoolSagaData, SagaEntity saga, boolean hasSchoolNumber) {
        School createSchoolObj = createSchoolObject(moveSchoolSagaData, hasSchoolNumber);
        School school  = restUtils.createSchool(UUID.randomUUID(), createSchoolObj);
        if(school == null) {
            throw new SagaRuntimeException("Create School Failed for: " + moveSchoolSagaData.getSchool().getSchoolNumber() + "," + moveSchoolSagaData.getSchool().getDisplayName());
        }
        moveSchoolSagaData.setNewSchoolId(school.getSchoolId());
        moveSchoolSagaData.setNewSchoolNumber(school.getSchoolNumber());
        try {
           updateSagaDataInternal(moveSchoolSagaData, saga);
         } catch (JsonProcessingException e) {
          throw new SagaRuntimeException(e);
        }
        return school;
    }

    public School updateSchool(MoveSchoolSagaData moveSchoolSagaData, SagaEntity saga) {
        List<School> schools = restUtils.getSchoolById(UUID.randomUUID(), moveSchoolSagaData.getSchool().getSchoolId());
        if(schools.isEmpty()) {
            throw new SagaRuntimeException("Update failed: School" + moveSchoolSagaData.getSchool().getSchoolId() + " does not exist.");
        }

        if(schools.size() > 1) {
            throw new SagaRuntimeException("Update failed: Multiple matches found for: " + moveSchoolSagaData.getSchool().getSchoolId());
        }
        School schoolToUpdate = schools.get(0);
        schoolToUpdate.setClosedDate(moveSchoolSagaData.getMoveDate());
        RequestUtil.setAuditColumnsForUpdate(schoolToUpdate);
        School school  = restUtils.updateSchool(UUID.randomUUID(), schoolToUpdate);
        try {
            if(school != null) {
                updateSagaDataInternal(moveSchoolSagaData, saga);
            }
        } catch (JsonProcessingException e) {
            throw new SagaRuntimeException(e);
        }
        return school;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void moveUsersToNewSchool(MoveSchoolSagaData moveSchoolSagaData, SagaEntity saga) {
        List<EdxUser> userEntities =
                getService().findEdxUsers(Optional.empty(), Optional.of(UUID.fromString(moveSchoolSagaData.getSchool().getSchoolId())),
                        null, null, Optional.empty())
                        .stream()
                        .map(userMapper::toStructure).collect(Collectors.toList());

        List<EdxUserSchool> userSchoolEntity = userEntities.stream().flatMap(edxUser -> edxUser.getEdxUserSchools().stream()).collect(Collectors.toList());
        List<EdxUserSchool> matchedSchoolEntity = userSchoolEntity.stream().filter(edxUserSchool -> edxUserSchool.getSchoolID().equals(UUID.fromString(moveSchoolSagaData.getSchool().getSchoolId()))).collect(Collectors.toList());


        for(EdxUserSchool schoolEntity: matchedSchoolEntity) {
            getService().moveEdxUsersToNewSchool(UUID.fromString(schoolEntity.getEdxUserID()), USER_SCHOOL_MAPPER.toModel(schoolEntity), UUID.fromString(moveSchoolSagaData.getNewSchoolId()));
        }

        try {
            updateSagaDataInternal(moveSchoolSagaData, saga);
        } catch (JsonProcessingException e) {
            throw new SagaRuntimeException(e);
        }
    }

    private void updateSagaDataInternal(MoveSchoolSagaData moveSchoolSagaData, SagaEntity sagaEntity) throws JsonProcessingException {
        sagaEntity.setSchoolID(UUID.fromString(moveSchoolSagaData.getSchool().getSchoolId()));
        sagaEntity.setPayload(JsonUtil.getJsonStringFromObject(moveSchoolSagaData)); // update the payload which will be updated in DB.
        this.sagaService.updateSagaRecord(sagaEntity); // save updated payload to DB again.
    }

    private School createSchoolObject(MoveSchoolSagaData moveSchoolSagaData, boolean hasSchoolNumber) {
        School createSchool = new School();
        BeanUtils.copyProperties(moveSchoolSagaData.getSchool(), createSchool);
        RequestUtil.setAuditColumnsForCreate(createSchool);

        createSchool.setSchoolId(null);
        createSchool.setNotes(null);
        createSchool.setMincode(null);
        createSchool.setOpenedDate(moveSchoolSagaData.getMoveDate());

        if(!createSchool.getGrades().isEmpty()) {
            createSchool.getGrades().stream().forEach(grade -> {
                grade.setSchoolId(null);
                grade.setSchoolGradeId(null);
            });
        }

        if(!createSchool.getNeighborhoodLearning().isEmpty()) {
            createSchool.getNeighborhoodLearning().stream().forEach(grade -> {
                grade.setNeighborhoodLearningId(null);
                grade.setSchoolId(null);
            });
        }

        if(hasSchoolNumber) {
            createSchool.setSchoolNumber(null);
        }
        return createSchool;
    }
}
