package ca.bc.gov.educ.api.edx.props;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class ApplicationProperties {
  public static final String CLIENT_ID = "EDX-API";
  public static final String YES = "Y";
  public static final String TRUE = "TRUE";
  public static final String CORRELATION_ID = "correlationID";
  @Value("${file.maxsize}")
  private int maxFileSize;

  @Value("${file.maxEncodedSize}")
  private int maxEncodedFileSize;

  @Value("${file.extensions}")
  private List<String> fileExtensions;

  @Value("${nats.server}")
  private String server;
  /**
   * The Max reconnect.
   */
  @Value("${nats.maxReconnect}")
  private int maxReconnect;
  /**
   * The Connection name.
   */
  @Value("${nats.connectionName}")
  private String connectionName;

  @Value("${ches.client.id}")
  private String chesClientID;
  @Value("${ches.client.secret}")
  private String chesClientSecret;
  @Value("${ches.token.url}")
  private String chesTokenURL;
  @Value("${ches.endpoint.url}")
  private String chesEndpointURL;
  @Value("${notification.email.switch.on}")
  private Boolean isEmailNotificationSwitchedOn;

  @Value("${edx.school.user.activation.invite.base.url}")
  private String edxApplicationBaseUrl;
  @Value("${edx.school.user.activation.invite.append.url}")
  private String edxSchoolUserActivationInviteAppendUrl;
  @Value("${edx.school.user.activation.invite.validity.hours}")
  private Long edxSchoolUserActivationInviteValidity;
  @Value("${apis.endpoints.student.api}")
  private String studentApiEndpoint;
}
