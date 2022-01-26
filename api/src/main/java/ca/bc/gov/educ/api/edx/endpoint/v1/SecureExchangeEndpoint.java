package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
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
 * The interface Pen request endpoint.
 */
@RequestMapping(URL.BASE_URL)
@OpenAPIDefinition(info = @Info(title = "API for Pen Requests.", description = "This CRUD API is for Pen Requests tied to a Digital ID for a particular student in BC.", version = "1"), security = {@SecurityRequirement(name = "OAUTH2", scopes = {"READ_PEN_REQUEST", "WRITE_PEN_REQUEST"})})
public interface SecureExchangeEndpoint {

  /**
   * Retrieve secure exchange request.
   *
   * @param secure_exchange_id the id
   * @return the secure exchange
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
  @GetMapping("/{id}")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  SecureExchange retrieveSecureExchange(@PathVariable String secure_exchange_id);

  /**
   * Find secure exchanges iterable.
   *
   * @param digitalID the digital id
   * @param status    the status
   * @return the iterable
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
  @GetMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Tag(name = "findSecureExchanges", description = "This api method will accept all or individual parameters and search the DB. if any parameter is null then it will be not included in the query.")
  List<SecureExchange> findSecureExchanges(@RequestParam(name = "digitalID", required = false) String digitalID, @RequestParam(name = "status", required = false) String status);

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
   * @param secureExchange the pen request
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
  @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE_STATUSES')")
  @GetMapping(URL.STATUSES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<SecureExchangeStatusCode> getSecureExchangeStatusCodes();

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
