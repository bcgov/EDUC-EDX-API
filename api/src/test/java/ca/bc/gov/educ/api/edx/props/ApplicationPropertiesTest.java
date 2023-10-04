package ca.bc.gov.educ.api.edx.props;

import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;
class ApplicationPropertiesTest extends BaseEdxAPITest {

  @Autowired
  ApplicationProperties applicationProperties;

  @Test
  void testGetDifferentAttributes_GivenPresentInProperties_ShouldReturnNotNull() {
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
    assertThat(applicationProperties.getMaxReconnect()).isPositive();
    assertThat(applicationProperties.getEdxActivationCodeLength()).isPositive();
    assertThat(applicationProperties.getEdxActivationCodeValidCharacters()).isNotEmpty();
  }
}
