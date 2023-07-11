package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.EdxStatsEndpoint;
import ca.bc.gov.educ.api.edx.service.v1.EdxStatsService;
import ca.bc.gov.educ.api.edx.struct.v1.CountSecureExchangeCreatedWithInstituteByMonth;
import ca.bc.gov.educ.api.edx.struct.v1.CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute;
import ca.bc.gov.educ.api.edx.struct.v1.SchoolWithoutActiveSecureExchangeUser;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class EdxStatsController extends BaseController implements EdxStatsEndpoint {

  @Getter(AccessLevel.PRIVATE)
  private final EdxStatsService edxStatsService;

  @Autowired
  EdxStatsController(final EdxStatsService edxStatsService) {
    this.edxStatsService = edxStatsService;
  }

  @Override
  public List<CountSecureExchangeCreatedWithInstituteByMonth> countSecureExchangesCreatedWithInstitute(String instituteType, Integer months) {
    return this.edxStatsService.countSecureExchangesCreatedWithInstitute(instituteType.toUpperCase(), months);
  }

  @Override
  public Long countSchoolsWithActiveEdxUsersByPermissionCode(String permissionCode) {
    return this.edxStatsService.countSchoolsWithActiveEdxUsersByPermissionCode(permissionCode);
  }

  @Override
  public List<CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute> countSecureExchangesCreatedWithInstituteTypeGroupedByInstitute(String instituteType) {
    return this.edxStatsService.countSecureExchangesCreatedWithInstituteTypeGroupedByInstitute(instituteType.toUpperCase());
  }

  @Override
  public List<SchoolWithoutActiveSecureExchangeUser> schoolListWithoutActiveSecureExchangeUser() {
    return this.edxStatsService.schoolListWithoutActiveSecureExchangeUser();
  }
}
