CREATE TABLE SECURE_EXCHANGE
(
    SECURE_EXCHANGE_ID             RAW(16)              NOT NULL,
    EDX_USER_ID                    RAW(16)              NOT NULL,
    EDX_USER_SCHOOL_ID             RAW(16)              NOT NULL,
    EDX_USER_DISTRICT_ID           RAW(16)              NOT NULL,
    EDX_MINISTRY_OWNERSHIP_TEAM_ID RAW(16)              NOT NULL,
    EDX_MINISTRY_CONTACT_TEAM_ID   RAW(16)              NOT NULL,
    SECURE_EXCHANGE_STATUS_CODE    VARCHAR2(10)         NOT NULL,
    REVIEWER                       VARCHAR2(255),
    IS_READ_BY_MINISTRY            VARCHAR2(1)          NOT NULL,
    IS_READ_BY_CONTACT             VARCHAR2(1)          NOT NULL,
    SUBJECT                        VARCHAR2(4000)       NOT NULL,
    SUBMISSION_TIMESTAMP           DATE                 NOT NULL,
    STATUS_UPDATE_TIMESTAMP        DATE                 NOT NULL,
    CREATE_USER                    VARCHAR2(32)         NOT NULL,
    CREATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                    VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT SECURE_EXCHANGE_ID_PK PRIMARY KEY (SECURE_EXCHANGE_ID)
);

CREATE TABLE SECURE_EXCHANGE_STATUS_CODE
(
    SECURE_EXCHANGE_STATUS_CODE VARCHAR2(10)           NOT NULL,
    LABEL                       VARCHAR2(30),
    DESCRIPTION                 VARCHAR2(255),
    DISPLAY_ORDER               NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE              DATE                   NOT NULL,
    EXPIRY_DATE                 DATE                   NOT NULL,
    CREATE_USER                 VARCHAR2(32)           NOT NULL,
    CREATE_DATE                 DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                 VARCHAR2(32)           NOT NULL,
    UPDATE_DATE                 DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT SECURE_EXCHANGE_STATUS_CODE_PK PRIMARY KEY (SECURE_EXCHANGE_STATUS_CODE)
);

CREATE TABLE SECURE_EXCHANGE_DOCUMENT_TYPE_CODE
(
    SECURE_EXCHANGE_DOCUMENT_TYPE_CODE VARCHAR2(10)           NOT NULL,
    LABEL                              VARCHAR2(30),
    DESCRIPTION                        VARCHAR2(255),
    DISPLAY_ORDER                      NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE                     DATE                   NOT NULL,
    EXPIRY_DATE                        DATE                   NOT NULL,
    CREATE_USER                        VARCHAR2(32)           NOT NULL,
    CREATE_DATE                        DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                        VARCHAR2(32)           NOT NULL,
    UPDATE_DATE                        DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT SECURE_EXCHANGE_DOCUMENT_TYPE_CODE_PK PRIMARY KEY (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE)
);

CREATE TABLE SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE
(
    SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE VARCHAR2(10)           NOT NULL,
    LABEL                                  VARCHAR2(30),
    DESCRIPTION                            VARCHAR2(255),
    DISPLAY_ORDER                          NUMBER DEFAULT 1       NOT NULL,
    EFFECTIVE_DATE                         DATE                   NOT NULL,
    EXPIRY_DATE                            DATE                   NOT NULL,
    CREATE_USER                            VARCHAR2(32)           NOT NULL,
    CREATE_DATE                            DATE   DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                            VARCHAR2(32)           NOT NULL,
    UPDATE_DATE                            DATE   DEFAULT SYSDATE NOT NULL,
    CONSTRAINT SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE_PK PRIMARY KEY (SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE)
);

CREATE TABLE EDX_PERMISSION
(
    EDX_PERMISSION_ID RAW(16)              NOT NULL,
    NAME              VARCHAR2(30)         NOT NULL,
    DESCRIPTION       VARCHAR2(255)        NOT NULL,
    CREATE_USER       VARCHAR2(32)         NOT NULL,
    CREATE_DATE       DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER       VARCHAR2(32)         NOT NULL,
    UPDATE_DATE       DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_PERMISSION_ID_PK PRIMARY KEY (EDX_PERMISSION_ID)
);

