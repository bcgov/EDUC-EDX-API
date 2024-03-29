package ca.bc.gov.educ.api.edx.service;


import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.CHESEmailService;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class CHESEmailServiceTest extends BaseEdxAPITest {


  @Autowired
  CHESEmailService emailService;

  @Autowired
  RestUtils restUtils;

  @Autowired
  ApplicationProperties properties;


  @BeforeEach
  public void setUp() {
    openMocks(this);
  }


  @Test
  void sendEmail() {
    final var payload = EmailNotification.builder().build();
    payload.setToEmail("test@gov.bc.ca");
    doNothing().when(this.restUtils).sendEmail(any(), any(), any(),any());
    this.emailService.sendEmail("test@test.com", "test@gov.bc.ca","hello", "Request Completed");
    verify(this.restUtils, atLeastOnce()).sendEmail("test@test.com","test@gov.bc.ca", "hello", "Request Completed");
  }
}
