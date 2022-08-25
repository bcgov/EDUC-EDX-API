package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeNote;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.CREATED;


@RequestMapping(URL.BASE_URL_SECURE_EXCHANGE)
public interface SecureExchangeNoteEndpoint {

  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE_NOTE')")
  @GetMapping(URL.SECURE_EXCHANGE_ID_NOTES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND"), @ApiResponse(responseCode = "204", description = "NO CONTENT")})
  @Transactional
  ResponseEntity<?> retrieveNotes(@PathVariable String secureExchangeId);

  @PreAuthorize("hasAuthority('SCOPE_WRITE_SECURE_EXCHANGE_NOTE')")
  @PostMapping(URL.SECURE_EXCHANGE_ID_NOTES)
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "400", description = "BAD REQUEST")})
  @ResponseStatus(CREATED)
  @Transactional
  SecureExchangeNote save(@PathVariable String secureExchangeId, @Validated @RequestBody SecureExchangeNote secureExchangeNote);

  @PreAuthorize("hasAuthority('SCOPE_DELETE_SECURE_EXCHANGE_NOTE')")
  @DeleteMapping(URL.SECURE_EXCHANGE_ID_NOTES + "/{secureExchangeNoteId}")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT")})
  @Transactional
  ResponseEntity<Void> deleteNote(@PathVariable String secureExchangeId, @PathVariable String secureExchangeNoteId);

}
