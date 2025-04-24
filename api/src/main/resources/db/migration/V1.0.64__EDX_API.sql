UPDATE EDX_ROLE
SET DESCRIPTION = 'Upload data and view reports related to graduation data collection.'
WHERE EDX_ROLE_CODE = 'GRAD_DIS_ADMIN';

UPDATE EDX_ROLE
SET DESCRIPTION = 'View reports related to graduation data collection.'
WHERE EDX_ROLE_CODE = 'GRAD_DIS_RO';

UPDATE EDX_ROLE
SET DESCRIPTION = 'Upload data and view reports related to graduation data collection.'
WHERE EDX_ROLE_CODE = 'GRAD_SCH_ADMIN';

UPDATE EDX_ROLE
SET DESCRIPTION = 'View reports related to graduation data collection.'
WHERE EDX_ROLE_CODE = 'GRAD_SCH_RO';
