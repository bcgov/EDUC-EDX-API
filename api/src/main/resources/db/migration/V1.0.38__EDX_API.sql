UPDATE EDX_ROLE
SET LABEL = 'School Manager', DESCRIPTION = 'Can edit the details and contacts for the school.'
WHERE EDX_ROLE_CODE = 'EDX_EDIT_SCHOOL';

UPDATE EDX_ROLE
SET LABEL = 'District Manager', DESCRIPTION = 'Can edit the details and contacts for the district and all schools in the district.'
WHERE EDX_ROLE_CODE = 'EDX_EDIT_DISTRICT';

UPDATE EDX_ROLE
SET DESCRIPTION = 'Has access to all EDX features, including managing EDX users for the district and all schools in the district.'
WHERE EDX_ROLE_CODE = 'EDX_DISTRICT_ADMIN';

UPDATE EDX_ROLE
SET DESCRIPTION = 'Has access to all EDX features, including managing EDX users for the school.'
WHERE EDX_ROLE_CODE = 'EDX_SCHOOL_ADMIN';

UPDATE EDX_ROLE
SET LABEL = 'Secure Messaging', DESCRIPTION = 'Can send and receive messages through the Secure Messaging Inbox.'
WHERE EDX_ROLE_CODE = 'SECURE_EXCHANGE_SCHOOL';

UPDATE EDX_ROLE
SET LABEL = 'Secure Messaging', DESCRIPTION = 'Can send and receive messages through the Secure Messaging Inbox.'
WHERE EDX_ROLE_CODE = 'SECURE_EXCHANGE_DISTRICT';