CREATE TABLE EDX_USER_SCHOOL
(
    EDX_USER_SCHOOL_ID RAW(16)              NOT NULL,
    MINCODE            VARCHAR2(8)          NOT NULL,
    EDX_USER_ID        RAW(16)              NOT NULL,
    CREATE_USER        VARCHAR2(32)         NOT NULL,
    CREATE_DATE        DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER        VARCHAR2(32)         NOT NULL,
    UPDATE_DATE        DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_USER_SCHOOL_ID_PK PRIMARY KEY (EDX_USER_SCHOOL_ID)
);


CREATE TABLE EDX_USER_DISTRICT
(
    EDX_USER_DISTRICT_ID RAW(16)              NOT NULL,
    DISTRICT_CODE        VARCHAR2(3)          NOT NULL,
    EDX_USER_ID          RAW(16)              NOT NULL,
    CREATE_USER          VARCHAR2(32)         NOT NULL,
    CREATE_DATE          DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER          VARCHAR2(32)         NOT NULL,
    UPDATE_DATE          DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_USER_DISTRICT_ID_PK PRIMARY KEY (EDX_USER_DISTRICT_ID)
);


CREATE TABLE EDX_ROLE_PERMISSION
(
    EDX_ROLE_PERMISSION_ID RAW(16)              NOT NULL,
    EDX_ROLE_ID            RAW(16)              NOT NULL,
    EDX_PERMISSION_ID      RAW(16)              NOT NULL,
    CREATE_USER            VARCHAR2(32)         NOT NULL,
    CREATE_DATE            DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER            VARCHAR2(32)         NOT NULL,
    UPDATE_DATE            DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_ROLE_PERMISSION_ID_PK PRIMARY KEY (EDX_ROLE_PERMISSION_ID)
);


CREATE TABLE EDX_ROLE
(
    EDX_ROLE_ID RAW(16)              NOT NULL,
    NAME        VARCHAR2(30)         NOT NULL,
    DESCRIPTION VARCHAR2(255)        NOT NULL,
    CREATE_USER VARCHAR2(32)         NOT NULL,
    CREATE_DATE DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER VARCHAR2(32)         NOT NULL,
    UPDATE_DATE DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_ROLE_ID_PK PRIMARY KEY (EDX_ROLE_ID)
);

CREATE TABLE EDX_USER
(
    EDX_USER_ID         RAW(16)              NOT NULL,
    FIRST_NAME          VARCHAR2(255)        NOT NULL,
    LAST_NAME           VARCHAR2(255)        NOT NULL,
    DIGITAL_IDENTITY_ID RAW(16)              NOT NULL,
    CREATE_USER         VARCHAR2(32)         NOT NULL,
    CREATE_DATE         DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER         VARCHAR2(32)         NOT NULL,
    UPDATE_DATE         DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_USER_ID_PK PRIMARY KEY (EDX_USER_ID)
);

CREATE TABLE EDX_USER_ROLE
(
    EDX_USER_ROLE_ID RAW(16)              NOT NULL,
    EDX_USER_ID      RAW(16)              NOT NULL,
    EDX_ROLE_ID      RAW(16)              NOT NULL,
    CREATE_USER      VARCHAR2(32)         NOT NULL,
    CREATE_DATE      DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER      VARCHAR2(32)         NOT NULL,
    UPDATE_DATE      DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_USER_ROLE_ID_PK PRIMARY KEY (EDX_USER_ROLE_ID)
);

CREATE TABLE EDX_ACTIVATION_CODE
(
    EDX_ACTIVATION_CODE_ID RAW(16)              NOT NULL,
    MINCODE                VARCHAR2(8)          NOT NULL,
    DISTRICT_CODE          VARCHAR2(3)          NOT NULL,
    ACTIVATION_CODE        VARCHAR2(10)         NOT NULL,
    CREATOR_EDX_USER_ID    RAW(16)              NOT NULL,
    EXPIRY_DATE            DATE DEFAULT SYSDATE NOT NULL,
    CREATE_USER            VARCHAR2(32)         NOT NULL,
    CREATE_DATE            DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER            VARCHAR2(32)         NOT NULL,
    UPDATE_DATE            DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_ACTIVATION_CODE_ID_PK PRIMARY KEY (EDX_ACTIVATION_CODE_ID)
);

