package ca.bc.gov.educ.api.edx.endpoint.v1;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping(URL.BASE_URL_SECURE_EXCHANGE)
public interface SecureExchangeStudentEndpoint {

    @PreAuthorize("hasAuthority('SCOPE_WRITE_SECURE_EXCHANGE')")
    @PutMapping(URL.SECURE_EXCHANGE_ID_STUDENTS + "/{studentId}")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    @Transactional
    void addStudent(@PathVariable String studentId, @PathVariable String secureExchangeID);

    @PreAuthorize("hasAuthority('SCOPE_WRITE_SECURE_EXCHANGE')")
    @DeleteMapping(URL.SECURE_EXCHANGE_ID_STUDENTS + "/{studentId}")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK")})
    @Transactional
    void deleteStudent(@PathVariable String studentId, @PathVariable String secureExchangeID);

    @PreAuthorize("hasAuthority('SCOPE_READ_SECURE_EXCHANGE')")
    @GetMapping(URL.SECURE_EXCHANGE_ID_STUDENTS )
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "OK"), @ApiResponse(responseCode = "404", description = "NOT FOUND")})
    @Transactional
    List<SecureExchangeStudent> getStudents(@PathVariable String secureExchangeID);

}
