ALTER TABLE EDX_ACTIVATION_CODE
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_ACTIVATION_CODE
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_ACTIVATION_ROLE
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_ACTIVATION_ROLE
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_MINISTRY_OWNERSHIP_TEAM
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_MINISTRY_OWNERSHIP_TEAM
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_PERMISSION
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_PERMISSION
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_ROLE
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_ROLE
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_ROLE_PERMISSION
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_ROLE_PERMISSION
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_SAGA
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_SAGA
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_SAGA_EVENT_STATES
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_SAGA_EVENT_STATES
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER_DISTRICT
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER_DISTRICT
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER_DISTRICT_ROLE
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER_DISTRICT_ROLE
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER_SCHOOL
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER_SCHOOL
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER_SCHOOL_ROLE
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE EDX_USER_SCHOOL_ROLE
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE_COMMENT
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE_COMMENT
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE_DOCUMENT
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE_DOCUMENT
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE_NOTE
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE_NOTE
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE_STUDENT
    ALTER COLUMN CREATE_USER TYPE VARCHAR(100);

ALTER TABLE SECURE_EXCHANGE_STUDENT
    ALTER COLUMN UPDATE_USER TYPE VARCHAR(100);
