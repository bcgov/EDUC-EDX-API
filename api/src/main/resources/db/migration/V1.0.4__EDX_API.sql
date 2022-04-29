INSERT INTO EDX_PERMISSION (EDX_PERMISSION_ID, NAME, DESCRIPTION, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SECURE_EXCHANGE', 'Secure exchange permission.', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE (EDX_ROLE_ID, NAME, IS_DISTRICT_ROLE, DESCRIPTION, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'SECURE_EXCHANGE', false, 'Secure exchange role.', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_ID, EDX_PERMISSION_ID, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), (select EDX_ROLE_ID from EDX_ROLE where NAME = 'SECURE_EXCHANGE'),
        (select EDX_PERMISSION_ID from EDX_PERMISSION where NAME = 'SECURE_EXCHANGE'), 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));
