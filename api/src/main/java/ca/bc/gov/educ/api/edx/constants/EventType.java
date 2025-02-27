package ca.bc.gov.educ.api.edx.constants;

import java.util.Arrays;
import java.util.Optional;

/**
 * The enum Event type.
 */
public enum EventType {
  /**
   * Initiated event type.
   */
  INITIATED,
  /**
   * Mark saga complete event type.
   */
  MARK_SAGA_COMPLETE,
  /**
   * Create personal activation code event type.
   */
  REMOVE_USER_SCHOOL_ACCESS,

  REMOVE_USER_DISTRICT_ACCESS,
  CREATE_PERSONAL_ACTIVATION_CODE,
  CREATE_SCHOOL_PRIMARY_CODE,
  CREATE_DISTRICT_PRIMARY_CODE,
  /**
   * Send edx user activation email event type.
   */
  SEND_EDX_SCHOOL_USER_ACTIVATION_EMAIL,
  /**
   * Edx school user activation complete event type.
   */
  EDX_SCHOOL_USER_ACTIVATION_COMPLETE,
  /**
   * Create new secure exchange event type.
   */
  CREATE_NEW_SECURE_EXCHANGE,

  /**
   * Send email notification for new secure exchange event type.
   */
  SEND_EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE,

  /**
   * New secure exchange complete event type.
   */
  NEW_SECURE_EXCHANGE_COMPLETE,

  /**
   * Create secure exchange comment event type.
   */
  CREATE_SECURE_EXCHANGE_COMMENT,

  /**
   * Send email notification for secure exchange comment event type.
   */
  SEND_EMAIL_NOTIFICATION_FOR_SECURE_EXCHANGE_COMMENT,

  /**
   * Secure exchange comment complete event type.
   */
  SECURE_EXCHANGE_COMMENT_COMPLETE,

  SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL,
  SEND_PRIMARY_ACTIVATION_CODE,


  // Create/move School Events
  CREATE_SCHOOL,
  UPDATE_SCHOOL,
  FIND_SCHOOL,
  COPY_USERS_TO_NEW_SCHOOL,
  GET_PAGINATED_SCHOOLS,
  MOVE_SCHOOL,
  ONBOARD_INITIAL_USER,
  INVITE_INITIAL_USER
;
  public static boolean isAValidEvent(String incomingEvent) {
    return Arrays.stream(values()).anyMatch(eventType -> eventType.toString().equalsIgnoreCase(incomingEvent));
  }

}
