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

@RequestMapping(URL.BASE_URL)
@Tag(name = "API for Pen Request Documents.", description = "This API is for Pen Request Documents.")
public interface PenReqDocumentEndpoint {

  @GetMapping(URL.PEN_REQUEST_ID_DOCUMENTS + URL.DOCUMENT_ID)
  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  PenReqDocument readDocument(@PathVariable String penRequestID, @PathVariable String documentID, @RequestParam(value = "includeDocData", defaultValue = "Y") String includeDocData);

  @PostMapping(URL.PEN_REQUEST_ID_DOCUMENTS)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED"), @ApiResponse(responseCode = "200", description = "OK")})
  @ResponseStatus(CREATED)
  PenReqDocMetadata createDocument(@PathVariable String penRequestID, @Validated @RequestBody PenReqDocument penReqDocument);

  @PutMapping(URL.PEN_REQUEST_ID_DOCUMENTS + URL.DOCUMENT_ID)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  PenReqDocMetadata updateDocument(@PathVariable UUID penRequestID, @PathVariable UUID documentID, @Validated @RequestBody PenReqDocument penReqDocument);

  @DeleteMapping(URL.PEN_REQUEST_ID_DOCUMENTS + URL.DOCUMENT_ID)
  @PreAuthorize("hasAuthority('SCOPE_DELETE_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  PenReqDocMetadata deleteDocument(@PathVariable String penRequestID, @PathVariable String documentID);

  @GetMapping(URL.PEN_REQUEST_ID_DOCUMENTS)
  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  Iterable<PenReqDocMetadata> readAllDocumentMetadata(@PathVariable String penRequestID);

  @GetMapping(URL.FILE_REQUIREMENTS)
  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT_REQUIREMENTS')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  PenReqDocRequirement getDocumentRequirements();

  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT_TYPES')")
  @GetMapping(URL.DOCUMENT_TYPES)
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  Iterable<PenReqDocTypeCode> getDocumentTypeCodes();

  @GetMapping(URL.ALL_DOCUMENTS)
  @PreAuthorize("hasAuthority('SCOPE_READ_DOCUMENT')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<PenReqDocumentMetadata> readAllDocumentsMetadata();
}
