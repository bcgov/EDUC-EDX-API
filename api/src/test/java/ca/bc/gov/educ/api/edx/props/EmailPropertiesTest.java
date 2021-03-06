package ca.bc.gov.educ.api.edx.props;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
public class EmailPropertiesTest extends BaseSecureExchangeAPITest {

  @Autowired
  EmailProperties emailProperties;
  @Test
  public void getEdxSchoolUserActivationInviteEmailSubject() {
    assertThat(emailProperties.getEdxSchoolUserActivationInviteEmailSubject()).isNotNull();
  }

  @Test
  public void getEdxSchoolUserActivationInviteEmailFrom() {
    assertThat(emailProperties.getEdxSchoolUserActivationInviteEmailFrom()).isNotNull();
  }
}
