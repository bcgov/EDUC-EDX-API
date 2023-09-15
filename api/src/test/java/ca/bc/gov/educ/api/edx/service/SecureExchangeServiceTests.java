package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeContactTypeCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeContactTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeService;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

class SecureExchangeServiceTests extends BaseSecureExchangeAPITest {

  @Autowired
  SecureExchangeService service;

  @Autowired
  private MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Autowired
  private SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Autowired
  private SecureExchangeContactTypeCodeTableRepository secureExchangeContactTypeCodeTableRepository;

  @BeforeEach
  public void setUp() {
    this.secureExchangeContactTypeCodeTableRepository.save(createContactType());
  }

  @AfterEach
  public void tearDown() {
    this.ministryOwnershipTeamRepository.deleteAll();
    this.secureExchangeContactTypeCodeTableRepository.deleteAll();
  }

  @Test
  @Transactional
  void createSecureExchange() {
    var ministryTeam = this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("Test Team", "TEST_TEAM"));
    var secureExchange = new SecureExchangeBuilder()
      .withoutSecureExchangeID().build();
    secureExchange.setMinistryOwnershipTeamID(ministryTeam.getMinistryOwnershipTeamId());
    var comment = getSecureExchangeCommentEntity();
    comment.setSecureExchangeEntity(secureExchange);
    secureExchange.setSecureExchangeComment(new HashSet<>());
    secureExchange.getSecureExchangeComment().add(comment);

    var savedExchange = this.service.createSecureExchange(secureExchange);
    assertThat(savedExchange).isNotNull();
    assertThat(savedExchange.getSecureExchangeComment()).hasSize(1);

    var pulledExchange = secureExchangeRequestRepository.findById(savedExchange.getSecureExchangeID());
    assertThat(pulledExchange.get()).isNotNull();
    Hibernate.initialize(pulledExchange.get());
  }

  @Test
  @Transactional
  void createSecureExchangeWithDocuments() {
    var ministryTeam = this.ministryOwnershipTeamRepository.save(getMinistryOwnershipEntity("Test Team", "TEST_TEAM"));
    var secureExchange = new SecureExchangeBuilder()
      .withoutSecureExchangeID().build();
    secureExchange.setMinistryOwnershipTeamID(ministryTeam.getMinistryOwnershipTeamId());
    var comment = getSecureExchangeCommentEntity();
    comment.setSecureExchangeEntity(secureExchange);
    secureExchange.setSecureExchangeComment(new HashSet<>());
    secureExchange.getSecureExchangeComment().add(comment);

    SecureExchangeDocumentEntity myDocument = new DocumentBuilder()
      .withoutDocumentID()
      .withSecureExchange(secureExchange).build();
    secureExchange.setSecureExchangeDocument(new HashSet<>());
    secureExchange.getSecureExchangeDocument().add(myDocument);

    var savedExchange = this.service.createSecureExchange(secureExchange);
    assertThat(savedExchange).isNotNull();
    assertThat(savedExchange.getSecureExchangeComment()).hasSize(1);
    assertThat(savedExchange.getSecureExchangeDocument()).hasSize(1);

    var pulledExchange = secureExchangeRequestRepository.findById(savedExchange.getSecureExchangeID());
    assertThat(pulledExchange.get()).isNotNull();
    Hibernate.initialize(pulledExchange.get());
  }

  @Test
  void getSecureExchangeContactTypes() {
    final Iterable<SecureExchangeContactTypeCodeEntity> contactTypes = this.service.getSecureExchangeContactTypeCodesList();
    assertThat(contactTypes).isNotNull();
    long count = StreamSupport.stream(contactTypes.spliterator(), false).count();
    assertThat(count).isEqualTo(1);
  }

  private SecureExchangeContactTypeCodeEntity createContactType() {
    final SecureExchangeContactTypeCodeEntity entity = new SecureExchangeContactTypeCodeEntity();
    entity.setSecureExchangeContactTypeCode("EDXUSER");
    entity.setDescription("Initial Review");
    entity.setDisplayOrder(1);
    entity.setEffectiveDate(LocalDateTime.now());
    entity.setLabel("Initial Review");
    entity.setCreateDate(LocalDateTime.now());
    entity.setCreateUser("TEST");
    entity.setUpdateUser("TEST");
    entity.setUpdateDate(LocalDateTime.now());
    entity.setExpiryDate(LocalDateTime.from(new GregorianCalendar(2099, Calendar.FEBRUARY, 1).toZonedDateTime()));
    return entity;
  }

  private MinistryOwnershipTeamEntity getMinistryOwnershipEntity(String teamName, String groupRoleIdentifier) {
    MinistryOwnershipTeamEntity entity = new MinistryOwnershipTeamEntity();
    entity.setTeamName(teamName);
    entity.setGroupRoleIdentifier(groupRoleIdentifier);
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }

  private SecureExchangeCommentEntity getSecureExchangeCommentEntity(){
    SecureExchangeCommentEntity entity = new SecureExchangeCommentEntity();
    entity.setStaffUserIdentifier("test");
    entity.setCommentUserName("test");
    entity.setContent("test");
    entity.setCommentTimestamp(LocalDateTime.now());
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }
}
