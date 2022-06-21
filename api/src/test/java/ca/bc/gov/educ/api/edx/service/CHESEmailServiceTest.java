package ca.bc.gov.educ.api.edx.service;


import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.service.v1.CHESEmailService;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import ca.bc.gov.educ.api.edx.utils.RestUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

public class CHESEmailServiceTest extends BaseSecureExchangeAPITest {


  @Autowired
  CHESEmailService emailService;

  @Autowired
  RestUtils restUtils;

  @Autowired
  ApplicationProperties properties;


  @Before
  public void setUp() {
    openMocks(this);
  }


  @Test
  public void sendEmail() {
    final var payload = EmailNotification.builder().build();
    payload.setToEmail("test@gov.bc.ca");
    doNothing().when(this.restUtils).sendEmail(any(), any(), any(),any());
    this.emailService.sendEmail("test@test.com", "test@gov.bc.ca","hello", "Request Completed");
    verify(this.restUtils, atLeastOnce()).sendEmail("test@test.com","test@gov.bc.ca", "hello", "Request Completed");
  }
}
