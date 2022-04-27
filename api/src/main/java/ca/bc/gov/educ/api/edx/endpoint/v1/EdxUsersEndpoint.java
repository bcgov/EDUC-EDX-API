package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.MinistryTeam;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.UUID;

/**
 * The interface EDX Users.
 */
@RequestMapping(URL.BASE_URL_USERS)
@OpenAPIDefinition(info = @Info(title = "API for EDX user management.", description = "This CRUD API is for EDX user management.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_MINISTRY_TEAMS"})})
public interface EdxUsersEndpoint {

  /**
   * Retrieve ministry teams.
   *
   * @return list of ministry teams
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_MINISTRY_TEAMS')")
  @GetMapping(URL.MINISTRY_TEAMS)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<MinistryTeam> findAllMinistryTeams();

  /**
   * Retrieve user school mincodes by permission name.
   *
   * @return list of user school codes
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USER_SCHOOLS')")
  @GetMapping(URL.USER_SCHOOL_MINCODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<String> findAllEdxUserSchoolMincodes(@RequestParam(name = "permissionName") String permissionName);

  /**
   * Retrieve edx user.
   *
   * @return the edx user
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping("/{id}")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  EdxUser retrieveEdxUser(@PathVariable String id);


  /**
   *   There is scope for adding other Query Params to this endpoint. When those get added digitalId can be made required=false.
   * @return  List of Edx User
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<EdxUser> findEdxUsers(@RequestParam(name = "digitalId", required = true) UUID digitalId);

}
