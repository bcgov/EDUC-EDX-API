package ca.bc.gov.educ.api.edx.service.event;

import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import ca.bc.gov.educ.api.edx.repository.EdxPermissionRepository;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.event.SchoolUpdateEventDelegatorService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;
import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventType.UPDATE_SCHOOL;
import static org.assertj.core.api.Assertions.assertThat;

public class SchoolUpdateEventDelegatorServiceTest extends BaseEdxAPITest {

    @Autowired
    private SchoolUpdateEventDelegatorService schoolUpdateEventDelegatorService;
    @Autowired
    private EdxRoleRepository edxRoleRepository;
    @Autowired
    private EdxPermissionRepository edxPermissionRepository;
    @Autowired
    private EdxUserRepository edxUserRepository;
    @Autowired
    private EdxUserSchoolRepository edxUserSchoolRepository;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testHandleEvent_givenEventTypeUPDATE_SCHOOL__whenTranscriptEligibilityIsFalseAndSchoolIsOpen_shouldNotUpdateExpiryDate() throws IOException {
        var sagaId = UUID.randomUUID();
        var school = createMockSchoolTombstone();
        school.setCanIssueTranscripts(false);
        school.setClosedDate(null);

        var userEntity = edxUserRepository.save(getEdxUserEntity());
        var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
        var roleEntity = getEdxRoleEntity();
        roleEntity.setEdxRoleCode("EDX_SCHOOL_ADMIN");
        var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
        roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
        edxRoleRepository.save(roleEntity);

        var userSchoolEntity = getEdxUserSchoolEntity(userEntity, UUID.fromString(school.getSchoolId()));
        var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
        userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
        userSchoolEntity.setExpiryDate(null);
        edxUserSchoolRepository.save(userSchoolEntity);

        final Event event = Event.builder().eventType(UPDATE_SCHOOL).sagaId(sagaId).eventPayload(JsonUtil.getJsonStringFromObject(school)).build();
        schoolUpdateEventDelegatorService.handleSchoolUpdateEvent(event);

        var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
        assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
        assertThat(userSchoolEntityAfterUpdate.size()).isEqualTo(1);
        assertThat(userSchoolEntityAfterUpdate.get(0).getExpiryDate()).isNull();
    }

    @Test
    public void testHandleEvent_givenEventTypeUPDATE_SCHOOL__whenTranscriptEligibilityIsTrueAndSchoolIsOpen_shouldNotUpdateExpiryDate() throws IOException {
        var sagaId = UUID.randomUUID();
        var school = createMockSchoolTombstone();
        school.setCanIssueTranscripts(true);
        school.setClosedDate(null);

        var userEntity = edxUserRepository.save(getEdxUserEntity());
        var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
        var roleEntity = getEdxRoleEntity();
        roleEntity.setEdxRoleCode("EDX_SCHOOL_ADMIN");
        var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
        roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
        edxRoleRepository.save(roleEntity);

        var userSchoolEntity = getEdxUserSchoolEntity(userEntity, UUID.fromString(school.getSchoolId()));
        var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
        userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
        userSchoolEntity.setExpiryDate(null);
        edxUserSchoolRepository.save(userSchoolEntity);

        final Event event = Event.builder().eventType(UPDATE_SCHOOL).sagaId(sagaId).eventPayload(JsonUtil.getJsonStringFromObject(school)).build();
        schoolUpdateEventDelegatorService.handleSchoolUpdateEvent(event);

        var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
        assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
        assertThat(userSchoolEntityAfterUpdate.size()).isEqualTo(1);
        assertThat(userSchoolEntityAfterUpdate.get(0).getExpiryDate()).isNull();
    }

    @Test
    public void testHandleEvent_givenEventTypeUPDATE_SCHOOL__whenTranscriptEligibilityIsTrueAndSchoolIsClosed_shouldNotUpdateExpiryDate() throws IOException {
        var sagaId = UUID.randomUUID();
        var schoolCloseDate = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).plusDays(2);
        var school = createMockSchoolTombstone();
        school.setCanIssueTranscripts(true);
        school.setClosedDate(String.valueOf(schoolCloseDate));

        var userEntity = edxUserRepository.save(getEdxUserEntity());
        var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
        var roleEntity = getEdxRoleEntity();
        roleEntity.setEdxRoleCode("EDX_SCHOOL_ADMIN");
        var rolePermissionEntity = getEdxRolePermissionEntity(roleEntity, permissionEntity);
        roleEntity.setEdxRolePermissionEntities(Set.of(rolePermissionEntity));
        edxRoleRepository.save(roleEntity);

        var userSchoolEntity = getEdxUserSchoolEntity(userEntity, UUID.fromString(school.getSchoolId()));
        var userSchoolRoleEntity = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity);
        userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity));
        userSchoolEntity.setExpiryDate(null);
        edxUserSchoolRepository.save(userSchoolEntity);

        final Event event = Event.builder().eventType(UPDATE_SCHOOL).sagaId(sagaId).eventPayload(JsonUtil.getJsonStringFromObject(school)).build();
        schoolUpdateEventDelegatorService.handleSchoolUpdateEvent(event);

        var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
        assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
        assertThat(userSchoolEntityAfterUpdate.size()).isEqualTo(1);
        assertThat(userSchoolEntityAfterUpdate.get(0).getExpiryDate()).isNotNull();
        assertThat(userSchoolEntityAfterUpdate.get(0).getExpiryDate()).isEqualTo(schoolCloseDate.plusMonths(3));
    }
}