CREATE TABLE EDX_ACTIVATION_ROLE
(
    EDX_ACTIVATION_ROLE_ID RAW(16)              NOT NULL,
    EDX_ACTIVATION_CODE_ID RAW(16)              NOT NULL,
    EDX_ROLE_ID            RAW(16)              NOT NULL,
    CREATE_USER            VARCHAR2(32)         NOT NULL,
    CREATE_DATE            DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER            VARCHAR2(32)         NOT NULL,
    UPDATE_DATE            DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_ACTIVATION_ROLE_ID_PK PRIMARY KEY (EDX_ACTIVATION_ROLE_ID)
);

CREATE TABLE EDX_MINISTRY_OWNERSHIP_TEAM
(
    EDX_MINISTRY_OWNERSHIP_TEAM_ID RAW(16)              NOT NULL,
    TEAM_NAME                      VARCHAR2(255)        NOT NULL,
    GROUP_ROLE_IDENTIFIER          VARCHAR2(255)        NOT NULL,
    CREATE_USER                    VARCHAR2(32)         NOT NULL,
    CREATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                    VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                    DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT EDX_MINISTRY_OWNERSHIP_TEAM_ID_PK PRIMARY KEY (EDX_MINISTRY_OWNERSHIP_TEAM_ID)
);

CREATE TABLE SECURE_EXCHANGE_COMMENT
(
    SECURE_EXCHANGE_COMMENT_ID             RAW(16)              NOT NULL,
    SECURE_EXCHANGE_ID                     RAW(16)              NOT NULL,
    COMMENT_USER_IDENTIFIER                RAW(16)              NOT NULL,
    COMMENT_USER_NAME                      VARCHAR2(255),
    SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE VARCHAR2(10)         NOT NULL,
    COMMENT_CONTENT                        VARCHAR2(4000)       NOT NULL,
    COMMENT_TIMESTAMP                      DATE                 NOT NULL,
    CREATE_USER                            VARCHAR2(32)         NOT NULL,
    CREATE_DATE                            DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                            VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                            DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT SECURE_EXCHANGE_COMMENT_ID_PK PRIMARY KEY (SECURE_EXCHANGE_COMMENT_ID)
);


CREATE TABLE SECURE_EXCHANGE_NOTE
(
    SECURE_EXCHANGE_NOTE_ID RAW(16)              NOT NULL,
    SECURE_EXCHANGE_ID      RAW(16)              NOT NULL,
    STAFF_USER_IDENTIFIER   RAW(16)              NOT NULL,
    STAFF_USER_NAME         VARCHAR2(255)        NOT NULL,
    NOTE_CONTENT            VARCHAR2(4000)       NOT NULL,
    NOTE_TIMESTAMP          DATE                 NOT NULL,
    CREATE_USER             VARCHAR2(32)         NOT NULL,
    CREATE_DATE             DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER             VARCHAR2(32)         NOT NULL,
    UPDATE_DATE             DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT SECURE_EXCHANGE_NOTE_ID_PK PRIMARY KEY (SECURE_EXCHANGE_NOTE_ID)
);

CREATE TABLE SECURE_EXCHANGE_DOCUMENT
(
    SECURE_EXCHANGE_DOCUMENT_ID        RAW(16)              NOT NULL,
    SECURE_EXCHANGE_ID                 RAW(16)              NOT NULL,
    SECURE_EXCHANGE_DOCUMENT_TYPE_CODE VARCHAR2(10)         NOT NULL,
    FILE_NAME                          VARCHAR2(255)        NOT NULL,
    FILE_EXTENSION                     VARCHAR2(255),
    FILE_SIZE                          NUMBER,
    DOCUMENT_DATA                      BLOB                 NOT NULL,
    CREATE_USER                        VARCHAR2(32)         NOT NULL,
    CREATE_DATE                        DATE DEFAULT SYSDATE NOT NULL,
    UPDATE_USER                        VARCHAR2(32)         NOT NULL,
    UPDATE_DATE                        DATE DEFAULT SYSDATE NOT NULL,
    CONSTRAINT SECURE_EXCHANGE_DOCUMENT_PK PRIMARY KEY (SECURE_EXCHANGE_DOCUMENT_ID)
)
    TABLESPACE API_EDX_BLOB_DATA;

