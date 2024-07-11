INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('SCH_SDC_RO', 'School SDC Read-Only', 'Student Data Collection (1701) read-only permission for School.', 'IDIR/MVILLENE', 'IDIR/MVILLENE');

INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('DIS_SDC_RO', 'District SDC Read-Only', 'Student Data Collection (1701) read-only permission for District.', 'IDIR/MVILLENE', 'IDIR/MVILLENE');

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SECR_TRES', 'DIS_SDC_RO', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SECR_TRES', 'SCH_SDC_RO', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SUPERINT', 'SCH_SDC_RO', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SUPERINT', 'DIS_SDC_RO', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));