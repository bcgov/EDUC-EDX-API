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

}