ALTER TABLE EDX_USER_SCHOOL
    ADD CONSTRAINT FK_EDX_USER_SCHOOL_EDX_USER_ID FOREIGN KEY (EDX_USER_ID) REFERENCES EDX_USER (EDX_USER_ID);

ALTER TABLE EDX_USER_DISTRICT
    ADD CONSTRAINT FK_EDX_USER_DISTRICT_EDX_USER_ID FOREIGN KEY (EDX_USER_ID) REFERENCES EDX_USER (EDX_USER_ID);

ALTER TABLE EDX_ROLE_PERMISSION
    ADD CONSTRAINT FK_EDX_USER_PERMISSION_EDX_ROLE_ID FOREIGN KEY (EDX_ROLE_ID) REFERENCES EDX_ROLE (EDX_ROLE_ID);
ALTER TABLE EDX_ROLE_PERMISSION
    ADD CONSTRAINT FK_EDX_USER_ROLE_EDX_PERMISSION_ID FOREIGN KEY (EDX_PERMISSION_ID) REFERENCES EDX_PERMISSION (EDX_PERMISSION_ID);

ALTER TABLE EDX_ACTIVATION_CODE
    ADD CONSTRAINT FK_EDX_ACTIVATION_CODE_EDX_USER_ID FOREIGN KEY (CREATOR_EDX_USER_ID) REFERENCES EDX_USER (EDX_USER_ID);

ALTER TABLE EDX_USER_ROLE
    ADD CONSTRAINT FK_EDX_USER_ROLE_EDX_USER_ID FOREIGN KEY (EDX_USER_ID) REFERENCES EDX_USER (EDX_USER_ID);
ALTER TABLE EDX_USER_ROLE
    ADD CONSTRAINT FK_EDX_USER_ROLE_EDX_ROLE_ID FOREIGN KEY (EDX_ROLE_ID) REFERENCES EDX_ROLE (EDX_ROLE_ID);

ALTER TABLE EDX_ACTIVATION_ROLE
    ADD CONSTRAINT FK_EDX_ACTIVATION_ROLE_EDX_ROLE_ID FOREIGN KEY (EDX_ROLE_ID) REFERENCES EDX_ROLE (EDX_ROLE_ID);
ALTER TABLE EDX_ACTIVATION_ROLE
    ADD CONSTRAINT FK_EDX_USER_ROLE_EDX_ACTIVATION_CODE_ID FOREIGN KEY (EDX_ACTIVATION_CODE_ID) REFERENCES EDX_ACTIVATION_CODE (EDX_ACTIVATION_CODE_ID);

ALTER TABLE SECURE_EXCHANGE_COMMENT
    ADD CONSTRAINT FK_SECURE_EXCHANGE_COMMENT_SECURE_EXCHANGE_ID FOREIGN KEY (SECURE_EXCHANGE_ID) REFERENCES SECURE_EXCHANGE (SECURE_EXCHANGE_ID);
ALTER TABLE SECURE_EXCHANGE_COMMENT
    ADD CONSTRAINT FK_SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE FOREIGN KEY (SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE) REFERENCES SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE (SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE);

ALTER TABLE SECURE_EXCHANGE_NOTE
    ADD CONSTRAINT FK_SECURE_EXCHANGE_NOTE_SECURE_EXCHANGE_ID FOREIGN KEY (SECURE_EXCHANGE_ID) REFERENCES SECURE_EXCHANGE (SECURE_EXCHANGE_ID);

