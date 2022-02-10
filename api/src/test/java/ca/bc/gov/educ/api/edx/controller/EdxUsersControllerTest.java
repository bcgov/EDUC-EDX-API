package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxUsersController;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
