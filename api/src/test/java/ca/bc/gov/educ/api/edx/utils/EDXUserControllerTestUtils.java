package ca.bc.gov.educ.api.edx.utils;

import ca.bc.gov.educ.api.edx.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("test")
public class EDXUserControllerTestUtils {

    @Autowired
    private EdxUserRepository edxUserRepository;

    @Autowired
    private EdxUserSchoolRepository edxUserSchoolRepository;

    @Autowired
    private EdxRoleRepository edxRoleRepository;

    @Autowired
    private EdxPermissionRepository edxPermissionRepository;

    @Autowired
    private EdxUserDistrictRepository edxUserDistrictRepository;

    @Autowired
    private EdxUserDistrictRoleRepository edxUserDistrictRoleRepository;

    @Autowired
    private EdxUserSchoolRoleRepository edxUserSchoolRoleRepository;

    @Autowired
    private EdxActivationCodeRepository edxActivationCodeRepository;

    @Autowired
    private EdxActivationRoleRepository edxActivationRoleRepository;

    @Autowired
    MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanDB() {
        this.edxActivationRoleRepository.deleteAll();
        this.edxActivationCodeRepository.deleteAll();
        this.ministryOwnershipTeamRepository.deleteAll();
        this.edxUserSchoolRoleRepository.deleteAll();
        this.edxUserDistrictRoleRepository.deleteAll();
        this.edxUserSchoolRepository.deleteAll();
        this.edxUserDistrictRepository.deleteAll();
        this.edxUserRepository.deleteAll();
        this.edxPermissionRepository.deleteAll();
        this.edxRoleRepository.deleteAll();
    }
}
