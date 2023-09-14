package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeStudentService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

class SecureExchangeStudentServiceAddTests extends BaseSecureExchangeAPITest {

    private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
    @MockBean
    SecureExchangeStudentService secureExchangeStudentServiceMock;
    @Autowired
    SecureExchangeRequestRepository secureExchangeRequestRepository;
    private static final String LEGIT_STUDENT_ID = "ac339d70-7649-1a2e-8176-49fbef5e0059";


    @Test
    @Transactional
    public void testAddStudentToExchange() throws Exception {
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(createSecureExchange());
        SecureExchange secureExchange = createSecureExchangeFromEntityWithStudent(LEGIT_STUDENT_ID, entity);
        doReturn(secureExchange).when(secureExchangeStudentServiceMock).addStudentToExchange(any(UUID.class), any(SecureExchangeStudent.class));
        this.secureExchangeStudentServiceMock.addStudentToExchange(entity.getSecureExchangeID(), createSecureExchangeStudent(LEGIT_STUDENT_ID));
        entity = this.secureExchangeRequestRepository.findById(entity.getSecureExchangeID()).orElse(null);
        assertThat(entity.getSecureExchangeStudents()).hasSize(1);
    }

    private SecureExchangeEntity createSecureExchange(){
        return new SecureExchangeBuilder().withoutSecureExchangeID().build();
    }

    private SecureExchangeStudent createSecureExchangeStudent(String studentId){
        SecureExchangeStudent student = new SecureExchangeStudent();
        student.setStudentId(studentId);
        return student;
    }

    private SecureExchange createSecureExchangeFromEntityWithStudent(String studentId, SecureExchangeEntity secureExchangeEntity){
        SecureExchangeStudentEntity secureExchangeStudentEntity = new SecureExchangeStudentEntity();
        secureExchangeStudentEntity.setStudentId(UUID.fromString(studentId));
        secureExchangeStudentEntity.setSecureExchangeEntity(secureExchangeEntity);
        secureExchangeStudentEntity.setSecureExchangeStudentId(UUID.randomUUID());
        secureExchangeStudentEntity.setCreateDate(LocalDateTime.now());
        secureExchangeStudentEntity.setCreateUser("test");
        secureExchangeEntity.setSecureExchangeStudents(new HashSet<>());
        secureExchangeEntity.getSecureExchangeStudents().add(secureExchangeStudentEntity);
        return mapper.toStructure(secureExchangeEntity);
    }

}
