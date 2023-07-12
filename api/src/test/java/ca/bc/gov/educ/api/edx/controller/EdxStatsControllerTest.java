package ca.bc.gov.educ.api.edx.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.EdxStatsController;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

public class EdxStatsControllerTest extends BaseSecureExchangeControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired
  EdxStatsController edxStatsController;
  @Autowired
  RestUtils restUtils;
  @Autowired
  SecureExchangeRequestRepository secureExchangeRequestRepository;
  @Autowired
  EdxUserRepository edxUserRepository;
  @Autowired
  EdxPermissionRepository edxPermissionRepository;
  @Autowired
  EdxRoleRepository edxRoleRepository;
  @Autowired
  EdxUserSchoolRepository edxUserSchoolRepository;
  @Autowired
  EdxUserDistrictRepository edxUserDistrictRepository;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @After
  public void after() {
    this.secureExchangeRequestRepository.deleteAll();
  }

  @Test
  public void testCountSecureExchangesCreatedWithInstitute_GivenValidInstituteAndInterval_ShouldReturnCount() throws Exception{

    this.secureExchangeRequestRepository.save(createDummySecureExchangeEntity(UUID.randomUUID().toString(), "SCHOOL"));
    this.secureExchangeRequestRepository.save(createDummySecureExchangeEntity(UUID.randomUUID().toString(), "SCHOOL"));

    this.secureExchangeRequestRepository.save(createDummySecureExchangeEntity(UUID.randomUUID().toString(), "DISTRICT"));

    //count of exchanges for SCHOOLS
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/stats/count-secure-exchanges-created-with-institute")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE")))
            .param("instituteType", "SCHOOL"))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].month", is(LocalDateTime.now().getMonth().toString())))
        .andExpect(jsonPath("$.[0].year", is(String.valueOf(LocalDateTime.now().getYear()))))
        .andExpect(jsonPath("$.[0].count", is(2)));

    //count of exchanges for DISTRICTS
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/stats/count-secure-exchanges-created-with-institute")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE")))
            .param("instituteType", "DISTRICT"))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].month", is(LocalDateTime.now().getMonth().toString())))
        .andExpect(jsonPath("$.[0].year", is(String.valueOf(LocalDateTime.now().getYear()))))
        .andExpect(jsonPath("$.[0].count", is(1)));
  }

  @Test
  public void countSecureExchangesCreatedWithInstituteTypeGroupedByInstitute_GivenValidInstituteType_ShouldReturnCount() throws Exception {

    String schoolId = UUID.randomUUID().toString();

    Map<String, School> schoolMap = new ConcurrentHashMap<>();
    schoolMap.put(schoolId, createDummySchool(schoolId));

    Mockito.when(this.restUtils.getSchoolMap()).thenReturn(schoolMap);

    this.secureExchangeRequestRepository.save(createDummySecureExchangeEntity(schoolId, "SCHOOL"));
    this.secureExchangeRequestRepository.save(createDummySecureExchangeEntity(schoolId, "SCHOOL"));
    //should ignore the district messages
    this.secureExchangeRequestRepository.save(createDummySecureExchangeEntity(UUID.randomUUID().toString(), "DISTRICT"));

    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/stats/count-secure-exchanges-by-institute")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE")))
            .param("instituteType", "SCHOOL"))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].count", is(2)));;
  }

  @Test
  public void countSchoolsWithActiveEdxUsersByPermissionCode_GivenPermissionCode_ShouldReturnCount() throws Exception {
    this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/stats/count-schools-with-active-edx-users")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS")))
            .param("permissionCode", "Exchange"))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$", is(1)));;
  }

  @Test
  public void schoolListWithoutActiveSecureExchangeUser_WithEdxUsers_ShouldReturnOneSchoolWithoutExchangeUsers() throws Exception {
    var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
    String schoolIdWithUser = entity.getEdxUserSchoolEntities().stream().findFirst().get().getSchoolID().toString();
    School dummySchoolWithUser = createDummySchool(schoolIdWithUser);
    dummySchoolWithUser.setDisplayName("School with user");

    Mockito.when(this.restUtils.getSchools()).thenReturn(List.of(createDummySchool(UUID.randomUUID().toString()), dummySchoolWithUser));

    this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE + "/stats/school-list-without-active-edx-users")
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_EDX_USERS"))).param("permissionCode", "Exchange"))
        .andDo(print()).andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].schoolName", is("Test School")))
        .andExpect(jsonPath("$.[0].schoolCategory", is("FED_BAND")))
        .andExpect(jsonPath("$.[0].mincode", is("1234567")))
        .andExpect(jsonPath("$", hasSize(1)));
  }

  private SecureExchangeEntity createDummySecureExchangeEntity(String contactIdenfier, String secureExchangeContactTypeCode) {
    return new SecureExchangeEntity().builder()
        .ministryOwnershipTeamID(UUID.randomUUID())
        .secureExchangeContactTypeCode(secureExchangeContactTypeCode)
        .contactIdentifier(contactIdenfier)
        .secureExchangeStatusCode("OPEN")
        .subject("TEST EXCHANGE")
        .isReadByMinistry(false)
        .isReadByExchangeContact(false)
        .statusUpdateDate(LocalDateTime.now())
        .createUser("TEST")
        .createDate(LocalDateTime.now())
        .updateUser("TEST")
        .updateDate(LocalDateTime.now())
        .build();
  }

  private School createDummySchool(String schoolId) {
    School school = new School();
    school.setDistrictId("34bb7566-ff59-653e-f778-2c1a4d669b00");
    school.setSchoolId(schoolId);
    school.setMincode("1234567");
    school.setSchoolNumber("00002");
    school.setDisplayName("Test School");
    school.setSchoolOrganizationCode("TRIMESTER");
    school.setSchoolCategoryCode("FED_BAND");
    school.setFacilityTypeCode("STANDARD");
    return school;
  }

}
