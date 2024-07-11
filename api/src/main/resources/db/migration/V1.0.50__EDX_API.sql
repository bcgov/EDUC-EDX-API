UPDATE EDX_ROLE
SET LABEL = 'View Student Data Collection',
DESCRIPTION = 'Read-only Student Data Collection (1701) role for District.'
WHERE EDX_ROLE_CODE = 'DIS_SDC_RO';

UPDATE EDX_ROLE
SET LABEL = 'View Student Data Collection',
DESCRIPTION = 'Read-only Student Data Collection (1701) role for School.'
WHERE EDX_ROLE_CODE = 'SCH_SDC_RO';
