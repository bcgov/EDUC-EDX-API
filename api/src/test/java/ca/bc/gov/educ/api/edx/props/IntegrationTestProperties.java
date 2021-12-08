package ca.bc.gov.educ.api.edx.props;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class IntegrationTestProperties {
  @Value("${soam_discovery_url: }")
  @Getter
  private String soamUrl;
  @Value("${soam_client_id: }")
  @Getter
  private String soamClientId;
  @Value("${soam_client_secret: }")
  @Getter
  private String soamClientSecret;
  @Value("${api_url: }")
  @Getter
  private String apiUrl;
}
