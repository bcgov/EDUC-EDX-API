UPDATE EDX_ROLE
SET LABEL = 'School Info Administrator', DESCRIPTION = 'Can edit school details and contacts.'
WHERE EDX_ROLE_CODE = 'EDX_EDIT_SCHOOL';

UPDATE EDX_ROLE
SET LABEL = 'District Info Administrator', DESCRIPTION = 'Can edit school and district details and contacts.'
WHERE EDX_ROLE_CODE = 'EDX_EDIT_DISTRICT';

UPDATE EDX_ROLE
SET LABEL = 'EDX District Account Manager', DESCRIPTION = 'Can manage the school and district''s EDX users.'
WHERE EDX_ROLE_CODE = 'EDX_DISTRICT_ADMIN';

UPDATE EDX_ROLE
SET LABEL = 'EDX School Account Manager', DESCRIPTION = 'Can manage the school''s EDX users.'
WHERE EDX_ROLE_CODE = 'EDX_SCHOOL_ADMIN';

UPDATE EDX_ROLE
SET LABEL = 'Secure Messaging', DESCRIPTION = 'Can access the school''s Secure Messaging Inbox.'
WHERE EDX_ROLE_CODE = 'SECURE_EXCHANGE_SCHOOL';

UPDATE EDX_ROLE
SET LABEL = 'Secure Messaging', DESCRIPTION = 'Can access the district''s Secure Messaging Inbox.'
WHERE EDX_ROLE_CODE = 'SECURE_EXCHANGE_DISTRICT';

DELETE FROM EDX_ROLE_PERMISSION
WHERE EDX_ROLE_CODE IN ('EDX_SCHOOL_ADMIN', 'EDX_DISTRICT_ADMIN')
AND EDX_PERMISSION_CODE IN ('EDX_SCHOOL_EDIT', 'EDX_DISTRICT_EDIT', 'SECURE_EXCHANGE', 'STUDENT_DATA_COLLECTION');