ALTER TABLE SECURE_EXCHANGE_DOCUMENT
    ADD CONSTRAINT FK_SECURE_EXCHANGE_DOCUMENT_SECURE_EXCHANGE_ID FOREIGN KEY (SECURE_EXCHANGE_ID) REFERENCES SECURE_EXCHANGE (SECURE_EXCHANGE_ID);
ALTER TABLE SECURE_EXCHANGE_DOCUMENT
    ADD CONSTRAINT FK_SECURE_EXCHANGE_DOCUMENT_TYPE_CODE FOREIGN KEY (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE) REFERENCES SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE);


ALTER TABLE SECURE_EXCHANGE
    ADD CONSTRAINT FK_SECURE_EXCHANGE_SECURE_EXCHANGE_STATUS_CODE FOREIGN KEY (SECURE_EXCHANGE_STATUS_CODE) REFERENCES SECURE_EXCHANGE_STATUS_CODE (SECURE_EXCHANGE_STATUS_CODE);
ALTER TABLE SECURE_EXCHANGE
    ADD CONSTRAINT FK_SECURE_EXCHANGE_EDX_MINISTRY_OWNERSHIP_TEAM_ID FOREIGN KEY (EDX_MINISTRY_OWNERSHIP_TEAM_ID) REFERENCES EDX_MINISTRY_OWNERSHIP_TEAM (EDX_MINISTRY_OWNERSHIP_TEAM_ID);
ALTER TABLE SECURE_EXCHANGE
    ADD CONSTRAINT FK_SECURE_EXCHANGE_EDX_MINISTRY_CONTACT_TEAM_ID FOREIGN KEY (EDX_MINISTRY_CONTACT_TEAM_ID) REFERENCES EDX_MINISTRY_OWNERSHIP_TEAM (EDX_MINISTRY_OWNERSHIP_TEAM_ID);
ALTER TABLE SECURE_EXCHANGE
    ADD CONSTRAINT FK_SECURE_EXCHANGE_EDX_USER_ID FOREIGN KEY (EDX_USER_ID) REFERENCES EDX_USER (EDX_USER_ID);
ALTER TABLE SECURE_EXCHANGE
    ADD CONSTRAINT FK_SECURE_EXCHANGE_EDX_USER_SCHOOL_ID FOREIGN KEY (EDX_USER_SCHOOL_ID) REFERENCES EDX_USER_SCHOOL (EDX_USER_SCHOOL_ID);
ALTER TABLE SECURE_EXCHANGE
    ADD CONSTRAINT FK_SECURE_EXCHANGE_EDX_USER_DISTRICT_ID FOREIGN KEY (EDX_USER_DISTRICT_ID) REFERENCES EDX_USER_DISTRICT (EDX_USER_DISTRICT_ID);

