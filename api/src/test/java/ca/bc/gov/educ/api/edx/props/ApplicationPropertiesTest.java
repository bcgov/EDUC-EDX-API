package ca.bc.gov.educ.api.edx.props;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
public class ApplicationPropertiesTest extends BaseSecureExchangeAPITest {

  @Autowired
  ApplicationProperties applicationProperties;

  @Test
  public void testGetDifferentAttributes_GivenPresentInProperties_ShouldReturnNotNull() {
    assertThat(applicationProperties.getChesClientID()).isNotBlank();
    assertThat(applicationProperties.getChesClientSecret()).isNotBlank();
    assertThat(applicationProperties.getChesEndpointURL()).isNotBlank();
    assertThat(applicationProperties.getChesTokenURL()).isNotBlank();
    assertThat(applicationProperties.getEdxApplicationBaseUrl()).isNotBlank();
    assertThat(applicationProperties.getEdxSchoolUserActivationInviteAppendUrl()).isNotBlank();
    assertThat(applicationProperties.getConnectionName()).isNotBlank();
    assertThat(applicationProperties.getIsEmailNotificationSwitchedOn()).isTrue();
    assertThat(applicationProperties.getMaxEncodedFileSize()).isPositive();
    assertThat(applicationProperties.getMaxFileSize()).isPositive();
    assertThat(applicationProperties.getEdxSchoolUserActivationInviteValidity()).isPositive();
    assertThat(applicationProperties.getFileExtensions()).isNotEmpty();
    assertThat(applicationProperties.getServer()).isNotEmpty();

  }
}
