package ca.bc.gov.educ.api.edx.service.event;

import ca.bc.gov.educ.api.edx.BaseEdxAPITest;
import ca.bc.gov.educ.api.edx.repository.EdxPermissionRepository;
import ca.bc.gov.educ.api.edx.repository.EdxRoleRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserSchoolRepository;
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

        final Event event = Event.builder().eventType(UPDATE_SCHOOL.toString()).sagaId(sagaId).eventPayload(JsonUtil.getJsonStringFromObject(school)).build();
        schoolUpdateEventDelegatorService.handleEvent(event);

        var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
        assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
        assertThat(userSchoolEntityAfterUpdate).hasSize(1);
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

        final Event event = Event.builder().eventType(UPDATE_SCHOOL.toString()).sagaId(sagaId).eventPayload(JsonUtil.getJsonStringFromObject(school)).build();
        schoolUpdateEventDelegatorService.handleEvent(event);

        var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
        assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
        assertThat(userSchoolEntityAfterUpdate).hasSize(1);
        assertThat(userSchoolEntityAfterUpdate.get(0).getExpiryDate()).isNull();
    }

    @Test
    public void testHandleEvent_givenEventTypeUPDATE_SCHOOL__whenTranscriptEligibilityIsFalseAndSchoolIsOpenAndUserHasGRADRole_shouldRemoveGRADRole() throws IOException {
        var sagaId = UUID.randomUUID();
        var school = createMockSchoolTombstone();
        school.setCanIssueTranscripts(false);
        school.setClosedDate(null);

        var userEntity = edxUserRepository.save(getEdxUserEntity());
        var permissionEntity = edxPermissionRepository.save(getEdxPermissionEntity());
        var roleEntity1 = getEdxRoleEntity();
        roleEntity1.setEdxRoleCode("EDX_SCHOOL_ADMIN");
        var roleEntity2 = getEdxRoleEntity();
        roleEntity2.setEdxRoleCode("GRAD_SCH_ADMIN");
        var rolePermissionEntity1 = getEdxRolePermissionEntity(roleEntity1, permissionEntity);
        var rolePermissionEntity2 = getEdxRolePermissionEntity(roleEntity2, permissionEntity);
        roleEntity1.setEdxRolePermissionEntities(Set.of(rolePermissionEntity1));
        roleEntity2.setEdxRolePermissionEntities(Set.of(rolePermissionEntity2));
        edxRoleRepository.save(roleEntity1);
        edxRoleRepository.save(roleEntity2);

        var userSchoolEntity = getEdxUserSchoolEntity(userEntity, UUID.fromString(school.getSchoolId()));
        var userSchoolRoleEntity1 = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity1);
        var userSchoolRoleEntity2 = getEdxUserSchoolRoleEntity(userSchoolEntity, roleEntity2);
        userSchoolEntity.setEdxUserSchoolRoleEntities(Set.of(userSchoolRoleEntity1, userSchoolRoleEntity2));
        userSchoolEntity.setExpiryDate(null);
        edxUserSchoolRepository.save(userSchoolEntity);

        var userSchoolEntityBeforeUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
        assertThat(userSchoolEntityBeforeUpdate.get(0).getEdxUserSchoolRoleEntities()).hasSize(2);

        final Event event = Event.builder().eventType(UPDATE_SCHOOL.toString()).sagaId(sagaId).eventPayload(JsonUtil.getJsonStringFromObject(school)).build();
        schoolUpdateEventDelegatorService.handleEvent(event);

        var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
        assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
        assertThat(userSchoolEntityAfterUpdate).hasSize(1);
        assertThat(userSchoolEntityAfterUpdate.get(0).getEdxUserSchoolRoleEntities()).hasSize(1);
    }

    @Test
    public void testHandleEvent_givenEventTypeUPDATE_SCHOOL__whenTranscriptEligibilityIsFalseAndSchoolIsOpenAndUserHasNoGRADRole_shouldNotUpdateRole() throws IOException {
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

        final Event event = Event.builder().eventType(UPDATE_SCHOOL.toString()).sagaId(sagaId).eventPayload(JsonUtil.getJsonStringFromObject(school)).build();
        schoolUpdateEventDelegatorService.handleEvent(event);

        var userSchoolEntityAfterUpdate = edxUserSchoolRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
        assertThat(userSchoolEntityAfterUpdate).isNotEmpty();
        assertThat(userSchoolEntityAfterUpdate).hasSize(1);
        assertThat(userSchoolEntityAfterUpdate.get(0).getEdxUserSchoolRoleEntities()).hasSize(1);
    }
}
