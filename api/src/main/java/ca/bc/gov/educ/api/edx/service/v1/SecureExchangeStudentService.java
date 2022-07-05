package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.*;
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
    private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;

    @Autowired
    public SecureExchangeStudentService(SecureExchangeRequestRepository secureExchangeRequestRepository, SecureExchangeRequestCommentRepository secureExchangeRequestCommentRepository, DocumentRepository documentRepository, SecureExchangeStatusCodeTableRepository secureExchangeStatusCodeTableRepo, SecureExchangeContactTypeCodeTableRepository secureExchangeContactTypeCodeTableRepository, SecureExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }

    public SecureExchange addStudentToExchange(UUID secureExchangeID, UUID studentID) throws EntityNotFoundException {
        // TODO check to see if student exists by id
        // get secure exchange
        SecureExchangeEntity secureExchange = this.exchangeService.retrieveSecureExchange(secureExchangeID);
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

    public void deleteStudentFromExchange(UUID secureExchangeID, UUID studentID) {
        // get secure exchange
        try {
            SecureExchangeEntity secureExchange = this.exchangeService.retrieveSecureExchange(secureExchangeID);
            Set<SecureExchangeStudentEntity> students = secureExchange.getSecureExchangeStudents();
            if(!students.isEmpty()){
                SecureExchangeStudentEntity student = students.stream()
                        .filter(s -> studentID.equals(s.getStudentId()))
                        .findAny()
                        .orElse(null);
                if(student != null){
                    students.remove(student);
                    this.exchangeService.updateSecureExchange(secureExchange);
                }
            }
        } catch (EntityNotFoundException e) {
            // ignore, they want deletion anyway
        }
    }

    public List<SecureExchangeStudent> getStudentIDsFromExchange(UUID secureExchangeID) throws EntityNotFoundException {
        SecureExchangeEntity secureExchangeEntity = this.exchangeService.retrieveSecureExchange(secureExchangeID);
        SecureExchange secureExchange = mapper.toStructure(secureExchangeEntity);
        return secureExchange.getStudentsList();
    }
}
