UPDATE EDX_ROLE
SET LABEL = 'Edit Student Data Collection',
DESCRIPTION = 'Edit Student Data Collection (1701) role for School.'
WHERE EDX_ROLE_CODE = 'SCHOOL_SDC';

UPDATE EDX_ROLE
SET LABEL = 'Edit Student Data Collection',
DESCRIPTION = 'Edit Student Data Collection (1701) role for District.'
WHERE EDX_ROLE_CODE = 'DISTRICT_SDC';
