package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComments;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.CREATED;


@RequestMapping(URL.BASE_URL)
public interface SecureExchangeCommentEndpoint {

  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
  @GetMapping(URL.SECURE_EXCHANGE_ID_COMMENTS)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  @Transactional
  List<SecureExchangeComments> retrieveComments(@PathVariable String secureExchangeId);

  @PreAuthorize("hasAuthority('SCOPE_WRITE_SECURE_EXCHANGE')")
  @PostMapping(URL.SECURE_EXCHANGE_ID_COMMENTS)
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  @Transactional
  SecureExchangeComments save(@PathVariable String secureExchangeId, @Validated @RequestBody SecureExchangeComments secureExchangeComments);


}
