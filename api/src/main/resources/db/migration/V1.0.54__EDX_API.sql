INSERT INTO EDX_ROLE (EDX_ROLE_CODE, LABEL, IS_DISTRICT_ROLE, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('GRAD_SCH_ADMIN', 'Graduation Administrator', FALSE, 'Graduation Administrator.', 'IDIR/AVSODHI', 'IDIR/AVSODHI');

INSERT INTO EDX_ROLE (EDX_ROLE_CODE, LABEL, IS_DISTRICT_ROLE, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('GRAD_DIS_ADMIN', 'Graduation Administrator', TRUE, 'Graduation Administrator.', 'IDIR/AVSODHI', 'IDIR/AVSODHI');

INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('GRAD_SCH_EDIT', 'GRAD School Edit', 'GRAD edit permission for School.', 'IDIR/AVSODHI', 'IDIR/AVSODHI');

INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('GRAD_DIS_EDIT', 'GRAD District Edit', 'GRAD edit permission for District.', 'IDIR/AVSODHI', 'IDIR/AVSODHI');

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'GRAD_SCH_ADMIN', 'GRAD_SCH_EDIT', 'IDIR/AVSODHI',
        to_date('2024-10-01', 'YYYY-MM-DD'), 'IDIR/AVSODHI', to_date('2024-10-01', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'GRAD_SCH_ADMIN', 'EDX_SCHOOL_VIEW', 'IDIR/AVSODHI',
        to_date('2024-10-01', 'YYYY-MM-DD'), 'IDIR/AVSODHI', to_date('2024-10-01', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'GRAD_DIS_ADMIN', 'GRAD_DIS_EDIT', 'IDIR/AVSODHI',
        to_date('2024-10-01', 'YYYY-MM-DD'), 'IDIR/AVSODHI', to_date('2024-10-01', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'GRAD_DIS_ADMIN', 'EDX_SCHOOL_VIEW', 'IDIR/AVSODHI',
        to_date('2024-10-01', 'YYYY-MM-DD'), 'IDIR/AVSODHI', to_date('2024-10-01', 'YYYY-MM-DD'));

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, CREATE_DATE,
                                 UPDATE_USER, UPDATE_DATE)
VALUES (gen_random_uuid(), 'GRAD_DIS_ADMIN', 'EDX_DISTRICT_VIEW', 'IDIR/AVSODHI',
        to_date('2024-10-01', 'YYYY-MM-DD'), 'IDIR/AVSODHI', to_date('2024-10-01', 'YYYY-MM-DD'));
