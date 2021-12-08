package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestMacro;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RequestMapping(URL.BASE_URL+URL.PEN_REQUEST_MACRO)
public interface PenRequestMacroEndpoint {

  @GetMapping
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_REQ_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
  List<PenRequestMacro> findPenReqMacros(@RequestParam(value = "macroTypeCode", required = false) String macroTypeCode);

  @GetMapping(URL.MACRO_ID)
  @PreAuthorize("hasAuthority('SCOPE_READ_PEN_REQ_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  PenRequestMacro findPenReqMacroById(@PathVariable UUID macroId);

  @PostMapping
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PEN_REQ_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "CREATED")})
  @ResponseStatus(CREATED)
  PenRequestMacro createPenReqMacro(@Validated @RequestBody PenRequestMacro penRequestMacro);

  @PutMapping(URL.MACRO_ID)
  @PreAuthorize("hasAuthority('SCOPE_WRITE_PEN_REQ_MACRO')")
  @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
  PenRequestMacro updatePenReqMacro(@PathVariable UUID macroId, @Validated @RequestBody PenRequestMacro penRequestMacro);
}
