package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.endpoint.v1.SecureExchangeStudentEndpoint;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.exception.NotFoundException;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeStudentService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class SecureExchangeStudentController implements SecureExchangeStudentEndpoint {

    @Getter(AccessLevel.PRIVATE)
    private final SecureExchangeStudentService studentService;

    @Autowired
    public SecureExchangeStudentController(SecureExchangeStudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public SecureExchange addStudent(String secureExchangeId, SecureExchangeStudent secureExchangeStudent) throws NotFoundException, EntityNotFoundException {
        return this.getStudentService().addStudentToExchange(UUID.fromString(secureExchangeId), secureExchangeStudent);
    }

    @Override
    public List<SecureExchangeStudent> getStudents(String secureExchangeId) {
        return this.getStudentService().getStudentIDsFromExchange(UUID.fromString(secureExchangeId));
    }

    @Override
    public ResponseEntity<Void> deleteStudent(String secureExchangeStudentId, String secureExchangeId) {
        this.getStudentService().deleteStudentFromExchange(UUID.fromString(secureExchangeStudentId));
        return ResponseEntity.noContent().build();
    }

}