INSERT INTO SECURE_EXCHANGE_STATUS_CODE (SECURE_EXCHANGE_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                               EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                               UPDATE_DATE)
VALUES ('NEW', 'New exchange', 'New secure exchange.', 1,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO SECURE_EXCHANGE_STATUS_CODE (SECURE_EXCHANGE_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                         EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                         UPDATE_DATE)
VALUES ('INPROG', 'In Progress', 'Secure exchange in progress.', 2,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO SECURE_EXCHANGE_STATUS_CODE (SECURE_EXCHANGE_STATUS_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                         EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                         UPDATE_DATE)
VALUES ('CLOSED', 'Closed exchange', 'Closed secure exchange.', 3,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE (SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                         EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                         UPDATE_DATE)
VALUES ('IDIR', 'IDIR User', 'IDIR User Type.', 1,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE (SECURE_EXCHANGE_COMMENT_USER_TYPE_CODE, LABEL, DESCRIPTION, DISPLAY_ORDER,
                                         EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER, CREATE_DATE, UPDATE_USER,
                                         UPDATE_DATE)
VALUES ('EDXUSER', 'EDX User', 'EDX User Type.', 2,
        to_date('2020-01-01', 'YYYY-MM-DD'), to_date('2099-12-31', 'YYYY-MM-DD'), 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CABIRTH', 'Canadian Birth Certificate', 'Canadian Birth Certificate', 10,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CAPASSPORT', 'Canadian Passport', 'Canadian Passport', 20,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CADL', 'Canadian Driver''s Licence', 'Canadian Driver''s Licence', 30,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('BCIDCARD', 'Provincial Identification Card', 'Provincial Identification Card', 40,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('BCSCPHOTO', 'BC Services Card w Photo', 'BC Services Card (Photo version only)', 50,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('CACITZCARD', 'Canadian Citizenship Card', 'Canadian Citizenship Card', 60,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('PRCARD', 'Permanent Residence Card', 'Permanent Residence Card', 70,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('STUDENTPMT', 'Student / Study Permit', 'Student / Study Permit', 80,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('IMM5292', 'IMM5292 Conf of Perm Residence', 'Confirmation of Permanent Residence (IMM5292)', 90,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('IMM1000', 'IMM1000 Record of Landing',
        'Canadian Immigration Record of Landing (IMM 1000, not valid after June 2002)', 100,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('INDSTATUS', 'Indian Status Card', 'Indian Status Card', 110,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('NAMECHANGE', 'Legal Name Change document', 'Canadian court order approving legal change of name', 120,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('FORPASSPRT', 'Foreign Passport', 'Foreign Passport', 130,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('ADOPTION', 'Canadian adoption order', 'Canadian adoption order', 140,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('MARRIAGE', 'Marriage Certificate', 'Marriage Certificate', 150,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('FORBIRTH', 'Foreign Birth Certificate', 'Foreign Birth Certificate (with English translation)', 160,
        to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO SECURE_EXCHANGE_DOCUMENT_TYPE_CODE (SECURE_EXCHANGE_DOCUMENT_TYPE_CODE, LABEL, DESCRIPTION,
                                                      DISPLAY_ORDER, EFFECTIVE_DATE, EXPIRY_DATE, CREATE_USER,
                                                      CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('OTHER', 'Other', 'Other document type', 170, to_date('2020-01-01 00:00:00', 'YYYY-MM-DD HH24:MI:SS'),
        to_date('2099-12-31 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'), 'IDIR/MVILLENE',
        to_date('2019-12-20 00:00:00', 'YYYY-MM-DD HH24:MI:SS'));

INSERT INTO EDX_MINISTRY_OWNERSHIP_TEAM (EDX_MINISTRY_OWNERSHIP_TEAM_ID, TEAM_NAME, GROUP_ROLE_IDENTIFIER,
                                         CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (sys_guid(), 'PEN', 'PEN_TEAM_ROLE', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

CREATE INDEX SECURE_EXCHANGE_EDX_USER_ID_I ON SECURE_EXCHANGE (EDX_USER_ID) TABLESPACE API_EDX_IDX;
CREATE INDEX SECURE_EXCHANGE_EDX_USER_SCHOOL_ID_I ON SECURE_EXCHANGE (EDX_USER_SCHOOL_ID) TABLESPACE API_EDX_IDX;
CREATE INDEX SECURE_EXCHANGE_EDX_USER_DISTRICT_ID_I ON SECURE_EXCHANGE (EDX_USER_DISTRICT_ID) TABLESPACE API_EDX_IDX;
CREATE INDEX SECURE_EXCHANGE_EDX_MINISTRY_CONTACT_TEAM_ID_I ON SECURE_EXCHANGE (EDX_MINISTRY_CONTACT_TEAM_ID) TABLESPACE API_EDX_IDX;
CREATE INDEX SECURE_EXCHANGE_EDX_MINISTRY_OWNERSHIP_TEAM_ID_I ON SECURE_EXCHANGE (EDX_MINISTRY_OWNERSHIP_TEAM_ID) TABLESPACE API_EDX_IDX;
CREATE INDEX SECURE_EXCHANGE_COMMENT_SECURE_EXCHANGE_ID_I ON SECURE_EXCHANGE_COMMENT (SECURE_EXCHANGE_ID) TABLESPACE API_EDX_IDX;
CREATE INDEX SECURE_EXCHANGE_DOCUMENT_SECURE_EXCHANGE_ID_I ON SECURE_EXCHANGE_DOCUMENT (SECURE_EXCHANGE_ID) TABLESPACE API_EDX_IDX;

