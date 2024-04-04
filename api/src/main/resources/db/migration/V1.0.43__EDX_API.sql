INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('EDX_SCHOOL_VIEW', 'EDX School View', 'Permission to view school details and contacts.', 'IDIR/KLAUR', 'IDIR/KLAUR');

INSERT INTO EDX_PERMISSION (EDX_PERMISSION_CODE, LABEL, DESCRIPTION, CREATE_USER, UPDATE_USER)
VALUES ('EDX_DISTRICT_VIEW', 'EDX District View', 'Permission to view district details and contacts.', 'IDIR/KLAUR', 'IDIR/KLAUR');

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, UPDATE_USER)
SELECT gen_random_uuid(), edx_role_code, 'EDX_SCHOOL_VIEW', 'IDIR/KLAUR', 'IDIR/KLAUR' FROM EDX_ROLE;

INSERT INTO EDX_ROLE_PERMISSION (EDX_ROLE_PERMISSION_ID, EDX_ROLE_CODE, EDX_PERMISSION_CODE, CREATE_USER, UPDATE_USER)
SELECT gen_random_uuid(), edx_role_code, 'EDX_DISTRICT_VIEW', 'IDIR/KLAUR', 'IDIR/KLAUR' FROM EDX_ROLE WHERE IS_DISTRICT_ROLE=true;