package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.StatsType;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.GenderCode;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequest;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestStats;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestStatusCode;
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
public interface PenRequestEndpoint {

  /**
   * Retrieve pen request pen request.
   *
   * @param id the id
   * @return the pen request
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_REQUEST')")
  @GetMapping("/{id}")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  PenRequest retrievePenRequest(@PathVariable String id);

  /**
   * Find pen requests iterable.
   *
   * @param digitalID the digital id
   * @param status    the status
   * @param pen       the pen
   * @return the iterable
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_REQUEST')")
  @GetMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Tag(name = "findPenRequests", description = "This api method will accept all or individual parameters and search the DB. if any parameter is null then it will be not included in the query.")
  Iterable<PenRequest> findPenRequests(@RequestParam(name = "digitalID", required = false) String digitalID, @RequestParam(name = "status", required = false) String status, @RequestParam(name = "pen", required = false) String pen);

  /**
   * Create pen request pen request.
   *
   * @param penRequest the pen request
   * @return the pen request
   */
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PEN_REQUEST')")
  @PostMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "200", description = "OK")})
  @ResponseStatus(CREATED)
  @Transactional
  PenRequest createPenRequest(@Validated @RequestBody PenRequest penRequest);

  /**
   * Update pen request pen request.
   *
   * @param penRequest the pen request
   * @return the pen request
   */
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PEN_REQUEST')")
  @PutMapping
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  @Transactional
  PenRequest updatePenRequest(@Validated @RequestBody PenRequest penRequest);

  /**
   * Gets pen request status codes.
   *
   * @return the pen request status codes
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_REQUEST_STATUSES')")
  @GetMapping(URL.STATUSES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<PenRequestStatusCode> getPenRequestStatusCodes();

  /**
   * Gets gender codes.
   *
   * @return the gender codes
   */
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_REQUEST_CODES')")
  @GetMapping(URL.GENDER_CODES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<GenderCode> getGenderCodes();

  /**
   * Delete by id response entity.
   *
   * @param id the id
   * @return the response entity
   */
  @DeleteMapping("/{id}")
  @PreAuthorize("hasAuthority('SCOPE_DELETE_PEN_REQUEST')")
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
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_REQUEST')")
  @Transactional(readOnly = true)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  CompletableFuture<Page<PenRequest>> findAll(@RequestParam(name = "pageNumber", defaultValue = "0") Integer pageNumber,
                                              @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                              @RequestParam(name = "sort", defaultValue = "") String sortCriteriaJson,
                                              @RequestParam(name = "searchCriteriaList", required = false) String searchCriteriaListJson);

  @GetMapping(URL.STATS)
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_REQUEST_STATS')")
  @Transactional(readOnly = true)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "500", description = "INTERNAL SERVER ERROR.")})
  ResponseEntity<PenRequestStats> getStats(@RequestParam(name = "statsType") StatsType statsType);

}
