INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('SCH_SDC_VIEW', 'School SDC View', 'Student Data Collection (1701) view permission for School.', 'IDIR/MVILLENE', 'IDIR/MVILLENE');

INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('DIS_SDC_VIEW', 'District SDC View', 'Student Data Collection (1701) view permission for District.', 'IDIR/MVILLENE', 'IDIR/MVILLENE');

INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('SCH_SDC_EDIT', 'School SDC Edit', 'Student Data Collection (1701) edit permission for School.', 'IDIR/MVILLENE', 'IDIR/MVILLENE');

INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('DIS_SDC_EDIT', 'District SDC Edit', 'Student Data Collection (1701) edit permission for District.', 'IDIR/MVILLENE', 'IDIR/MVILLENE');

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'SECR_TRES'
AND EDX_PERMISSION_CODE = 'DIS_SDC_RO';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'SECR_TRES'
AND EDX_PERMISSION_CODE = 'SCH_SDC_RO';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'SUPERINT'
AND EDX_PERMISSION_CODE = 'DIS_SDC_RO';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'SUPERINT'
AND EDX_PERMISSION_CODE = 'SCH_SDC_RO';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'DIS_SDC_RO'
  AND EDX_PERMISSION_CODE = 'DIS_SDC_RO';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'DIS_SDC_RO'
  AND EDX_PERMISSION_CODE = 'SCH_SDC_RO';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'SCH_SDC_RO'
  AND EDX_PERMISSION_CODE = 'SCH_SDC_RO';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'DISTRICT_SDC'
  AND EDX_PERMISSION_CODE = 'DISTRICT_SDC';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'DISTRICT_SDC'
  AND EDX_PERMISSION_CODE = 'SCHOOL_SDC';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE = 'SCHOOL_SDC'
  AND EDX_PERMISSION_CODE = 'SCHOOL_SDC';

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SECR_TRES', 'DIS_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SECR_TRES', 'SCH_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SUPERINT', 'DIS_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SUPERINT', 'SCH_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'DIS_SDC_RO', 'DIS_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'DIS_SDC_RO', 'SCH_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SCH_SDC_RO', 'SCH_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'DISTRICT_SDC', 'DIS_SDC_EDIT', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'DISTRICT_SDC', 'DIS_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'DISTRICT_SDC', 'SCH_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SCHOOL_SDC', 'SCH_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));