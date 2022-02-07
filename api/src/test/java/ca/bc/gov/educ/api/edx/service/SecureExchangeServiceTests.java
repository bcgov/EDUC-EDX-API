package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.repository.MinistryOwnershipTeamRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeService;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import org.hibernate.Hibernate;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SecureExchangeServiceTests extends BaseSecureExchangeAPITest {

  @Autowired
  SecureExchangeService service;

  @Autowired
  private MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Autowired
  private SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Before
  public void setUp() {
  }

  @Test
  public void createSecureExchange() {
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
    assertThat(savedExchange.getSecureExchangeComment().size()).isEqualTo(1);

    var pulledExchange = secureExchangeRequestRepository.findById(savedExchange.getSecureExchangeID());
    assertThat(pulledExchange.get()).isNotNull();
    Hibernate.initialize(pulledExchange.get());
//    assertThat(pulledExchange.get().getSecureExchangeComment().size()).isEqualTo(1);
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
    entity.setCreateUser("test");
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateUser("test");
    entity.setUpdateDate(LocalDateTime.now());
    return entity;
  }
}
