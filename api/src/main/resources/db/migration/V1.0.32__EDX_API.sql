INSERT INTO EDX_ROLE (EDX_ROLE_CODE, LABEL, IS_DISTRICT_ROLE, DESCRIPTION, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('STUDENT_DATA_COLLECTION', 'Student Data Collection', FALSE, 'Student Data Collection (1701) role', 'IDIR/AVSODHI', CURRENT_TIMESTAMP, 'IDIR/AVSODHI', CURRENT_TIMESTAMP);

INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('STUDENT_DATA_COLLECTION', 'Student Data Collection', 'Student Data Collection (1701) permission', 'IDIR/AVSODHI', CURRENT_TIMESTAMP, 'IDIR/AVSODHI', CURRENT_TIMESTAMP);

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'STUDENT_DATA_COLLECTION', 'STUDENT_DATA_COLLECTION', 'IDIR/AVSODHI', CURRENT_TIMESTAMP, 'IDIR/AVSODHI', CURRENT_TIMESTAMP);

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'EDX_SCHOOL_ADMIN', 'STUDENT_DATA_COLLECTION', 'IDIR/AVSODHI', CURRENT_TIMESTAMP, 'IDIR/AVSODHI', CURRENT_TIMESTAMP);

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'EDX_DISTRICT_ADMIN', 'STUDENT_DATA_COLLECTION', 'IDIR/AVSODHI', CURRENT_TIMESTAMP, 'IDIR/AVSODHI', CURRENT_TIMESTAMP);