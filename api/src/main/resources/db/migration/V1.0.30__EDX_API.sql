INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'EDX_DISTRICT_ADMIN', 'EDX_USER_SCHOOL_ADMIN', 'IDIR/SRATH',
        to_date('2019-11-07', 'YYYY-MM-DD'), 'IDIR/SRATH', to_date('2019-11-07', 'YYYY-MM-DD'));