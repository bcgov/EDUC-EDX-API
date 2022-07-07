package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeStudentRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@Slf4j
public class SecureExchangeStudentService {

    @Getter(AccessLevel.PRIVATE)
    private final SecureExchangeService exchangeService;

    private RESTService restService;
    private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
    private final SecureExchangeStudentRepository repository;

    @Autowired
    public SecureExchangeStudentService(SecureExchangeService exchangeService, RESTService restService, SecureExchangeStudentRepository repository) {
        this.exchangeService = exchangeService;
        this.restService = restService;
        this.repository = repository;
    }

    public SecureExchange addStudentToExchange(UUID secureExchangeID, UUID studentID)  {
        // not found exception handler will fire if student not found
        restService.get("https://student-api-75e61b-dev.apps.silver.devops.gov.bc.ca/api/v1/student/" + studentID, String.class);
        // get secure exchange
        // entity not found will fire if not found
        SecureExchangeEntity secureExchange = this.exchangeService.retrieveSecureExchange(secureExchangeID);
        // TODO check repo for exchange student with studentID and secureExchangeID

        if(secureExchange.getSecureExchangeStudents() == null){
            secureExchange.setSecureExchangeStudents(new HashSet<>());
        }
        SecureExchangeStudentEntity student = secureExchange.getSecureExchangeStudents()
                .stream()
                .filter(s -> studentID.equals(s.getStudentId()))
                .findAny()
                .orElse(null);
        if(student != null){
            // do nothing, student exists
        } else {
            student = new SecureExchangeStudentEntity();
            student.setSecureExchangeEntity(secureExchange);
            student.setStudentId(studentID);
            student.setCreateUser(ApplicationProperties.CLIENT_ID);
            student.setCreateDate(LocalDateTime.now());
            secureExchange.getSecureExchangeStudents().add(student);
            this.exchangeService.updateSecureExchange(secureExchange);
        }
        return mapper.toStructure(secureExchange);
    }

    public void deleteStudentFromExchange(UUID secureExchangeStudentId) {
        if (repository.existsById(secureExchangeStudentId)) {
            repository.deleteById(secureExchangeStudentId);
        }
    }

    public List<SecureExchangeStudent> getStudentIDsFromExchange(UUID secureExchangeID) throws EntityNotFoundException {
        SecureExchangeEntity secureExchangeEntity = this.exchangeService.retrieveSecureExchange(secureExchangeID);
        SecureExchange secureExchange = mapper.toStructure(secureExchangeEntity);
        return secureExchange.getStudentsList();
    }
}
