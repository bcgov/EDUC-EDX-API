package ca.bc.gov.educ.api.edx.props;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Getter
public class ApplicationProperties {
  public static final String CLIENT_ID = "PEN-REQUEST-API";
  public static final String YES = "Y";
  public static final String TRUE = "TRUE";
  public static final String CORRELATION_ID = "correlationID";
  @Value("${file.maxsize}")
  private int maxFileSize;

  @Value("${file.maxEncodedSize}")
  private int maxEncodedFileSize;

  @Value("${file.extensions}")
  private List<String> fileExtensions;

  @Value("${bcsc.auto.match.outcomes}")
  private List<String> bcscAutoMatchOutcomes;

}
