package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.model.v1.custom.IStatsSecureExchangeCreatedWithInstitute;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.struct.v1.CountSecureExchangeCreatedWithInstituteByMonth;
import ca.bc.gov.educ.api.edx.struct.v1.CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.struct.v1.SchoolWithoutActiveSecureExchangeUser;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EdxStatsService {

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserSchoolRepository edxUserSchoolRepository;

  @Getter(AccessLevel.PRIVATE)
  private final RestUtils restUtils;

  @Autowired
  public EdxStatsService(final RestUtils restUtils, final SecureExchangeRequestRepository secureExchangeRequestRepository, final EdxUserSchoolRepository edxUserSchoolRepository) {
    this.restUtils = restUtils;
    this.secureExchangeRequestRepository = secureExchangeRequestRepository;
    this.edxUserSchoolRepository = edxUserSchoolRepository;
  }

  public List<CountSecureExchangeCreatedWithInstituteByMonth> countSecureExchangesCreatedWithInstitute(String instituteType, Integer months) {
    List<IStatsSecureExchangeCreatedWithInstitute> results = this.secureExchangeRequestRepository.countSecureExchangesCreatedWithByMonth(instituteType, months);

    List<CountSecureExchangeCreatedWithInstituteByMonth> statsCreatedWithInstitutesList = new ArrayList<>();

    for (var result : results) {
      CountSecureExchangeCreatedWithInstituteByMonth countSecureExchangeCreatedWithInstituteByMonth = new CountSecureExchangeCreatedWithInstituteByMonth();
      countSecureExchangeCreatedWithInstituteByMonth.setMonth(result.getLocalDateTimeMonth().getMonth().toString());
      countSecureExchangeCreatedWithInstituteByMonth.setYear(String.valueOf(result.getLocalDateTimeMonth().getYear()));
      countSecureExchangeCreatedWithInstituteByMonth.setCount(result.getTotal());

      statsCreatedWithInstitutesList.add(countSecureExchangeCreatedWithInstituteByMonth);
    }

    return statsCreatedWithInstitutesList;
  }

  public Long countSchoolsWithActiveEdxUsersByPermissionCode(String permissionCode) {
    var listOfUsers = this.edxUserSchoolRepository.findSchoolsByPermission(permissionCode);
    return listOfUsers.stream().map(school -> school.getSchoolID().toString()).distinct().count();
  }

  public List<CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute> countSecureExchangesCreatedWithInstituteTypeGroupedByInstitute(String instituteType) {
    var results = this.secureExchangeRequestRepository.countSecureExchangesCreatedWithInstituteTypeGroupedByInstitute(instituteType);

    List<CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute> statsCreatedWithInstitutesList = new ArrayList<>();

    if (instituteType.equals("SCHOOL")) {
      var schoolMap = this.restUtils.getSchoolMap();

      for (var result : results) {
        CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute statsCreatedWithInstitute = new CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute();
        statsCreatedWithInstitute.setDisplayName(schoolMap.get(result.getContactIdentifier().toString()).getDisplayName());
        statsCreatedWithInstitute.setCount(result.getTotal());

        statsCreatedWithInstitutesList.add(statsCreatedWithInstitute);
      }
    } else if (instituteType.equals("DISTRICT")) {
      var districtMap = this.restUtils.getDistrictMap();

      for (var result : results) {
        CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute statsCreatedWithInstitute = new CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute();
        statsCreatedWithInstitute.setDisplayName(districtMap.get(result.getContactIdentifier().toString()).getDisplayName());
        statsCreatedWithInstitute.setCount(result.getTotal());

        statsCreatedWithInstitutesList.add(statsCreatedWithInstitute);
      }
    }

    return statsCreatedWithInstitutesList;
  }

  public List<SchoolWithoutActiveSecureExchangeUser> schoolListWithoutActiveSecureExchangeUser() {
    List<EdxUserSchoolEntity> listOfUsers = this.edxUserSchoolRepository.findSchoolsByPermission("SECURE_EXCHANGE");
    List<String> listOfSchoolsWithExchangeUser = listOfUsers.stream().map(school -> school.getSchoolID().toString()).distinct().toList();

    List<School> schoolList = this.restUtils.getSchools();

    List<SchoolWithoutActiveSecureExchangeUser> schoolWithoutActiveSecureExchangeUserList = new ArrayList<>();
    for (var school : schoolList) {
      if (!listOfSchoolsWithExchangeUser.contains(school.getSchoolId())) {
        SchoolWithoutActiveSecureExchangeUser schoolWithoutActiveSecureExchangeUser = new SchoolWithoutActiveSecureExchangeUser();
        schoolWithoutActiveSecureExchangeUser.setSchoolId(school.getSchoolId());
        schoolWithoutActiveSecureExchangeUser.setSchoolName(school.getDisplayName());
        schoolWithoutActiveSecureExchangeUser.setMincode(school.getMincode());
        schoolWithoutActiveSecureExchangeUser.setSchoolCategory(school.getSchoolCategoryCode());
        schoolWithoutActiveSecureExchangeUser.setFacilityType(school.getFacilityTypeCode());

        schoolWithoutActiveSecureExchangeUserList.add(schoolWithoutActiveSecureExchangeUser);
      }
    }

    return schoolWithoutActiveSecureExchangeUserList;
  }

}
