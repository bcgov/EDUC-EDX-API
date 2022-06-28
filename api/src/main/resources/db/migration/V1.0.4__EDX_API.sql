INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('SECURE_EXCHANGE', 'Secure Exchange', 'Secure exchange permission.', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE (EDX_ROLE_CODE, IS_DISTRICT_ROLE, DESCRIPTION, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('SECURE_EXCHANGE', false, 'Secure exchange role.', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SECURE_EXCHANGE', 'SECURE_EXCHANGE', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));
