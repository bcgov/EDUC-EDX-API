INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES ('EDX_USER_ADMIN', 'EDX User Administrator', 'EDX user administrator permission.', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE, UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'EDX_ADMIN', 'EDX_USER_ADMIN', 'IDIR/MVILLENE',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/MVILLENE', to_date('2019-11-07', 'YYYY-MM-DD'));