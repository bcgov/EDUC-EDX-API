package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeStudentController;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        final SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(mapper.toModel(this.getSecureExchangeEntityFromJsonString()));
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
    public void testAddSecureExchangeStudents_GivenInvalidExchangeID_ShouldReturnStatusNotFound() throws Exception {
        this.mockMvc.perform(put(URL.BASE_URL_SECURE_EXCHANGE+"/"+URL.SECURE_EXCHANGE_ID_STUDENTS+"/"+LEGIT_STUDENT_ID, UUID.randomUUID())
                        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isNotFound());
    }





}
