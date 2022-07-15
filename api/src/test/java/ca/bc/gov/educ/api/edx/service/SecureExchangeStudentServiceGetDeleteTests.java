package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeStudentRepository;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeStudentService;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SecureExchangeStudentServiceGetDeleteTests extends BaseSecureExchangeAPITest {

    private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;

    @Autowired
    SecureExchangeStudentService secureExchangeStudentService;
    @Autowired
    SecureExchangeRequestRepository secureExchangeRequestRepository;
    @Autowired
    SecureExchangeStudentRepository secureExchangeStudentRepository;
    private static final String LEGIT_STUDENT_ID = "ac339d70-7649-1a2e-8176-49fbef5e0059";


    @Test
    @Transactional
    public void testDeleteStudentFromExchange() {
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(
                addStudentToSecureExchangeEntity(createSecureExchange(), UUID.fromString(LEGIT_STUDENT_ID))
        );
        Set<SecureExchangeStudentEntity> students = entity.getSecureExchangeStudents();
        assertThat(students).hasSize(1);
        SecureExchangeStudentEntity student = students.stream()
                .filter(s -> s.getStudentId().equals(UUID.fromString(LEGIT_STUDENT_ID)))
                .findAny()
                .orElse(null);
        if(student == null){
            fail("Student not found");
        }
        this.secureExchangeStudentService.deleteStudentFromExchange(student.getSecureExchangeStudentId());
        SecureExchangeStudentEntity studentThatShouldBeNull = secureExchangeStudentRepository.findById(student.getSecureExchangeStudentId()).orElse(null);
        MatcherAssert.assertThat(studentThatShouldBeNull, equalTo(null));
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
