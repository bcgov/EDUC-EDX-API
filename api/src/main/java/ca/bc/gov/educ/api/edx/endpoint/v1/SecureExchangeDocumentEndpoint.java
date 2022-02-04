package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping(URL.BASE_URL_SECURE_EXCHANGE)
@Tag(name = "API for secure exchange documents.", description = "This API is for secure exchange documents.")
public interface SecureExchangeDocumentEndpoint {

  @GetMapping(URL.SECURE_EXCHANGE_ID_DOCUMENTS + URL.DOCUMENT_ID)
  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  SecureExchangeDocument readDocument(@PathVariable String secureExchangeID, @PathVariable String documentID, @RequestParam(value = "includeDocData", defaultValue = "Y") String includeDocData);

  @PostMapping(URL.SECURE_EXCHANGE_ID_DOCUMENTS)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "200", description = "OK")})
  @ResponseStatus(CREATED)
  SecureExchangeDocMetadata createDocument(@PathVariable String secureExchangeID, @Validated @RequestBody SecureExchangeDocument secureExchangeDocument);

  @PutMapping(URL.SECURE_EXCHANGE_ID_DOCUMENTS + URL.DOCUMENT_ID)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  SecureExchangeDocMetadata updateDocument(@PathVariable UUID secureExchangeID, @PathVariable UUID documentID, @Validated @RequestBody SecureExchangeDocument secureExchangeDocument);

  @DeleteMapping(URL.SECURE_EXCHANGE_ID_DOCUMENTS + URL.DOCUMENT_ID)
  @PreAuthorize("hasAuthority('SCOPE_DELETE_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  SecureExchangeDocMetadata deleteDocument(@PathVariable String secureExchangeID, @PathVariable String documentID);

  @GetMapping(URL.SECURE_EXCHANGE_ID_DOCUMENTS)
  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  Iterable<SecureExchangeDocMetadata> readAllDocumentMetadata(@PathVariable String secureExchangeID);

  @GetMapping(URL.FILE_REQUIREMENTS)
  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT_REQUIREMENTS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  SecureExchangeDocRequirement getDocumentRequirements();

  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT_TYPES')")
  @GetMapping(URL.DOCUMENT_TYPES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  Iterable<SecureExchangeDocumentTypeCode> getDocumentTypeCodes();

  @GetMapping(URL.ALL_DOCUMENTS)
  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<SecureExchangeDocumentMetadata> readAllDocumentsMetadata();
}
