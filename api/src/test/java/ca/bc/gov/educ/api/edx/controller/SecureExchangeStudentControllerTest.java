package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeStudentController;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeStudentService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import static org.hamcrest.MatcherAssert.assertThat;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecureExchangeStudentControllerTest extends BaseSecureExchangeControllerTest {

    private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private SecureExchangeStudentController secureExchangeStudentController;
    @Autowired
    SecureExchangeRequestRepository secureExchangeRequestRepository;
    @Autowired
    SecureExchangeStudentService secureExchangeStudentService;
    private static final String LEGIT_STUDENT_ID = "ac339d70-7649-1a2e-8176-49fbef5e0059";

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @After
    public void after() {
        this.secureExchangeRequestRepository.deleteAll();
    }


    @Test
    public void testAddSecureExchangeStudents_GivenInvalidStudentID_ShouldReturnStatusNotFound() throws Exception {
        final SecureExchangeEntity entity = createSecureExchangeWithStudents(null);
        final String sid = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE+"/"+URL.SECURE_EXCHANGE_ID_STUDENTS+"/"+UUID.randomUUID(), sid)
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    public void testAddExchangeStudents_GivenInvalidExchangeID_ShouldReturnStatusNotFound() throws Exception {
        this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE+"/"+URL.SECURE_EXCHANGE_ID_STUDENTS+"/"+LEGIT_STUDENT_ID, UUID.randomUUID())
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isNotFound());
    }

    @Test
    public void testAddSecureExchangeStudents_ShouldReturnStatusOKWithUpdatedExchangeObject() throws Exception {
        final SecureExchangeEntity entity = createSecureExchangeWithStudents(null);
        final String sid = entity.getSecureExchangeID().toString();
        final String jsonPath = "$.studentsList[?(@.studentId=='" + LEGIT_STUDENT_ID + "')].studentId";
        this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE+"/"+URL.SECURE_EXCHANGE_ID_STUDENTS+"/"+LEGIT_STUDENT_ID, sid)
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE"))))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andExpect(
                                MockMvcResultMatchers.jsonPath(jsonPath)
                                        .value(LEGIT_STUDENT_ID));
    }

    @Test
    @Transactional
    public void testDeleteSecureExchangeStudents_ShouldReturnStatusOK_AndShouldDeleteFromExchange() throws Exception {
        SecureExchangeEntity entity = createSecureExchangeWithStudents(Arrays.asList(LEGIT_STUDENT_ID));
        final String sid = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(delete(URL.BASE_URL_SECURE_EXCHANGE+"/"+URL.SECURE_EXCHANGE_ID_STUDENTS+"/"+LEGIT_STUDENT_ID, sid)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE"))))
                .andDo(print())
                .andExpect(status().isOk());
        entity = this.secureExchangeRequestRepository.findById(UUID.fromString(sid)).orElse(null);
        if(entity == null){
            assertThat("Could not retrieve entity", false);
        }
        assertThat(entity.getSecureExchangeStudents().size(), equalTo(0));
    }

    public void testGetStudentsFromExchange_shouldReceiveStatusOK_withListOfStudents() throws Exception {
        final SecureExchangeEntity entity = createSecureExchangeWithStudents(Arrays.asList(LEGIT_STUDENT_ID, UUID.randomUUID().toString()));
        final String sid = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/"+URL.SECURE_EXCHANGE_ID_STUDENTS, sid)
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$", hasSize(2)));
    }

    @Test
    public void testGetStudentsFromExchange_GivenInvalidExchangeID_ShouldReturnNotFound() throws Exception {
        this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/"+URL.SECURE_EXCHANGE_ID_STUDENTS, UUID.randomUUID())
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isNotFound());
    }

    /**
     * Convenience method for creating an exchange entity with n students
     */
    private SecureExchangeEntity createSecureExchangeWithStudents(List<String> studentIDs){
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        if(studentIDs != null && studentIDs.size() > 0){
            entity.setSecureExchangeStudents(new HashSet<>());
            for(String s : studentIDs) {
                SecureExchangeStudentEntity student = new SecureExchangeStudentEntity();
                student.setSecureExchangeEntity(entity);
                student.setStudentId(UUID.fromString(s));
                student.setCreateUser(ApplicationProperties.CLIENT_ID);
                student.setCreateDate(LocalDateTime.now());
                entity.getSecureExchangeStudents().add(student);
            }
            this.secureExchangeRequestRepository.save(entity);
        }
        return entity;
    }


}
