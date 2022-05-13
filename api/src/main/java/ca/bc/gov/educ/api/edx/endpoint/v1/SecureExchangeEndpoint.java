package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeContactTypeCode;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStatusCode;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.springframework.http.HttpStatus.CREATED;

/**
 * The interface secure exchange endpoint.
 */
@RequestMapping(URL.BASE_URL_SECURE_EXCHANGE)
@OpenAPIDefinition(info = @Info(title = "API for secure exchange.", description = "This CRUD API is for secure exchanges.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_SECURE_EXCHANGE", "WRITE_SECURE_EXCHANGE"})})
public interface SecureExchangeEndpoint {

  /**
   * Retrieve secure exchange request.
   *
   * @return the secure exchange
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
  @GetMapping("/{id}")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  SecureExchange retrieveSecureExchange(@PathVariable String id);

  /**
   * Find secure exchanges iterable.
   *
   * @return the iterable
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
  @GetMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Tag(name = "findSecureExchanges", description = "This api method will accept all or individual parameters and search the DB. if any parameter is null then it will be not included in the query.")
  List<SecureExchange> findSecureExchanges(@RequestParam(name = "contactIdentifier", required = false) String contactIdentifier, @RequestParam(name = "secureExchangeContactTypeCode", required = false) String secureExchangeContactTypeCode);

  /**
   * Create secure exchange request.
   *
   * @param secureExchange Secure Exchange
   * @return the Secure Exchange request
   */
  @PreAuthorize("hasAuthority('SCOPE_WRITE_SECURE_EXCHANGE')")
  @PostMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "200", description = "OK")})
  @ResponseStatus(CREATED)
  @Transactional
  SecureExchange createSecureExchange(@Validated @RequestBody SecureExchange secureExchange);

  /**
   * Update Secure Exchange request.
   *
   * @param secureExchange the secure exchange
   * @return the secure Exchange request
   */
  @PreAuthorize("hasAuthority('SCOPE_WRITE_SECURE_EXCHANGE')")
  @PutMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Transactional
  SecureExchange updateSecureExchange(@Validated @RequestBody SecureExchange secureExchange);

  /**
   * Gets secure Exchange status codes.
   *
   * @return the secure Exchange status codes
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE_CODES')")
  @GetMapping(URL.STATUSES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<SecureExchangeStatusCode> getSecureExchangeStatusCodes();

  /**
   * Gets secure Exchange contact type codes.
   *
   * @return the secure Exchange contact type codes
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE_CODES')")
  @GetMapping(URL.CONTACT_TYPE)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<SecureExchangeContactTypeCode> getSecureExchangeContactTypeCodes();

  /**
   * Delete by id response entity.
   *
   * @param id the id
   * @return the response entity
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('SCOPE_DELETE_SECURE_EXCHANGE')")
  @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "NO CONTENT"), @ApiResponse(responseCode = "404", description = "NOT FOUND."), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  ResponseEntity<Void> deleteById(@PathVariable UUID id);

  /**
   * Claim all secure exchanges provided
   *
   * @param secureExchangeIDs the secure exchange IDs
   * @return the response entity
   */
  @PostMapping(URL.CLAIM_ALL)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_SECURE_EXCHANGE')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  @Tag(name = "Endpoint to claim all secure exchanges provided by ID.", description = "Endpoint to claim all secure exchanges provided by ID.")
  ResponseEntity<List<SecureExchange>> claimAllSecureExchanges(@RequestParam List<UUID> secureExchangeIDs, @RequestParam String reviewer);

  /**
   * Find all completable future.
   *
   * @param pageNumber             the page number
   * @param pageSize               the page size
   * @param sortCriteriaJson       the sort criteria json
   * @param searchCriteriaListJson the search criteria list json
   * @return the completable future
   */
  @GetMapping(URL.PAGINATED)
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
  @Transactional(readOnly = true)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  CompletableFuture<Page<SecureExchange>> findAll(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                                  @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                  @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                                  @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson);

}
