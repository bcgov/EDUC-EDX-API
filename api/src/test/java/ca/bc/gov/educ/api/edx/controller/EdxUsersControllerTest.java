package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.config.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxUsersController;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeController;
import ca.bc.gov.educ.api.edx.filter.FilterOperation;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeStatusCodeTableRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SearchCriteria;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.ValueType;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EdxUsersControllerTest extends BaseSecureExchangeControllerTest {

  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  EdxUsersController controller;

  @Autowired
  MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void after() {
    this.ministryOwnershipTeamRepository.deleteAll();
  }

  @Test
  public void testRetrieveSecureExchange_GivenValidID_ShouldReturnOkStatus() throws Exception {
    var entity = this.ministryOwnershipTeamRepository.save(getMinistryOwnershipTeam());
    this.mockMvc.perform(get(URL.BASE_URL_USERS + URL.MINISTRY_TEAMS)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_MINISTRY_TEAMS"))))
            .andDo(print()).andExpect(status().isOk());
  }

  private MinistryOwnershipTeamEntity getMinistryOwnershipTeam(){
    MinistryOwnershipTeamEntity entity = new MinistryOwnershipTeamEntity();
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    entity.setUpdateUser("JACK");
    entity.setCreateUser("JACK");
    entity.setUpdateDate(LocalDateTime.now());
    entity.setTeamName("JOHN");
    entity.setGroupRoleIdentifier("ABC");
    return entity;
  }


}
