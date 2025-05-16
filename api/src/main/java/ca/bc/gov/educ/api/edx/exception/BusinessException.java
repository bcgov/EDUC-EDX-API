package ca.bc.gov.educ.api.edx.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * The type Business exception.
 */
@Slf4j
public class BusinessException extends Exception {

  @Getter
  private final BusinessError businessError;

  /**
   * Instantiates a new Business exception.
   *
   * @param businessError the business error
   * @param messageArgs   the message args
   */
  public BusinessException(BusinessError businessError, String... messageArgs) {
    super(businessError.getCode());
    this.businessError = businessError;
    var finalLogMessage = businessError.getCode();
    if (messageArgs != null) {
      finalLogMessage = getFormattedMessage(finalLogMessage, messageArgs);
    }
    log.error(finalLogMessage);
  }

  /**
   * Gets formatted message.
   *
   * @param msg           the msg
   * @param substitutions the substitutions
   * @return the formatted message
   */
  private static String getFormattedMessage(String msg, String... substitutions) {
    final String format = msg.replace("$?", "%s");
    return String.format(format, (Object[]) substitutions);
  }
}

