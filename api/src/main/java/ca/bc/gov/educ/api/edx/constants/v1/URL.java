package ca.bc.gov.educ.api.edx.constants.v1;

public final class URL {
  public static final String STATUSES = "/statuses";
  public static final String CLAIM_ALL = "/claim";
  public static final String CONTACT_TYPE = "/contact-type";
  public static final String PAGINATED = "/paginated";
  public static final String SECURE_EXCHANGE_ID_COMMENTS = "/{secureExchangeId}/comments";
  public static final String SECURE_EXCHANGE_ID_DOCUMENTS = "/{secureExchangeID}/documents";

  public static final String SECURE_EXCHANGE_ID_NOTES = "/{secureExchangeId}/notes";
  public static final String SECURE_EXCHANGE_ID_STUDENTS = "/{secureExchangeId}/students";
  public static final String ALL_DOCUMENTS = "/documents";
  public static final String DOCUMENT_ID = "/{documentID}";
  public static final String DOCUMENT_TYPES = "/document-types";
  public static final String FILE_REQUIREMENTS = "/file-requirements";
  public static final String MINISTRY_TEAMS = "/ministry-teams";
  public static final String USER_SCHOOLS = "/user-schools";

  private URL(){

  }
  public static final String BASE_URL_SECURE_EXCHANGE="/api/v1/edx/exchange";
  public static final String BASE_URL_USERS="/api/v1/edx/users";
}
