INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'GRAD_DIS_ADMIN', 'GRAD_SCH_RPT_VIEW', 'IDIR/EECKERMA',
        to_date('2025-03-07', 'YYYY-MM-DD'), 'IDIR/EECKERMA', to_date('2025-03-07', 'YYYY-MM-DD'));
