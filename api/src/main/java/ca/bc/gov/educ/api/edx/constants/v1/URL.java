package ca.bc.gov.educ.api.edx.constants.v1;

public final class URL {
  public static final String STATUSES = "/statuses";
  public static final String PAGINATED = "/paginated";
  public static final String SECURE_EXCHANGE_ID_COMMENTS = "/{secureExchangeId}/comments";
  public static final String SECURE_EXCHANGE_ID_DOCUMENTS = "/{secureExchangeID}/documents";
  public static final String ALL_DOCUMENTS = "/documents";
  public static final String DOCUMENT_ID = "/{documentID}";
  public static final String DOCUMENT_TYPES = "/document-types";
  public static final String FILE_REQUIREMENTS = "/file-requirements";

  private URL(){

  }
  public static final String BASE_URL="/api/v1/pen-request";
}
