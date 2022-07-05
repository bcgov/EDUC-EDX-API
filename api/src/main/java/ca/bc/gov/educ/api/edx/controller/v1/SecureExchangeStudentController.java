package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.endpoint.v1.SecureExchangeStudentEndpoint;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeStudentService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class SecureExchangeStudentController implements SecureExchangeStudentEndpoint {

    @Getter(AccessLevel.PRIVATE)
    private final SecureExchangeStudentService studentService;
    private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;

    @Autowired
    public SecureExchangeStudentController(SecureExchangeStudentService studentService) {
        this.studentService = studentService;
    }

    @Override
    public SecureExchange addStudent(String studentId, String secureExchangeID) {
        return mapper.toStructure(studentService.addStudentToExchange(UUID.fromString(secureExchangeID), UUID.fromString(studentId)));
    }

    @Override
    public void deleteStudent(String studentId, String secureExchangeID) {
        studentService.deleteStudentFromExchange(UUID.fromString(secureExchangeID), UUID.fromString(studentId));
    }

    @Override
    public List<SecureExchangeStudent> getStudents(String secureExchangeID) {
        return studentService.getStudentIDsFromExchange(UUID.fromString(secureExchangeID));
    }
}
