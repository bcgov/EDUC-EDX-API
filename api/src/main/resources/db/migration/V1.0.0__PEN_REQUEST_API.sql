CREATE TABLE PEN_RETRIEVAL_REQUEST_GENDER_CODE
(
    GENDER_CODE    VARCHAR2(10)           NOT NULL,
    LABEL          VARCHAR2(30),
    DESCRIPTION    VARCHAR2(255),
    DISPLAY_ORDER  NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE DATE                   NOT NULL,
    EXPIRY_DATE    DATE                   NOT NULL,
    CREATE_USER    VARCHAR2(32)           NOT NULL,
    CREATE_DATE    DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER    VARCHAR2(32)           NOT NULL,
    UPDATE_DATE    DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_RETRIEVAL_REQUEST_GENDER_CODE_PK PRIMARY KEY (GENDER_CODE)
);
COMMENT ON TABLE PEN_RETRIEVAL_REQUEST_GENDER_CODE IS 'Gender Code lists the standard codes for Gender: Female, Male, Diverse.';

CREATE TABLE PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE
(
    PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE VARCHAR2(10)           NOT NULL,
    LABEL                                 VARCHAR2(30),
    DESCRIPTION                           VARCHAR2(255),
    DISPLAY_ORDER                         NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE                        DATE                   NOT NULL,
    EXPIRY_DATE                           DATE                   NOT NULL,
    CREATE_USER                           VARCHAR2(32)           NOT NULL,
    CREATE_DATE                           DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                           VARCHAR2(32)           NOT NULL,
    UPDATE_DATE                           DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE_PK PRIMARY KEY (PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE)
);
COMMENT ON TABLE PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE IS 'Macro Type Code indicates the supported types of text macros.';

CREATE TABLE PEN_RETRIEVAL_REQUEST_STATUS_CODE
(
    PEN_RETRIEVAL_REQUEST_STATUS_CODE VARCHAR2(10)           NOT NULL,
    LABEL                             VARCHAR2(30),
    DESCRIPTION                       VARCHAR2(255),
    DISPLAY_ORDER                     NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE                    DATE                   NOT NULL,
    EXPIRY_DATE                       DATE                   NOT NULL,
    CREATE_USER                       VARCHAR2(32)           NOT NULL,
    CREATE_DATE                       DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                       VARCHAR2(32)           NOT NULL,
    UPDATE_DATE                       DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_RETRIEVAL_REQUEST_STATUS_CODE_PK PRIMARY KEY (PEN_RETRIEVAL_REQUEST_STATUS_CODE)
);
CREATE TABLE PEN_RETRIEVAL_REQUEST_MACRO
(
    PEN_RETRIEVAL_REQUEST_MACRO_ID RAW(16)              NOT NULL,
    MACRO_CODE                     VARCHAR2(10)         NOT NULL,
    MACRO_TEXT                     VARCHAR2(4000)       NOT NULL,
    MACRO_TYPE_CODE                VARCHAR2(10)         NOT NULL,
    CREATE_USER                    VARCHAR2(32)         NOT NULL,
    CREATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                    VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_RETRIEVAL_REQUEST_MACRO_PK PRIMARY KEY (PEN_RETRIEVAL_REQUEST_MACRO_ID)
);
COMMENT ON TABLE PEN_RETRIEVAL_REQUEST_MACRO IS 'List of text macros used as standard messages by PEN Staff when completing PEN Retrieval Requests.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_MACRO.MACRO_CODE IS 'A short text string that identifies the macro and when identified will be replaced by the macro text.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_MACRO.MACRO_TEXT IS 'A standard text string that will be substituted for the macro code by the application.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_MACRO.MACRO_TYPE_CODE IS 'A code value indicating the type, or class, of the text macro.';

ALTER TABLE PEN_RETRIEVAL_REQUEST_MACRO
    ADD CONSTRAINT UQ_REQUEST_MACRO_ID_CODE_TYPE UNIQUE (MACRO_CODE, MACRO_TYPE_CODE);
ALTER TABLE PEN_RETRIEVAL_REQUEST_MACRO
    ADD CONSTRAINT FK_REQUEST_MACRO_TYPE_CODE FOREIGN KEY (MACRO_TYPE_CODE) REFERENCES PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE (PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE);
