package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationRelinkSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
   * @param edxUserActivationInviteSagaData
   * @return
   */
  @PostMapping("/school-user-activation-invite-saga")
  @PreAuthorize("hasAuthority('SCOPE_SCHOOL_USER_ACTIVATION_INVITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT.")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> edxSchoolUserActivationInvite(@Validated @RequestBody EdxUserActivationInviteSagaData edxUserActivationInviteSagaData);


  /**
   * Endpoint for creating creating new secure exchange and sending email notification to the edx_users
   * @param secureExchangeCreateSagaData
   * @return
   */
  @PostMapping("/new-secure-exchange-saga")
  @PreAuthorize("hasAuthority('SCOPE_CREATE_SECURE_EXCHANGE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> createNewSecureExchange(@Validated @RequestBody SecureExchangeCreateSagaData secureExchangeCreateSagaData);

  /**
   * End point for relinking personal activaton code and sending email invite.
   * @param edxUserActivationRelinkSagaData
   * @return
   */
  @PostMapping("/school-user-activation-relink-saga")
  @PreAuthorize("hasAuthority('SCOPE_SCHOOL_USER_ACTIVATION_INVITE_SAGA')")
  @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "ACCEPTED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST."), @ApiResponse(responseCode = "409", description = "CONFLICT.")})
  @ResponseStatus(ACCEPTED)
  ResponseEntity<String> edxSchoolUserActivationRelink(@Validated @RequestBody EdxUserActivationRelinkSagaData edxUserActivationRelinkSagaData);

}
