package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.exception.EdxRuntimeException;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity.SagaEntityBuilder;
import ca.bc.gov.educ.api.edx.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.struct.v1.OnboardDistrictUserSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.OnboardSchoolUserSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.OnboardingFileRow;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.*;

import static ca.bc.gov.educ.api.edx.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.ONBOARD_DISTRICT_USER_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.ONBOARD_SCHOOL_USER_SAGA;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.STARTED;
import static lombok.AccessLevel.PRIVATE;

@Service
@Slf4j
public class EdxFileOnboardingService {
  private final SagaService sagaService;
  private final RestUtils restUtils;

  @Getter(PRIVATE)
  private final Map<String, Orchestrator> orchestratorMap = new HashMap<>();

  public EdxFileOnboardingService(SagaService sagaService, RestUtils restUtils) {
    this.sagaService = sagaService;
    this.restUtils = restUtils;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public List<SagaEntity> processOnboardingFile(byte [] fileContents, String createUser) {
    try {
      List<OnboardingFileRow> onboardingFileRows = new CsvToBeanBuilder<OnboardingFileRow>(new InputStreamReader(new ByteArrayInputStream(fileContents)))
        .withType(OnboardingFileRow.class)
        .withSkipLines(1)
        .build()
        .parse();

      List<OnboardingFileRow> validRows = new ArrayList<>();
      onboardingFileRows.forEach(onboardingFileRow -> {
        if(StringUtils.isNotEmpty(onboardingFileRow.getMincode())) {
          validRows.add(onboardingFileRow);
        }
      });

      return writeNewSagaRecordsForProcessing(validRows, createUser);
    }catch(Exception e){
      throw new EdxRuntimeException("Error occurred parsing the uploading onboarding file :: " + e.getMessage());
    }
  }

  public List<SagaEntity> writeNewSagaRecordsForProcessing(List<OnboardingFileRow> fileOnboardingRows, String createUser) {
    List<SagaEntity> sagaEntities = new ArrayList<>();
    var districtMap = restUtils.getDistrictNumberMap();
    var schoolMap = restUtils.getSchoolMincodeMap();
    fileOnboardingRows.forEach(onboardingFileRow -> {
      try {
        if (onboardingFileRow.getMincode().length() < 4) {
          var district = districtMap.get(onboardingFileRow.getMincode());
          if (district != null) {
            var sagaRecord = prepareSagaRecord(onboardingFileRow, createUser, UUID.fromString(district.getDistrictId()), null);
            sagaEntities.add(sagaService.createSagaRecord(sagaRecord));
          } else {
            log.info("Skipped district code :: " + onboardingFileRow.getMincode() + " :: no district was found in the cache containing this value");
          }
        } else {
          var school = schoolMap.get(onboardingFileRow.getMincode());
          if (school != null) {
            var sagaRecord = prepareSagaRecord(onboardingFileRow, createUser, null, UUID.fromString(school.getSchoolId()));
            sagaEntities.add(sagaService.createSagaRecord(sagaRecord));
          } else {
            log.info("Skipped school mincode :: " + onboardingFileRow.getMincode() + " :: no school was found in the cache containing this value");
          }
        }
      } catch (JsonProcessingException e) {
        log.info("Skipped mincode :: " + onboardingFileRow.getMincode() + " :: due to a JSON parsing exception: " + e.getMessage());
      }
    });
    return sagaEntities;
  }

  private SagaEntity prepareSagaRecord(final OnboardingFileRow onboardingFileRow, final String createUser, UUID districtID, UUID schoolID) throws JsonProcessingException {
    SagaEntityBuilder builder = SagaEntity.builder()
      .createUser(createUser)
      .updateUser(createUser)
      .status(STARTED.toString())
      .sagaState(INITIATED.toString())
      .schoolID(schoolID)
      .districtID(districtID)
      .createDate(LocalDateTime.now())
      .updateDate(LocalDateTime.now())
      .emailId(onboardingFileRow.getEmail())
      .sagaCompensated(false);

    if (districtID == null) {
      OnboardSchoolUserSagaData payload = new OnboardSchoolUserSagaData();
      payload.setFirstName(onboardingFileRow.getFirstName());
      payload.setLastName(onboardingFileRow.getLastName());
      payload.setEmail(onboardingFileRow.getEmail());
      payload.setMincode(onboardingFileRow.getMincode());
      payload.setSchoolID(schoolID);
      builder.sagaName(ONBOARD_SCHOOL_USER_SAGA.toString());
      builder.payload(JsonUtil.getJsonStringFromObject(payload));
    } else {
      OnboardDistrictUserSagaData payload = new OnboardDistrictUserSagaData();
      payload.setFirstName(onboardingFileRow.getFirstName());
      payload.setLastName(onboardingFileRow.getLastName());
      payload.setEmail(onboardingFileRow.getEmail());
      payload.setMincode(onboardingFileRow.getMincode());
      payload.setDistrictID(districtID);
      builder.sagaName(ONBOARD_DISTRICT_USER_SAGA.toString());
      builder.payload(JsonUtil.getJsonStringFromObject(payload));
    }

    return builder.build();
  }

}
