package ca.bc.gov.educ.api.edx.constants.v1;

public final class URL {
  public static final String STATUSES = "/statuses";
  public static final String GENDER_CODES = "/gender-codes";
  public static final String PAGINATED = "/paginated";
  public static final String STATS = "/stats";
  public static final String PEN_REQUEST_ID_COMMENTS = "/{penRequestId}/comments";
  public static final String PEN_REQUEST_ID_DOCUMENTS = "/{penRequestID}/documents";
  public static final String ALL_DOCUMENTS = "/documents";
  public static final String DOCUMENT_ID = "/{documentID}";
  public static final String DOCUMENT_TYPES = "/document-types";
  public static final String FILE_REQUIREMENTS = "/file-requirements";
  public static final String PEN_REQUEST_MACRO = "/pen-request-macro";
  public static final String MACRO_ID = "/{macroId}";

  private URL(){

  }
  public static final String BASE_URL="/api/v1/pen-request";
}
