package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.constants.EventOutcome;
import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.controller.BaseSagaControllerTest;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;


import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.*;
import static ca.bc.gov.educ.api.edx.constants.EventType.*;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
public class MoveSchoolOrchestratorTest extends BaseSagaControllerTest {

    /**
     * The Repository.
     */
    @Autowired
    SagaRepository sagaRepository;

    @Autowired
    private EdxUserRepository edxUserRepository;

    @Autowired
    private EdxUserSchoolRepository edxUserSchoolRepository;

    @Autowired
    private EdxUserDistrictRepository edxUserDistrictRepository;

    @Autowired
    private EdxRoleRepository edxRoleRepository;

    @Autowired
    private EdxPermissionRepository edxPermissionRepository;
    /**
     * The Saga event repository.
     */
    @Autowired
    SagaEventStateRepository sagaEventStateRepository;

    @Autowired
    private SagaService sagaService;

    MoveSchoolData sagaData;

    String sagaPayload;

    private SagaEntity saga;

    @Autowired
    private MessagePublisher messagePublisher;

    @Autowired
    RestUtils restUtils;

    @Autowired
    MoveSchoolOrchestrator orchestrator;

    @Captor
    ArgumentCaptor<byte[]> eventCaptor;

    private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;


    @After
    public void after() {
        sagaEventStateRepository.deleteAll();
        sagaRepository.deleteAll();
        edxUserSchoolRepository.deleteAll();

    }

