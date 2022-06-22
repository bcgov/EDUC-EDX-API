package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.SecureExchangeRuntimeException;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.Map;

@Service
@Slf4j
public class EmailNotificationService {
  /**
   * the Thymeleaf template engine
   */
  private final SpringTemplateEngine templateEngine;

  @Getter(AccessLevel.PRIVATE)
  private final CHESEmailService chesEmailService;

  private final Map<String, String> templateConfig;

  public EmailNotificationService(final Map<String, String> templateConfig, final SpringTemplateEngine templateEngine, final CHESEmailService chesEmailService) {
    this.templateEngine = templateEngine;
    this.chesEmailService = chesEmailService;
    this.templateConfig = templateConfig;
  }

  public void sendEmail(final EmailNotification emailNotification) {
    log.debug("Sending email");

    final var ctx = new Context();
    emailNotification.getEmailFields().forEach(ctx::setVariable);

    if (!this.templateConfig.containsKey(emailNotification.getTemplateName())) {
      throw new SecureExchangeRuntimeException("Not found email template for template name :: " + emailNotification.getTemplateName());
    }

    final var body = this.templateEngine.process(this.templateConfig.get(emailNotification.getTemplateName()), ctx);

    this.getChesEmailService().sendEmail(emailNotification.getFromEmail(), emailNotification.getToEmail(), body, emailNotification.getSubject());
    log.debug("Email sent successfully");
  }
}
