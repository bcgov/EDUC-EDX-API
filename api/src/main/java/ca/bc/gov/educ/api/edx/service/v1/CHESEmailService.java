package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.rest.RestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CHESEmailService {
  private final RestUtils restUtils;

  @Autowired
  public CHESEmailService(final RestUtils restUtils) {
    this.restUtils = restUtils;
  }


  public void sendEmail(final String fromEmail, final String toEmail, final String body, final String subject) {
    this.restUtils.sendEmail(fromEmail, toEmail, body, subject);
  }

}
