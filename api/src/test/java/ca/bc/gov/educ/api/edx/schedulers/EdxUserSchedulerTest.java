package ca.bc.gov.educ.api.edx.schedulers;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserDistrictMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserSchoolMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.repository.EdxUserDistrictRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;

class EdxUserSchedulerTest extends BaseEdxAPITest {
  @Autowired
  private EdxUserSchoolRepository userSchoolRepository;

  @Autowired
  private EdxUserDistrictRepository userDistrictRepository;

  @Autowired
  private EdxUserRepository userRepository;

  @Autowired
  private EdxUserScheduler scheduler;

  private EdxUserSchoolMapper userSchoolMapper = EdxUserSchoolMapper.mapper;
  private EdxUserDistrictMapper userDistrictMapper = EdxUserDistrictMapper.mapper;
  private EdxUserMapper userMapper = EdxUserMapper.mapper;

  @BeforeEach
  public void setUp() {
    // LockAssert.TestHelper.makeAllAssertsPass(true);
  }

  @AfterEach
  public void tearDown() {
    this.userSchoolRepository.deleteAll();
    this.userDistrictRepository.deleteAll();
    this.userRepository.deleteAll();
  }

  @Test
  void testPurgeExpiredUsers() {
    LocalDateTime now = LocalDateTime.now().withNano(0);
    EdxUser user = userMapper.toStructure(this.userRepository.save(userMapper.toModel(this.createEdxUser())));

    List<EdxUserSchoolEntity> userSchools = List.of(
      this.createEdxUserSchool(user, now.minusDays(1)),
      this.createEdxUserSchool(user, now.plusDays(1))
    ).stream().map(userSchoolMapper::toModel).toList();

    List<EdxUserDistrictEntity> userDistricts = List.of(
      this.createEdxUserDistrict(user, now.minusDays(1)),
      this.createEdxUserDistrict(user, now.plusDays(1))
    ).stream().map(userDistrictMapper::toModel).toList();

    this.userSchoolRepository.saveAll(userSchools);
    this.userDistrictRepository.saveAll(userDistricts);
    scheduler.purgeExpiredEdxUsers();

    List<EdxUserSchoolEntity> foundUserSchoolEntities = this.userSchoolRepository.findAll();
    assertThat(foundUserSchoolEntities).hasSize(1);
    assertThat(foundUserSchoolEntities.get(0).getExpiryDate()).isAfter(now);

    List<EdxUserDistrictEntity> foundUserDistrictEntities = this.userDistrictRepository.findAll();
    assertThat(foundUserDistrictEntities).hasSize(1);
    assertThat(foundUserDistrictEntities.get(0).getExpiryDate()).isAfter(now);
  }

}
