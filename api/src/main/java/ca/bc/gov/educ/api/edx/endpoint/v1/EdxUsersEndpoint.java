package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.*;

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
   *   This api method will accept all or individual parameters and search the DB. if any parameter is null then it will be not included in the query.
   *   If we search via mincode we will only return user's roles at that specific mincode. Districts and other schools are filtered out.
   *
   * @return  List of Edx User
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<EdxUser> findEdxUsers(@RequestParam(name = "digitalId", required = false) Optional<UUID> digitalId, @RequestParam(name = "mincode", required = false) String mincode, @RequestParam(name = "firstName", required = false) String firstName, @RequestParam(name = "lastName", required = false) String lastName);


  @PreAuthorize("hasAuthority('SCOPE_WRITE_EDX_USER')")
  @PostMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxUser createEdxUser(@Validated @RequestBody  EdxUser edxUser);

  @PreAuthorize("hasAuthority('SCOPE_DELETE_EDX_USER')")
  @DeleteMapping("/{id}")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> deleteEdxUserById(@PathVariable UUID id);


  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_EDX_USER_SCHOOL')")
  @PostMapping("/{id}/school")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxUserSchool createEdxSchoolUser(@PathVariable UUID id, @Validated @RequestBody  EdxUserSchool edxUserSchool);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_DELETE_EDX_USERS_SCHOOL')")
  @DeleteMapping("/{id}/school/{edxUserSchoolId}")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> deleteEdxSchoolUserById(@PathVariable UUID id, @PathVariable UUID edxUserSchoolId );

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_EDX_USER_SCHOOL_ROLE')")
  @PostMapping("{id}/school/{edxUserSchoolId}/role")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxUserSchoolRole createEdxSchoolUserRole(@PathVariable UUID id, @PathVariable UUID edxUserSchoolId, @Validated @RequestBody EdxUserSchoolRole edxUserSchoolRole);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_DELETE_EDX_USER_SCHOOL_ROLE')")
  @DeleteMapping("{id}/school/role/{edxSchoolUserRoleId}")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> deleteEdxSchoolUserRoleById(@PathVariable UUID id,@PathVariable UUID edxSchoolUserRoleId);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping("/roles")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<EdxRole> findAllEdxRoles();


  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_ACTIVATE_EDX_USER')")
  @PostMapping("/activation")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxUser activateUser(@Validated @RequestBody  EdxActivateUser edxActivateUser);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_ACTIVATION_CODE')")
  @PostMapping("/activation-code/url")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(OK)
  ResponseEntity<Void> updateIsUrlClicked(@RequestBody EdxActivationCode edxActivationCode);


  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_ACTIVATION_CODE')")
  @PostMapping("/activation-code")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxActivationCode createActivationCode(@RequestBody EdxActivationCode edxActivationCode);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_DELETE_ACTIVATION_CODE')")
  @DeleteMapping("/activation-code/{activationCodeId}")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> deleteActivationCode(@PathVariable UUID activationCodeId);

  @PreAuthorize("hasAuthority('SCOPE_READ_PRIMARY_ACTIVATION_CODE')")
  @GetMapping("/activation-code/primary/{mincode}")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  EdxActivationCode findPrimaryEdxActivationCode(@PathVariable String mincode);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PRIMARY_ACTIVATION_CODE')")
  @PutMapping("/activation-code/primary/{mincode}")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(OK)
  EdxActivationCode generateOrRegeneratePrimaryEdxActivationCode(@PathVariable String mincode);

}
