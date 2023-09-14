package ca.bc.gov.educ.api.edx.props;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
class EmailPropertiesTest extends BaseSecureExchangeAPITest {

  @Autowired
  EmailProperties emailProperties;
  @Test
  void getEdxSchoolUserActivationInviteEmailSubject() {
    assertThat(emailProperties.getEdxSchoolUserActivationInviteEmailSubject()).isNotNull();
  }

  @Test
  void getEdxSchoolUserActivationInviteEmailFrom() {
    assertThat(emailProperties.getEdxSchoolUserActivationInviteEmailFrom()).isNotNull();
  }
}
