ALTER TABLE EDX_ACTIVATION_CODE
    ADD COLUMN FIRST_NAME VARCHAR(255) NOT NULL,
    ADD COLUMN LAST_NAME  VARCHAR(255) NOT NULL,
    ADD COLUMN EMAIL      VARCHAR(255) NOT NULL;


TRUNCATE TABLE EDX_USER
    CASCADE;

ALTER TABLE EDX_USER
    ADD COLUMN EMAIL VARCHAR(255) NOT NULL;
