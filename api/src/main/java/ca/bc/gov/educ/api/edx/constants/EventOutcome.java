package ca.bc.gov.educ.api.edx.constants;

/**
 * The enum Event outcome.
 */
public enum EventOutcome {

  /**
   * Initiate success event outcome.
   */
  INITIATE_SUCCESS,
  /**
   * Saga completed event outcome.
   */
  SAGA_COMPLETED,
  /**
   * EDX User School removed outcome
   */
  EDX_USER_SCHOOL_REMOVED,
  /**
   * Personal Activation code creation outcome
   */
  PERSONAL_ACTIVATION_CODE_CREATED,
  /**
   * User activation email sent outcome
   */
  EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT,

  /**
   * New secure exchange created event outcome.
   */
  NEW_SECURE_EXCHANGE_CREATED,
  /**
   * Email notification for new secure exchange sent event outcome.
   */
  EMAIL_NOTIFICATION_FOR_NEW_SECURE_EXCHANGE_SENT


  }
