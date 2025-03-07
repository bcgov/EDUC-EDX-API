DELETE FROM EDX_ROLE_PERMISSION
WHERE
EDX_ROLE_CODE = 'DISTRICT_SDC' AND
EDX_PERMISSION_CODE = 'DIS_SDC_VIEW';

DELETE FROM EDX_ROLE_PERMISSION
WHERE
    EDX_ROLE_CODE = 'DIS_SDC_RO' AND
    EDX_PERMISSION_CODE = 'DIS_SDC_VIEW';

DELETE FROM EDX_ROLE_PERMISSION
WHERE
    EDX_ROLE_CODE = 'DISTRICT_SDC' AND
    EDX_PERMISSION_CODE = 'SCH_SDC_VIEW';

DELETE FROM EDX_ROLE_PERMISSION
WHERE
    EDX_ROLE_CODE = 'DIS_SDC_RO' AND
    EDX_PERMISSION_CODE = 'SCH_SDC_VIEW';

DELETE FROM EDX_ROLE_PERMISSION
WHERE
    EDX_ROLE_CODE = 'SCH_SDC_RO' AND
    EDX_PERMISSION_CODE = 'SCH_SDC_VIEW';

DELETE FROM EDX_ROLE_PERMISSION
WHERE
    EDX_ROLE_CODE = 'DISTRICT_SDC' AND
    EDX_PERMISSION_CODE = 'DIS_SDC_EDIT';

DELETE FROM EDX_ROLE_PERMISSION
WHERE
    EDX_ROLE_CODE = 'SCHOOL_SDC' AND
    EDX_PERMISSION_CODE = 'SCH_SDC_VIEW';

ALTER TABLE EDX_ROLE_PERMISSION
ADD UNIQUE (EDX_ROLE_CODE, EDX_PERMISSION_CODE);

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'DISTRICT_SDC', 'DIS_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'DIS_SDC_RO', 'DIS_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'DISTRICT_SDC', 'SCH_SDC_VIEW', 'IDIR/MVILLENE',
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
VALUES (gen_random_uuid(), 'SCHOOL_SDC', 'SCH_SDC_VIEW', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));
