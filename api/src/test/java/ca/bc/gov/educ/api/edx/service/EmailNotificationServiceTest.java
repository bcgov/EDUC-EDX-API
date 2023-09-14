package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;

import ca.bc.gov.educ.api.edx.service.v1.EmailNotificationService;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;


public class EmailNotificationServiceTest extends BaseSecureExchangeAPITest {

  @Autowired
  EmailNotificationService emailNotificationService;

  @Autowired
  RestUtils restUtils;

  @Captor
  ArgumentCaptor<String> emailBodyCaptor;

  @BeforeEach
  public void setUp() {
    openMocks(this);
    doNothing().when(this.restUtils).sendEmail(any(), any(), any(), any());
  }

  @Test
  public void sendEmail_givenGMP_ADDITIONAL_INFO_EmailNotificationEntity_shouldSendCorrectEmail() {
    final var emailNotificationEntity = this.createEmailNotificationEntity("edx.school.user.activation.invite", Map.of("firstName", "FirstName"));
    this.emailNotificationService.sendEmail(emailNotificationEntity);
    verify(this.restUtils, atLeastOnce()).sendEmail(eq(emailNotificationEntity.getFromEmail()), eq(emailNotificationEntity.getToEmail()), this.emailBodyCaptor.capture(), eq(emailNotificationEntity.getSubject()));
    assertThat(this.emailBodyCaptor.getValue()).doesNotContainPattern("\\{\\d\\}");
    assertThat(this.emailBodyCaptor.getValue()).contains("Edx School User Activation");
    assertThat(this.emailBodyCaptor.getValue()).contains("FirstName");

  }



  EmailNotification createEmailNotificationEntity(String templateName, Map<String, String> emailFields) {
    return EmailNotification.builder()
      .fromEmail("test@email.co")
      .toEmail("test@email.co")
      .subject("Edx User")
      .templateName(templateName)
      .emailFields(emailFields)
      .build();
  }
}