CREATE TABLE PEN_RETRIEVAL_REQUEST
(
    PEN_RETRIEVAL_REQUEST_ID          RAW(16)              NOT NULL,
    DIGITAL_IDENTITY_ID               RAW(16)              NOT NULL,
    PEN_RETRIEVAL_REQUEST_STATUS_CODE VARCHAR2(10)         NOT NULL,
    LEGAL_FIRST_NAME                  VARCHAR2(40),
    LEGAL_MIDDLE_NAMES                VARCHAR2(255),
    LEGAL_LAST_NAME                   VARCHAR2(40)         NOT NULL,
    DOB                               DATE                 NOT NULL,
    GENDER_CODE                       VARCHAR2(1)          NOT NULL,
    USUAL_FIRST_NAME                  VARCHAR2(40),
    USUAL_MIDDLE_NAMES                VARCHAR2(255),
    USUAL_LAST_NAME                   VARCHAR2(40),
    EMAIL                             VARCHAR2(255)        NOT NULL,
    MAIDEN_NAME                       VARCHAR2(40),
    PAST_NAMES                        VARCHAR2(255),
    LAST_BC_SCHOOL                    VARCHAR2(255),
    BCSC_AUTO_MATCH_OUTCOME           VARCHAR2(255),
    BCSC_AUTO_MATCH_DETAIL            VARCHAR2(255),
    LAST_BC_SCHOOL_STUDENT_NUMBER     VARCHAR2(12),
    CURRENT_SCHOOL                    VARCHAR2(255),
    REVIEWER                          VARCHAR2(255),
    INITIAL_SUBMIT_DATE               DATE,
    STATUS_UPDATE_DATE                DATE,
    FAILURE_REASON                    VARCHAR2(4000),
    EMAIL_VERIFIED                    VARCHAR2(1)          NOT NULL,
    PEN                               VARCHAR2(9),
    CREATE_USER                       VARCHAR2(32)         NOT NULL,
    CREATE_DATE                       DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                       VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                       DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_RETRIEVAL_REQUEST_PK PRIMARY KEY (PEN_RETRIEVAL_REQUEST_ID)
);

COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.PEN_RETRIEVAL_REQUEST_ID IS 'Unique surrogate key for each PEN Retrieval request. GUID value must be provided during insert.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.DIGITAL_IDENTITY_ID IS 'Foreign key to Digital Identity table identifying the Digital Identity that is was used to make this request';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.PEN_RETRIEVAL_REQUEST_STATUS_CODE IS 'Code indicating the status of the Student PEN Retrieval request';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.LEGAL_FIRST_NAME IS 'The legal first name of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.LEGAL_MIDDLE_NAMES IS 'The legal middle names of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.LEGAL_LAST_NAME IS 'The legal last name of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.DOB IS 'The date of birth of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.GENDER_CODE IS 'The gender of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.USUAL_FIRST_NAME IS 'The usual/preferred first name of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.USUAL_MIDDLE_NAMES IS 'The usual/preferred middle name of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.USUAL_LAST_NAME IS 'The usual/preferred last name of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.EMAIL IS 'Email of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.MAIDEN_NAME IS 'Maiden Name of the student, if applicable';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.PAST_NAMES IS 'Past Names of the student';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.LAST_BC_SCHOOL IS 'Name of last BC school that the student attended';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.LAST_BC_SCHOOL_STUDENT_NUMBER IS 'Student Number assigned to student at the last BC school attended';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.CURRENT_SCHOOL IS 'Name of current BC school, if applicable';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.REVIEWER IS 'IDIR of the staff user who is working or did work on the PEN Retrieval Request';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.INITIAL_SUBMIT_DATE IS 'Date and time that the Student first fully submitted the request, which does not happen until after they submit and verify their email address.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.STATUS_UPDATE_DATE IS 'Date and time that the status of the PEN Retrieval Request was last updated.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.FAILURE_REASON IS 'Free text reason for why Min EDUC staff could not complete the request. This is used for both Rejects and Unable to complete failures.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.BCSC_AUTO_MATCH_OUTCOME IS 'Short value indicating the outcome of performing the BCSC AutoMatch search. Values NOMATCH, ONEMATCH, MANYMATCHES, RIGHTPEN, WRONGPEN, null.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.BCSC_AUTO_MATCH_DETAIL IS 'Description providing more info about outcome of performing the BCSC AutoMatch search. When the search returned one result, this will hold the PEN and Legal Names of the the record matched.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.PEN IS 'The PEN value that was matched to this PEN Request, either manually by staff or automatically by the system.';

