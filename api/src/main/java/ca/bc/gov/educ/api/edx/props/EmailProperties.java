package ca.bc.gov.educ.api.edx.props;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class EmailProperties {
  @Value("${email.subject.edx.school.user.activation.invite}")
  private String edxSchoolUserActivationInviteEmailSubject;

  @Value("${email.from.edx.school.user.activation.invite}")
  private String edxSchoolUserActivationInviteEmailFrom;

  @Value("${email.subject.edx.new.secure.exchange.notification}")
  private String edxNewSecureExchangeNotificationEmailSubject;

  @Value("${email.subject.edx.secure.exchange.comment.notification}")
  private String edxSecureExchangeCommentNotificationEmailSubject;

  @Value("${email.subject.edx.school.primary-code.notification}")
  private String edxSecureExchangePrimaryCodeNotificationEmailSubject;

}


