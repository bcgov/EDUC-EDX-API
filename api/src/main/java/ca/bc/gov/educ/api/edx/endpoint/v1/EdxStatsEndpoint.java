package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.CountSecureExchangeCreatedWithInstituteByMonth;
import ca.bc.gov.educ.api.edx.struct.v1.CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute;
import ca.bc.gov.educ.api.edx.struct.v1.SchoolWithoutActiveSecureExchangeUser;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * The interface EDX Users.
 */
@RequestMapping(URL.BASE_URL_SECURE_EXCHANGE)
@OpenAPIDefinition(info = @Info(title = "API for EDX statistics.", description = "This API is for EDX statistics.", version = "1"))
public interface EdxStatsEndpoint {

  /**
   * End point for obtaining count of secure exchanges created with schools or districts for the last number of months.
   *
   * @param instituteType can be either SCHOOL or DISTRICT.
   * @param months number of months to search back from.
   * @return statistics for number of secure messages created to a school or district
   */
  @GetMapping("/stats/count-secure-exchanges-created-with-institute")
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Transactional(readOnly = true)
  List<CountSecureExchangeCreatedWithInstituteByMonth> countSecureExchangesCreatedWithInstitute(@Validated @RequestParam String instituteType, @Validated @RequestParam Integer months);

  @GetMapping("/stats/count-schools-with-active-edx-users")
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST.")})
  @Transactional(readOnly = true)
  Long countSchoolsWithActiveEdxUsersByPermissionCode(@Validated @RequestParam String permissionCode);

  @GetMapping("/stats/count-secure-exchanges-by-institute")
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Transactional(readOnly = true)
  List<CountSecureExchangesCreatedWithInstituteTypeGroupedByInstitute> countSecureExchangesCreatedWithInstituteTypeGroupedByInstitute(@Validated @RequestParam String instituteType);

  @GetMapping("/stats/school-list-without-active-edx-users")
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "503", description = "SERVICE UNAVAILABLE.")})
  @Transactional(readOnly = true)
  List<SchoolWithoutActiveSecureExchangeUser> schoolListWithoutActiveSecureExchangeUser();
}