ALTER TABLE PEN_RETRIEVAL_REQUEST
    ADD CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_PEN_RETRIEVAL_REQUEST_STATUS_CODE FOREIGN KEY (PEN_RETRIEVAL_REQUEST_STATUS_CODE) REFERENCES PEN_RETRIEVAL_REQUEST_STATUS_CODE (PEN_RETRIEVAL_REQUEST_STATUS_CODE);
ALTER TABLE PEN_RETRIEVAL_REQUEST
    ADD CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_PEN_RETRIEVAL_REQUEST_GENDER_CODE FOREIGN KEY (GENDER_CODE) REFERENCES PEN_RETRIEVAL_REQUEST_GENDER_CODE (GENDER_CODE);

CREATE TABLE PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE
(
    PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE VARCHAR2(10)           NOT NULL,
    LABEL                                    VARCHAR2(30),
    DESCRIPTION                              VARCHAR2(255),
    DISPLAY_ORDER                            NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE                           DATE                   NOT NULL,
    EXPIRY_DATE                              DATE                   NOT NULL,
    CREATE_USER                              VARCHAR2(32)           NOT NULL,
    CREATE_DATE                              DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                              VARCHAR2(32)           NOT NULL,
    UPDATE_DATE                              DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE_PK PRIMARY KEY (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE)
);
COMMENT ON TABLE PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE IS 'PEN Retrieval Request Document Type Code lists the semantic types of documents that are supported. Examples include Birth Certificate (image of), Passport image, Permanent Resident Card image, etc.';

-- Table PEN_RETRIEVAL_REQUEST_DOCUMENT
CREATE TABLE PEN_RETRIEVAL_REQUEST_DOCUMENT
(
    PEN_RETRIEVAL_REQUEST_DOCUMENT_ID        RAW(16)              NOT NULL,
    PEN_RETRIEVAL_REQUEST_ID                 RAW(16)              NOT NULL,
    PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE VARCHAR2(10)         NOT NULL,
    FILE_NAME                                VARCHAR2(255)        NOT NULL,
    FILE_EXTENSION                           VARCHAR2(255),
    FILE_SIZE                                NUMBER,
    DOCUMENT_DATA                            BLOB                 NOT NULL,
    CREATE_USER                              VARCHAR2(32)         NOT NULL,
    CREATE_DATE                              DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                              VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                              DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_RETRIEVAL_REQUEST_DOCUMENT_PK PRIMARY KEY (PEN_RETRIEVAL_REQUEST_DOCUMENT_ID)
)
    TABLESPACE API_PEN_RETRIEVAL_BLOB_DATA;
COMMENT ON TABLE PEN_RETRIEVAL_REQUEST_DOCUMENT IS 'Holds documents related to Students, either directly or indirectly.';

COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_DOCUMENT.PEN_RETRIEVAL_REQUEST_DOCUMENT_ID IS 'Unique surrogate primary key for each Student Document. GUID value must be provided during insert.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_DOCUMENT.PEN_RETRIEVAL_REQUEST_ID IS 'Foreign key to the PEN Retrieval Request.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_DOCUMENT.PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE IS 'Code indicating the type of the semantic type of the document. E.g. Birth Certificate, Passport, etc.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_DOCUMENT.FILE_NAME IS 'Name of the document file, without any local file path.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_DOCUMENT.FILE_EXTENSION IS 'Extension portion of the filename, if present. E.g. JPG, PNG, PDF, etc.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_DOCUMENT.FILE_SIZE IS 'Size of the file in bytes, if known.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST_DOCUMENT.DOCUMENT_DATA IS 'Binary representation of the file contents.';

ALTER TABLE PEN_RETRIEVAL_REQUEST_DOCUMENT
    ADD CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_DOCUMENT_PEN_RETRIEVAL_REQUEST_ID FOREIGN KEY (PEN_RETRIEVAL_REQUEST_ID) REFERENCES PEN_RETRIEVAL_REQUEST (PEN_RETRIEVAL_REQUEST_ID);
