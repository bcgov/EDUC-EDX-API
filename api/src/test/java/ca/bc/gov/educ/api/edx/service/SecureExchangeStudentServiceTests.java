package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.exception.NotFoundException;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeStudentService;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SecureExchangeStudentServiceTests extends BaseSecureExchangeAPITest {

    @Autowired
    SecureExchangeStudentService secureExchangeStudentService;

    @Autowired
    SecureExchangeRequestRepository secureExchangeRequestRepository;
    private static final String LEGIT_STUDENT_ID = "ac339d70-7649-1a2e-8176-49fbef5e0059";


    @Test
    @Transactional
    public void testAddStudentToExchange() {
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(createSecureExchange());
        this.secureExchangeStudentService.addStudentToExchange(entity.getSecureExchangeID(), UUID.fromString(LEGIT_STUDENT_ID));
        entity = this.secureExchangeRequestRepository.findById(entity.getSecureExchangeID()).orElse(null);
        assertThat(entity.getSecureExchangeStudents()).hasSize(1);
    }

    @Test
    @Transactional
    public void testDeleteStudentFromExchange() {
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(
                addStudentToSecureExchangeEntity(createSecureExchange(), UUID.fromString(LEGIT_STUDENT_ID))
        );
        Set<SecureExchangeStudentEntity> students = entity.getSecureExchangeStudents();
        assertThat(students).hasSize(1);
        SecureExchangeStudentEntity student = students.stream()
                .filter(s -> s.getStudentId().equals(LEGIT_STUDENT_ID))
                .findAny()
                .orElse(null);
        this.secureExchangeStudentService.deleteStudentFromExchange(student.getSecureExchangeStudentId());
        entity = this.secureExchangeRequestRepository.findById(entity.getSecureExchangeID()).orElse(null);
        assertThat(entity.getSecureExchangeStudents()).hasSize(0);
    }

    @Test
    @Transactional
    public void testGetStudentIDsFromExchange(){
        SecureExchangeEntity entity = addStudentToSecureExchangeEntity(createSecureExchange(), UUID.fromString(LEGIT_STUDENT_ID));
        addStudentToSecureExchangeEntity(entity, UUID.randomUUID());
        entity = this.secureExchangeRequestRepository.save(entity);
        assertThat(this.secureExchangeStudentService.getStudentIDsFromExchange(entity.getSecureExchangeID())).hasSize(2);
    }

    private SecureExchangeEntity createSecureExchange(){
        return new SecureExchangeBuilder().withoutSecureExchangeID().build();
    }

    private SecureExchangeEntity addStudentToSecureExchangeEntity(SecureExchangeEntity secureExchange, UUID studentId) {
        if(secureExchange.getSecureExchangeStudents() == null){
            secureExchange.setSecureExchangeStudents(new HashSet<>());
        }
        SecureExchangeStudentEntity student = new SecureExchangeStudentEntity();
        student.setSecureExchangeEntity(secureExchange);
        student.setStudentId(studentId);
        student.setCreateUser(ApplicationProperties.CLIENT_ID);
        student.setCreateDate(LocalDateTime.now());
        secureExchange.getSecureExchangeStudents().add(student);
        return secureExchange;
    }

}