    @Before
    public void setUp() throws JsonProcessingException {
        MockitoAnnotations.openMocks(this);
        try {
            UUID schoolID = mockUserEntity();
            sagaData = createMoveSchoolSagaData(schoolID);
            sagaPayload = getJsonString(sagaData);
            val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(SagaEnum.MOVE_SCHOOL_SAGA), sagaData);
            saga = sagaService.createSagaRecordInDB(sagaEntity);
            doReturn(List.of(createDummySchool())).when(this.restUtils).getSchoolNumberInDistrict(any(), any(), any());
            doReturn(createDummySchool()).when(this.restUtils).createSchool(any(), any());
            doReturn(List.of(createDummySchool())).when(this.restUtils).getSchoolById(any(), any());
            doReturn(createDummySchool()).when(this.restUtils).updateSchool(any(), any());

        } catch (Exception e) {
            throw new SagaRuntimeException(e);
        }
    }

    private MoveSchoolData createMoveSchoolSagaData(UUID schoolID) throws Exception {
        MoveSchoolData sagaData =
                createDummyMoveSchoolSagaData(schoolID);
        return sagaData;
    }

    @Test
    public void testMoveSchool_GivenEventAndSagaData_shouldPostEventToInstituteApi() throws IOException, InterruptedException, TimeoutException {
        final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
        final var event = Event.builder()
            .eventType(INITIATED)
            .eventOutcome(EventOutcome.INITIATE_SUCCESS)
            .sagaId(this.saga.getSagaId())
            .eventPayload(sagaPayload)
            .build();
        this.orchestrator.moveSchool(event, this.saga, this.sagaData);

        verify(this.messagePublisher, atMost(invocations + 1)).dispatchMessage(eq(INSTITUTE_API_TOPIC.toString()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(MOVE_SCHOOL);
        assertThat(newEvent.getEventPayload()).isEqualTo(sagaPayload);
        final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
        assertThat(sagaFromDB).isPresent();
        assertThat(sagaFromDB.get().getSagaState()).isEqualTo(MOVE_SCHOOL.toString());
        final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
        assertThat(sagaStates).hasSize(1);
        assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.INITIATED.toString());
        assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.INITIATE_SUCCESS.toString());
    }

    @Test
    public void testMoveSchoolEvent_GivenEventAndSagaData_ShouldCreateRecordInDB() throws IOException, InterruptedException, TimeoutException {
        final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
        final var event = Event.builder()
            .eventType(MOVE_SCHOOL)
            .eventOutcome(EventOutcome.SCHOOL_MOVED)
            .sagaId(this.saga.getSagaId())
            .eventPayload(sagaPayload)
            .build();
        this.orchestrator.handleEvent(event);

        verify(this.messagePublisher, atMost(invocations + 2)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(CREATE_USERS_AT_NEW_SCHOOL);
        assertThat(newEvent.getEventOutcome()).isEqualTo(USERS_AT_NEW_SCHOOL_CREATED);

        final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
        assertThat(sagaFromDB).isPresent();
        assertThat(sagaFromDB.get().getSagaState()).isEqualTo(CREATE_USERS_AT_NEW_SCHOOL.toString());
        var payload = JsonUtil.getJsonObjectFromString(MoveSchoolData.class, newEvent.getEventPayload());
        assertThat(payload.getMoveDate()).isNotNull();

        final var sagaStates = this.sagaService.findAllSagaStates(this.saga);
        assertThat(sagaStates).hasSize(1);
        assertThat(sagaStates.get(0).getSagaEventState()).isEqualTo(EventType.MOVE_SCHOOL.toString());
        assertThat(sagaStates.get(0).getSagaEventOutcome()).isEqualTo(EventOutcome.SCHOOL_MOVED.toString());
    }

    @Test
    public void testMoveUsersEvent_GivenEventAndSagaData_ShouldCreateRecordInDBAndPostMessageToNatsAndEdxUserSchoolsShouldBeCopiedToNewSchool() throws IOException, InterruptedException, TimeoutException {
        this.edxUserRepository.findAll();

        final var invocations = mockingDetails(this.messagePublisher).getInvocations().size();
        final var event = Event.builder()
                .eventType(INITIATED)
                .eventOutcome(EventOutcome.INITIATE_SUCCESS)
                .sagaId(this.saga.getSagaId())
                .eventPayload(sagaPayload)
                .build();
        this.orchestrator.handleEvent(event);

        verify(this.messagePublisher, atMost(invocations + 2)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
        final var newEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(newEvent.getEventType()).isEqualTo(INITIATED);
        assertThat(newEvent.getEventOutcome()).isEqualTo(INITIATE_SUCCESS);

        final var sagaFromDB = this.sagaService.findSagaById(this.saga.getSagaId());
        assertThat(sagaFromDB).isPresent();
        assertThat(sagaFromDB.get().getSagaState()).isEqualTo(MOVE_SCHOOL.toString());

        final var nextEvent = Event.builder()
                .eventType(MOVE_SCHOOL)
                .eventOutcome(SCHOOL_MOVED)
                .sagaId(this.saga.getSagaId())
                .eventPayload(newEvent.getEventPayload())
                .build();
        this.orchestrator.handleEvent(nextEvent);

        verify(this.messagePublisher, atMost(invocations + 3)).dispatchMessage(eq(this.orchestrator.getTopicToSubscribe()), this.eventCaptor.capture());
        final var nextNewEvent = JsonUtil.getJsonObjectFromString(Event.class, new String(this.eventCaptor.getValue()));
        assertThat(nextNewEvent.getEventType()).isEqualTo(CREATE_USERS_AT_NEW_SCHOOL);
        assertThat(nextNewEvent.getEventOutcome()).isEqualTo(USERS_AT_NEW_SCHOOL_CREATED);

        final List<EdxUserSchoolEntity> edxUserSchoolEntityList = this.edxUserSchoolRepository.findAll();
        assertThat(edxUserSchoolEntityList).hasSize(2);
        assertThat(edxUserSchoolEntityList.get(1).getEdxUserSchoolRoleEntities()).hasSize(1);
    }

    private MoveSchoolData createDummyMoveSchoolSagaData(UUID schoolID) {
        MoveSchoolData moveSchool = new MoveSchoolData();
        moveSchool.setToSchool(createDummySchool());
        moveSchool.setMoveDate(String.valueOf(LocalDateTime.now().minusDays(1).withNano(0)));
        moveSchool.setFromSchoolId(schoolID.toString());
        moveSchool.setCreateUser("Test");
        moveSchool.setUpdateUser("Test");
        return moveSchool;
    }

    private School createDummySchool() {
        School school = new School();
        school.setDistrictId("34bb7566-ff59-653e-f778-2c1a4d669b00");
        school.setSchoolId("be44a3f7-1a04-938e-dcdc-118989f6dd23");
        school.setSchoolNumber("00002");
        school.setDisplayName("Test College");
        school.setSchoolOrganizationCode("TRIMESTER");
        school.setSchoolCategoryCode("FED_BAND");
        school.setFacilityTypeCode("STANDARD");
        school.setGrades(List.of(createSchoolGrade()));
        school.setNeighborhoodLearning(List.of(createNeighborhoodLearning()));
        return school;
    }

    private SchoolGrade createSchoolGrade() {
        SchoolGrade schoolGrade = new SchoolGrade();
        schoolGrade.setSchoolGradeCode("01");
        schoolGrade.setCreateUser("TEST");
        schoolGrade.setUpdateUser("TEST");
        return schoolGrade;
    }
    private NeighborhoodLearning createNeighborhoodLearning() {
        NeighborhoodLearning neighborhoodLearning = new NeighborhoodLearning();
        neighborhoodLearning.setNeighborhoodLearningTypeCode("COMM_USE");
        neighborhoodLearning.setCreateUser("TEST");
        neighborhoodLearning.setUpdateUser("TEST");
        return neighborhoodLearning;
    }

    private UUID mockUserEntity() {
        var entity = this.createUserEntity(this.edxUserRepository, this.edxPermissionRepository, this.edxRoleRepository, this.edxUserSchoolRepository, this.edxUserDistrictRepository);
        return entity.getEdxUserSchoolEntities().stream().toList().get(0).getSchoolID();
    }

}