ALTER TABLE PEN_RETRIEVAL_REQUEST_DOCUMENT
    ADD CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE FOREIGN KEY (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE) REFERENCES PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE);

-- Table PEN_RETRIEVAL_REQUEST_COMMENT
CREATE TABLE PEN_RETRIEVAL_REQUEST_COMMENT
(
    PEN_RETRIEVAL_REQUEST_COMMENT_ID RAW(16)              NOT NULL,
    PEN_RETRIEVAL_REQUEST_ID         RAW(16)              NOT NULL,
    STAFF_MEMBER_IDIR_GUID           RAW(16),
    STAFF_MEMBER_NAME                VARCHAR2(255),
    COMMENT_CONTENT                  VARCHAR2(4000),
    COMMENT_TIMESTAMP                DATE,
    CREATE_USER                      VARCHAR2(32)         NOT NULL,
    CREATE_DATE                      DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                      VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                      DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT PEN_RETRIEVAL_REQUEST_COMMENT_PK PRIMARY KEY (PEN_RETRIEVAL_REQUEST_COMMENT_ID)
);
ALTER TABLE PEN_RETRIEVAL_REQUEST_COMMENT
    ADD CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_COMMENT_PEN_RETRIEVAL_REQUEST_ID FOREIGN KEY (PEN_RETRIEVAL_REQUEST_ID) REFERENCES PEN_RETRIEVAL_REQUEST (PEN_RETRIEVAL_REQUEST_ID);

