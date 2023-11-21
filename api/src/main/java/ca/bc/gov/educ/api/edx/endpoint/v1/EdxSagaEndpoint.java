package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.springframework.http.HttpStatus.ACCEPTED;

/**
 * The interface EDX Users.
 */
@RequestMapping(URL.BASE_URL_SECURE_EXCHANGE)
@OpenAPIDefinition(info = @Info(title = "API for EDX SAGA management.", description = "This API is for EDX SAGA management.", version = "1"))
public interface EdxSagaEndpoint {

  /**
   * End point for creating personal activaton code and sending email invite.
   *
   * @param edxUserActivationInviteSagaData the edx user activation invite saga data
   * @return response entity
   */
  @PostMapping("/school-user-activation-invite-saga")
  @PreAuthorize("hasAuthority('SCOPE_SCHOOL_USER_ACTIVATION_INVITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT.")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> edxSchoolUserActivationInvite(@Validated @RequestBody EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData);


  /**
   * Endpoint for creating creating new secure exchange and sending email notification to the edx_users
   *
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   * @return response entity
   */
  @PostMapping("/new-secure-exchange-saga")
  @PreAuthorize("hasAuthority('SCOPE_CREATE_SECURE_EXCHANGE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> createNewSecureExchange(@Validated @RequestBody SecureExchangeCreateSagaData secureExchangeCreateSagaData);

  /**
   * End point for relinking personal activaton code and sending email invite.
   *
   * @param edxUserActivationRelinkSagaData the edx user activation relink saga data
   * @return response entity
   */
  @PostMapping("/school-user-activation-relink-saga")
  @PreAuthorize("hasAuthority('SCOPE_SCHOOL_USER_ACTIVATION_INVITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT.")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> edxSchoolUserActivationRelink(@Validated @RequestBody EdxUserSchoolActivationRelinkSagaData edxUserActivationRelinkSagaData);

  /**
   * Create secure exchange comment response entity.
   *
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @return the response entity
   */
  @PostMapping("/secure-exchange-comment-saga")
  @PreAuthorize("hasAuthority('SCOPE_CREATE_SECURE_EXCHANGE_COMMENT_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> createSecureExchangeComment(@Validated @RequestBody SecureExchangeCommentSagaData secureExchangeCommentSagaData);


  @PostMapping("/district-user-activation-invite-saga")
  @PreAuthorize("hasAuthority('SCOPE_DISTRICT_USER_ACTIVATION_INVITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT.")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> edxDistrictUserActivationInvite(@Validated @RequestBody EdxUserDistrictActivationInviteSagaData edxDistrictUserActivationInviteSagaData);

  /**
   * End point for relinking personal activaton code and sending email invite.
   *
   * @param edxUserActivationRelinkSagaData the edx user activation relink saga data
   * @return response entity
   */
  @PostMapping("/district-user-activation-relink-saga")
  @PreAuthorize("hasAuthority('SCOPE_DISTRICT_USER_ACTIVATION_INVITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT.")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> edxDistrictUserActivationRelink(@Validated @RequestBody EdxUserDistrictActivationRelinkSagaData edxUserActivationRelinkSagaData);

  @PostMapping("/create-school-saga")
  @PreAuthorize("hasAuthority('SCOPE_CREATE_SCHOOL_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT.")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> createSchool(@Validated @RequestBody CreateSchoolSagaData edxSchoolCreationSagaData);

  @PostMapping("/move-school-saga")
  @PreAuthorize("hasAuthority('SCOPE_MOVE_SCHOOL_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT.")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> moveSchool(@Validated @RequestBody MoveSchoolData moveSchoolData);

  @PreAuthorize("hasAuthority('SCOPE_WRITE_ACTIVATION_CODE')")
  @PostMapping("onboarding-file")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @Transactional
  @Tag(name = "Endpoint to Upload an excel file and convert to json structure.", description = "Endpoint to upload an onboarding CSV file")
  @Schema(name = "OnboardingFileUpload", implementation = OnboardingFileUpload.class)
  OnboardingFileProcessResponse processOnboardingFile(@Validated @RequestBody OnboardingFileUpload fileUpload);
}
