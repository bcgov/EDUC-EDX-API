package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
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
   * Retrieve user school ID by permission name.
   *
   * @return list of user school codes
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping(URL.USER_SCHOOLS)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<String> findAllEdxUserSchoolIDs(@RequestParam(name = "permissionCode", required = false) String permissionCode);

  /**
   * Retrieves user district ID by permission code
   * @param permissionCode
   * @return List of allowed district ids
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping(URL.USER_DISTRICTS)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<String> findAllEdxUserDistrictIDs(@RequestParam(name = "permissionCode", required = false) String permissionCode);

  /**
   * Retrieve edx user.
   *
   * @return the edx user
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping("/{id}")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  EdxUser retrieveEdxUser(@PathVariable String id);


  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping(URL.INVITATIONS)
  @Transactional(readOnly = true)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<EdxActivationCode> findAllInvitations(@RequestParam(name = "instituteType") String instituteType);

  /**
   *   This api method will accept all or individual parameters and search the DB. if any parameter is null then it will be not included in the query.
   *
   * @return  List of Edx User
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping
  @Transactional(readOnly = true)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<EdxUser> findEdxUsers(@RequestParam(name = "digitalId", required = false) Optional<UUID> digitalId, @RequestParam(name = "schoolID", required = false) Optional<UUID> schoolID, @RequestParam(name = "firstName", required = false) String firstName, @RequestParam(name = "lastName", required = false) String lastName, @RequestParam(name = "districtID", required = false) Optional<UUID> districtID);

  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping("/districtSchools/{districtID}")
  @Transactional(readOnly = true)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<EdxSchool> findAllDistrictEdxUsers(@PathVariable String districtID);

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
  EdxUserSchool createEdxUserSchool(@PathVariable UUID id, @Validated @RequestBody  EdxUserSchool edxUserSchool);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_EDX_USER_SCHOOL')")
  @PutMapping("/{id}/school")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  EdxUserSchool updateEdxUserSchool(@PathVariable UUID id, @Validated @RequestBody  EdxUserSchool edxUserSchool);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_DELETE_EDX_USER_SCHOOL')")
  @DeleteMapping("/{id}/school/{edxUserSchoolId}")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> deleteEdxUserSchoolById(@PathVariable UUID id, @PathVariable UUID edxUserSchoolId );

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
  ResponseEntity<Void> deleteEdxUserSchoolRoleById(@PathVariable UUID id, @PathVariable UUID edxSchoolUserRoleId);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_READ_EDX_USERS')")
  @GetMapping("/roles")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<EdxRole> findAllEdxRoles(@RequestParam(name="instituteType", required = false) InstituteTypeCode instituteType);


  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_ACTIVATE_EDX_USER')")
  @PostMapping("/activation")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxUser activateUser(@Validated @RequestBody EdxActivateUser edxActivateUser);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_ACTIVATION_CODE')")
  @PostMapping("/activation-code/url")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(OK)
  InstituteTypeCode updateIsUrlClicked(@RequestBody EdxActivationCode edxActivationCode);


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

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_DELETE_ACTIVATION_CODE')")
  @DeleteMapping("/activation-code/user/{edxUserId}")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "NO CONTENT"),
      @ApiResponse(responseCode = "404", description = "NOT FOUND."),
      @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")
  })
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> deleteActivationCodesByUserId(@PathVariable UUID edxUserId);

  @PreAuthorize("hasAuthority('SCOPE_READ_PRIMARY_ACTIVATION_CODE')")
  @GetMapping("/activation-code/primary/{instituteType}/{instituteIdentifier}")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  EdxActivationCode findPrimaryEdxActivationCode(@PathVariable InstituteTypeCode instituteType, @PathVariable String instituteIdentifier);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PRIMARY_ACTIVATION_CODE')")
  @PostMapping("/activation-code/primary/{instituteType}/{instituteIdentifier}")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxActivationCode generateOrRegeneratePrimaryEdxActivationCode(@PathVariable InstituteTypeCode instituteType, @PathVariable String instituteIdentifier, @RequestBody EdxPrimaryActivationCode edxPrimaryActivationCode);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_EDX_USER_DISTRICT')")
  @PostMapping("/{id}/district")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxUserDistrict createEdxDistrictUser(@PathVariable UUID id, @Validated @RequestBody  EdxUserDistrict edxUserDistrict);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_EDX_USER_DISTRICT')")
  @PutMapping("/{id}/district")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  EdxUserDistrict updateEdxUserDistrict(@PathVariable UUID id, @Validated @RequestBody  EdxUserDistrict edxUserDistrict);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_DELETE_EDX_USER_DISTRICT')")
  @DeleteMapping("/{id}/district/{edxUserDistrictID}")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> deleteEdxDistrictUserByID(@PathVariable UUID id, @PathVariable UUID edxUserDistrictID );

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_WRITE_EDX_USER_DISTRICT_ROLE')")
  @PostMapping("{id}/district/{edxUserDistrictID}/role")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  EdxUserDistrictRole createEdxDistrictUserRole(@PathVariable UUID id, @PathVariable UUID edxUserDistrictID, @Validated @RequestBody EdxUserDistrictRole edxUserDistrictRole);

  @Transactional
  @PreAuthorize("hasAuthority('SCOPE_DELETE_EDX_USER_DISTRICT_ROLE')")
  @DeleteMapping("{id}/district/role/{edxUserDistrictRoleID}")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @ResponseStatus(NO_CONTENT)
  ResponseEntity<Void> deleteEdxDistrictUserRoleByID(@PathVariable UUID id,@PathVariable UUID edxUserDistrictRoleID);
}