--Master Data
INSERT INTO PEN_RETRIEVAL_REQUEST_GENDER_CODE (GENDER_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE,
                                               EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('F', 'Female',
        'Persons whose current gender is female. This includes cisgender and transgender persons who are female.', 1,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_GENDER_CODE (GENDER_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE,
                                               EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('M', 'Male',
        'Persons whose current gender is male. This includes cisgender and transgender persons who are male.', 2,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_GENDER_CODE (GENDER_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE,
                                               EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('X', 'Gender Diverse',
        'Persons whose current gender is not exclusively as male or female. It includes people who do not have one gender, have no gender, are non-binary, or are Two-Spirit.',
        3, to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_GENDER_CODE (GENDER_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER, EFFECTIVE_DATE,
                                               EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('U', 'Unknown',
        'Persons whose gender is not known at the time of data collection. It may or may not get updated at a later point in time. X is different than U.',
        4, to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO PEN_RETRIEVAL_REQUEST_STATUS_CODE (PEN_RETRIEVAL_REQUEST_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                               EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                               UPDATE_DATE)
VALUES ('DRAFT', 'Draft', 'Request created but not yet submitted.', 1, to_date('2020-01-01', 'YYYY-MM-DD'),
        to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_STATUS_CODE (PEN_RETRIEVAL_REQUEST_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                               EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                               UPDATE_DATE)
VALUES ('INITREV', 'First Review', 'Request has been submitted and is now in it''s first review by staff.', 2,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_STATUS_CODE (PEN_RETRIEVAL_REQUEST_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                               EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                               UPDATE_DATE)
VALUES ('RETURNED', 'Returned for more information', 'Request has been returned to the submitter for more information.',
        3, to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_STATUS_CODE (PEN_RETRIEVAL_REQUEST_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                               EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                               UPDATE_DATE)
VALUES ('SUBSREV', 'Subsequent Review',
        'Request has been resubmitted with more info and is now in another review by staff.', 4,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_STATUS_CODE (PEN_RETRIEVAL_REQUEST_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                               EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                               UPDATE_DATE)
VALUES ('AUTO', 'Completed by auto-match', 'Request was completed by the auto-match process, without staff review.', 5,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_STATUS_CODE (PEN_RETRIEVAL_REQUEST_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                               EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                               UPDATE_DATE)
VALUES ('MANUAL', 'Completed by manual match', 'Request was completed by staff determining the matching PEN.', 6,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));
INSERT INTO PEN_RETRIEVAL_REQUEST_STATUS_CODE (PEN_RETRIEVAL_REQUEST_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                               EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                               UPDATE_DATE)
VALUES ('REJECTED', 'Could not be fulfilled', 'Request could not be fullfilled by staff for the reasons provided.', 7,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/GRCHWELO',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/GRCHWELO', to_date('2019-11-07', 'YYYY-MM-DD'));


INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CABIRTH', 'Canadian Birth Certificate', 'Canadian Birth Certificate', 10,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CAPASSPORT', 'Canadian Passport', 'Canadian Passport', 20,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CADL', 'Canadian Driver''s Licence', 'Canadian Driver''s Licence', 30,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('BCIDCARD', 'Provincial Identification Card', 'Provincial Identification Card', 40,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('BCSCPHOTO', 'BC Services Card w Photo', 'BC Services Card (Photo version only)', 50,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CACITZCARD', 'Canadian Citizenship Card', 'Canadian Citizenship Card', 60,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('PRCARD', 'Permanent Residence Card', 'Permanent Residence Card', 70,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('STUDENTPMT', 'Student / Study Permit', 'Student / Study Permit', 80,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('IMM5292', 'IMM5292 Conf of Perm Residence', 'Confirmation of Permanent Residence (IMM5292)', 90,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('IMM1000', 'IMM1000 Record of Landing',
        'Canadian Immigration Record of Landing (IMM 1000, not valid after June 2002)', 100,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('INDSTATUS', 'Indian Status Card', 'Indian Status Card', 110,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('NAMECHANGE', 'Legal Name Change document', 'Canadian court order approving legal change of name', 120,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('FORPASSPRT', 'Foreign Passport', 'Foreign Passport', 130,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('ADOPTION', 'Canadian adoption order', 'Canadian adoption order', 140,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('MARRIAGE', 'Marriage Certificate', 'Marriage Certificate', 150,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('FORBIRTH', 'Foreign Birth Certificate', 'Foreign Birth Certificate (with English translation)', 160,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE (PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('OTHER', 'Other', 'Other document type', 170, to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/GRCHWELO',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));


INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE (PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE, LABEL, DESCRIPTION,
                                                   DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE,
                                                   UPDATE_USER, UPDATE_DATE)
VALUES ('MOREINFO', 'More Information Macro',
        'Macros used when requesting that the student provide more information for a PEN Retrieval Request', 1,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-02 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-02 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE (PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE, LABEL, DESCRIPTION,
                                                   DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE,
                                                   UPDATE_USER, UPDATE_DATE)
VALUES ('REJECT', 'Reject Reason Macro', 'Macros used when rejecting a PEN Retrieval Request', 2,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-02 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-02 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PCN',
        'A PEN number can not be located using the information in your PEN request.' || CHR(10) || CHR(10) ||
        'Please provide all other given names or surnames you have previously used or advise if you have never used any other names.',
        'MOREINFO', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PID',
        'To continue with your PEN request upload an IMG or PDF of your current Government Issued photo Identification (ID).' ||
        CHR(10) || CHR(10) ||
        'NOTE: If the name listed on the ID you upload is different from what''s in the PEN system, we will update our data to match. ID is covered by the B.C. Freedom of Information Protection of Privacy.',
        'MOREINFO', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'SOA',
        'To continue with your PEN request please confirm the last B.C. Schools you attended or graduated from, including any applications to B.C. Post Secondary Institutions',
        'MOREINFO', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NPF',
        'A PEN number cannot be located using the information in your PEN request.' || CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=CCE3580078AD4F988579DD5EBB42BA85 .' ||
        CHR(10) || CHR(10) ||
        'You do not require a PEN for an application to a B.C. school or PSI, a PEN will be assigned upon registration.',
        'REJECT', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'OOP',
        'The information provided in your PEN request indicates you may not have attended a B.C. School or public Post-Secondary Institution (PSI).' ||
        CHR(10) || CHR(10) ||
        'You do not require a PEN for an application to a B.C. school or PSI, a PEN will be assigned upon registration.' ||
        CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=CCE3580078AD4F988579DD5EBB42BA85',
        'REJECT', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'XPR',
        'The identity of the person making the request cannot be confirmed as the same as the PEN owner.' || CHR(10) ||
        CHR(10) ||
        'Under the B.C. Freedom of Information Protection of Privacy Act, the PEN number can only be provided to the person assigned the PEN, that person''s current or future school, or that person''s parent or guardian.' ||
        CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=CCE3580078AD4F988579DD5EBB42BA85',
        'REJECT', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

CREATE INDEX PEN_RETRIEVAL_REQUEST_DIGITAL_IDENTITY_ID_I ON PEN_RETRIEVAL_REQUEST (DIGITAL_IDENTITY_ID);
CREATE INDEX PEN_RETRIEVAL_REQUEST_COMMENT_PEN_RETRIEVAL_REQUEST_ID_I ON PEN_RETRIEVAL_REQUEST_COMMENT (PEN_RETRIEVAL_REQUEST_ID);
CREATE INDEX PEN_RETRIEVAL_REQUEST_DOCUMENT_PEN_RETRIEVAL_REQUEST_ID_I ON PEN_RETRIEVAL_REQUEST_DOCUMENT (PEN_RETRIEVAL_REQUEST_ID);

INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE (PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE, LABEL, DESCRIPTION,
                                                   DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE,
                                                   UPDATE_USER, UPDATE_DATE)
values ('COMPLETE', 'Complete Reason Macro', 'Macros used when completing a PEN Retrieval Request', 3,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2020-04-02 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2020-04-02 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

DELETE
FROM PEN_RETRIEVAL_REQUEST_MACRO;

-- PEN Retrieval Request Macro
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PCN',
        'A PEN number can not be located using the information in your PEN request.' || CHR(10) || CHR(10) ||
        'Please provide all other given names or surnames you have previously used or advise if you have never used any other names.',
        'MOREINFO', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PID',
        'To continue with your PEN request upload an IMG or PDF of your current Government Issued photo Identification (ID).' ||
        CHR(10) || CHR(10) ||
        'NOTE: If the name listed on the ID you upload is different from what''s in the PEN system, we will update our data to match. ID is covered by the B.C. Freedom of Information Protection of Privacy.',
        'MOREINFO', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'SOA',
        'To continue with your PEN request please confirm the last B.C. Schools you attended or graduated from, including any applications to B.C. Post Secondary Institutions',
        'MOREINFO', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NPF',
        'A PEN number cannot be located using the information in your PEN request.' || CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=74E29C67215B4988ABCD778F453A3129.' ||
        CHR(10) || CHR(10) ||
        'You do not require a PEN for an application to a B.C. school or PSI, a PEN will be assigned upon registration.',
        'REJECT', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'OOP',
        'The information provided in your PEN request indicates you may not have attended a B.C. School or public Post-Secondary Institution (PSI).' ||
        CHR(10) || CHR(10) ||
        'You do not require a PEN for an application to a B.C. school or PSI, a PEN will be assigned upon registration.' ||
        CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=74E29C67215B4988ABCD778F453A3129',
        'REJECT', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'XPR',
        'The identity of the person making the request cannot be confirmed as the same as the PEN owner.' || CHR(10) ||
        CHR(10) ||
        'Under the B.C. Freedom of Information Protection of Privacy Act, the PEN number can only be provided to the person assigned the PEN, that person''s current or future school, or that person''s parent or guardian.' ||
        CHR(10) || CHR(10) ||
        'For additional information visit: https://www2.gov.bc.ca/gov/content?id=74E29C67215B4988ABCD778F453A3129',
        'REJECT', 'IDIR/JOCOX', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/JOCOX',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));


INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NME',
        'Based on the information you have provided, we have updated your Legal Name format in the PEN system now.',
        'COMPLETE', 'IDIR/MVILLENE', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'NMG',
        'Based on the information you have provided, we have updated your Legal Name format and Gender in the PEN system now.',
        'COMPLETE', 'IDIR/MVILLENE', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));
INSERT INTO PEN_RETRIEVAL_REQUEST_MACRO (PEN_RETRIEVAL_REQUEST_MACRO_ID, MACRO_CODE, MACRO_TEXT, MACRO_TYPE_CODE,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'DOB',
        'Based on the information you have provided, we have updated your Date of Birth in the PEN system now.',
        'COMPLETE', 'IDIR/MVILLENE', to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2020-04-06 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

--Add Columns to PEN_RETRIEVAL_REQUEST
ALTER TABLE PEN_RETRIEVAL_REQUEST
    ADD (
        DEMOG_CHANGED VARCHAR2(1),
        COMPLETE_COMMENT VARCHAR2(4000)
        );

COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.EMAIL_VERIFIED IS 'Short value indicating whether the email of the student has been verified.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.DEMOG_CHANGED IS 'Short value indicating whether the demographic information reported to PEN has been updated when completing PEN Retrieval Requests.';
COMMENT ON COLUMN PEN_RETRIEVAL_REQUEST.COMPLETE_COMMENT IS 'Free text message entered by PEN Staff when completing PEN Retrieval Requests.';

ALTER TABLE API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST RENAME CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_PEN_RETRIEVAL_REQUEST_GENDER_CODE TO PEN_RETRIEVAL_REQUEST_PEN_RETRIEVAL_REQUEST_GENDER_CODE_FK;

ALTER TABLE API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST RENAME CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_PEN_RETRIEVAL_REQUEST_STATUS_CODE TO PEN_RETRIEVAL_REQUEST_PEN_RETRIEVAL_REQUEST_STATUS_CODE_FK;

ALTER TABLE API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_COMMENT RENAME CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_COMMENT_PEN_RETRIEVAL_REQUEST_ID TO PEN_RETRIEVAL_REQUEST_COMMENT_PEN_RETRIEVAL_REQUEST_ID_FK;

ALTER TABLE API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_DOCUMENT RENAME CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_DOCUMENT_PEN_RETRIEVAL_REQUEST_ID TO PEN_RETRIEVAL_REQUEST_DOCUMENT_PEN_RETRIEVAL_REQUEST_ID_FK;

ALTER TABLE API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_DOCUMENT RENAME CONSTRAINT FK_PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE TO PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE_FK;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_DIGITAL_IDENTITY_ID_I RENAME TO PEN_RETRIEVAL_REQUEST_DIGITAL_IDENTITY_ID_IDX;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_COMMENT_PEN_RETRIEVAL_REQUEST_ID_I RENAME TO PEN_RETRIEVAL_REQUEST_COMMENT_PEN_RETRIEVAL_REQUEST_ID_IDX;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_DOCUMENT_PEN_RETRIEVAL_REQUEST_ID_I RENAME TO PEN_RETRIEVAL_REQUEST_DOCUMENT_PEN_RETRIEVAL_REQUEST_ID_IDX;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_DIGITAL_IDENTITY_ID_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_COMMENT_PEN_RETRIEVAL_REQUEST_ID_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_DOCUMENT_PEN_RETRIEVAL_REQUEST_ID_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_PK REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_COMMENT_PK REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_DOCUMENT_TYPE_CODE_PK REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_GENDER_CODE_PK REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_MACRO_PK REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_MACRO_TYPE_CODE_PK REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_STATUS_CODE_PK REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER TABLE API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_MACRO RENAME CONSTRAINT UQ_REQUEST_MACRO_ID_CODE_TYPE TO REQUEST_MACRO_ID_CODE_TYPE_UK;

ALTER INDEX API_PEN_RETRIEVAL.UQ_REQUEST_MACRO_ID_CODE_TYPE RENAME TO REQUEST_MACRO_ID_CODE_TYPE_UK;

ALTER INDEX API_PEN_RETRIEVAL.REQUEST_MACRO_ID_CODE_TYPE_UK REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

CREATE INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_PEN_RETRIEVAL_REQUEST_STATUS_CODE_IDX ON API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST (PEN_RETRIEVAL_REQUEST_STATUS_CODE);
CREATE INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_PEN_IDX ON API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST (PEN);
CREATE INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_LEGAL_LAST_NAME_IDX ON API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST (LEGAL_LAST_NAME);
CREATE INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_LEGAL_FIRST_NAME_IDX ON API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST (LEGAL_FIRST_NAME);
CREATE INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_REVIEWER_IDX ON API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST (REVIEWER);
CREATE INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_INITIAL_SUBMIT_DATE_IDX ON API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST (INITIAL_SUBMIT_DATE);

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_PEN_RETRIEVAL_REQUEST_STATUS_CODE_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_PEN_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_LEGAL_LAST_NAME_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_LEGAL_FIRST_NAME_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_REVIEWER_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;

ALTER INDEX API_PEN_RETRIEVAL.PEN_RETRIEVAL_REQUEST_INITIAL_SUBMIT_DATE_IDX REBUILD LOGGING NOREVERSE TABLESPACE API_PEN_RETRIEVAL_IDX NOCOMPRESS;
