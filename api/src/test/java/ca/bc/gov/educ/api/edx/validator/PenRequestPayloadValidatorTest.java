package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import ca.bc.gov.educ.api.edx.model.v1.GenderCodeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.service.v1.PenRequestService;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

public class PenRequestPayloadValidatorTest extends BasePenRequestAPITest {
  private boolean isCreateOperation = false;
  @Mock
  PenRequestRepository repository;
  @Mock
  PenRequestStatusCodeTableRepository penRequestStatusCodeTableRepo;
  @Mock
  GenderCodeTableRepository genderCodeTableRepo;
  @Mock
  PenRequestService service;
  @Autowired
    ApplicationProperties properties;
    @InjectMocks
    PenRequestPayloadValidator penRequestPayloadValidator;
    @Mock
    private PenRequestCommentRepository penRequestCommentRepository;
    @Mock
    private DocumentRepository documentRepository;

    @Before
    public void before() {
      this.service = new PenRequestService(this.repository, this.penRequestCommentRepository, this.documentRepository, this.penRequestStatusCodeTableRepo, this.genderCodeTableRepo);
      this.penRequestPayloadValidator = new PenRequestPayloadValidator(this.service, this.properties);
    }

    @Test
    public void testValidateGenderCode_WhenGenderCodeDoesNotExistInDB_ShouldAddAnErrorTOTheReturnedList() {
      this.isCreateOperation = true;
        final List<FieldError> errorList = new ArrayList<>();
        when(this.service.getGenderCodesList()).thenReturn(new ArrayList<>());
        final PenRequest penRequest = this.getPenRequestEntityFromJsonString();
      this.penRequestPayloadValidator.validateGenderCode(penRequest, errorList);
        assertEquals(1, errorList.size());
        assertEquals("Invalid Gender Code.", errorList.get(0).getDefaultMessage());
    }

    @Test
    public void testValidateGenderCode_WhenGenderCodeExistInDBAndIsNotEffective_ShouldAddAnErrorTOTheReturnedList() {
      this.isCreateOperation = true;
        final List<FieldError> errorList = new ArrayList<>();
        final List<GenderCodeEntity> genderCodeEntities = new ArrayList<>();
        final GenderCodeEntity entity = this.createGenderCodeData();
        entity.setEffectiveDate(LocalDateTime.MAX);
        genderCodeEntities.add(entity);
        when(this.service.getGenderCodesList()).thenReturn(genderCodeEntities);
        final PenRequest penRequest = this.getPenRequestEntityFromJsonString();
      this.penRequestPayloadValidator.validateGenderCode(penRequest, errorList);
        assertEquals(1, errorList.size());
        assertEquals("Gender Code provided is not yet effective.", errorList.get(0).getDefaultMessage());
    }

    @Test
    public void testValidateGenderCode_WhenGenderCodeExistInDBAndIsExpired_ShouldAddAnErrorTOTheReturnedList() {
      this.isCreateOperation = true;
        final List<FieldError> errorList = new ArrayList<>();
        final List<GenderCodeEntity> genderCodeEntities = new ArrayList<>();
        final GenderCodeEntity entity = this.createGenderCodeData();
        entity.setExpiryDate(LocalDateTime.MIN);
        genderCodeEntities.add(entity);
        when(this.service.getGenderCodesList()).thenReturn(genderCodeEntities);
        final PenRequest penRequest = this.getPenRequestEntityFromJsonString();
      this.penRequestPayloadValidator.validateGenderCode(penRequest, errorList);
        assertEquals(1, errorList.size());
        assertEquals("Gender Code provided has expired.", errorList.get(0).getDefaultMessage());
    }

    @Test
    public void testValidatePayload_GivenPenRequestIDInCreate_ShouldAddAnErrorTOTheReturnedList() {
      this.isCreateOperation = true;
        final List<GenderCodeEntity> genderCodeEntities = new ArrayList<>();
        genderCodeEntities.add(this.createGenderCodeData());
        when(this.service.getGenderCodesList()).thenReturn(genderCodeEntities);
        final PenRequest penRequest = this.getPenRequestEntityFromJsonString();
        penRequest.setPenRequestID(UUID.randomUUID().toString());
        final List<FieldError> errorList = this.penRequestPayloadValidator.validatePayload(penRequest, true);
        assertEquals(1, errorList.size());
        assertEquals("penRequestID should be null for post operation.", errorList.get(0).getDefaultMessage());
    }

    @Test
    public void testValidatePayload_GivenInitialSubmitDateInCreate_ShouldAddAnErrorTOTheReturnedList() {
      this.isCreateOperation = true;
        final List<GenderCodeEntity> genderCodeEntities = new ArrayList<>();
        genderCodeEntities.add(this.createGenderCodeData());
        when(this.service.getGenderCodesList()).thenReturn(genderCodeEntities);
        final PenRequest penRequest = this.getPenRequestEntityFromJsonString();
        penRequest.setInitialSubmitDate(LocalDateTime.now().toString());
        final List<FieldError> errorList = this.penRequestPayloadValidator.validatePayload(penRequest, true);
        assertEquals(1, errorList.size());
        assertEquals("initialSubmitDate should be null for post operation.", errorList.get(0).getDefaultMessage());
    }

    @Test
    public void testValidatePayload_WhenBCSCAutoMatchIsInvalid_ShouldAddAnErrorTOTheReturnedList() {
      this.isCreateOperation = true;
        final List<GenderCodeEntity> genderCodeEntities = new ArrayList<>();
        genderCodeEntities.add(this.createGenderCodeData());
        when(this.service.getGenderCodesList()).thenReturn(genderCodeEntities);
        final PenRequest penRequest = this.getPenRequestEntityFromJsonString();
        penRequest.setBcscAutoMatchOutcome("junk");
        final List<FieldError> errorList = this.penRequestPayloadValidator.validatePayload(penRequest, true);
        assertEquals(1, errorList.size());
        assertEquals("Invalid bcscAutoMatchOutcome. It should be one of :: [RIGHTPEN, WRONGPEN, NOMATCH, MANYMATCHES, ONEMATCH]", errorList.get(0).getDefaultMessage());
    }

    private GenderCodeEntity createGenderCodeData() {
        return GenderCodeEntity.builder().genderCode("M").description("Male")
                .effectiveDate(LocalDateTime.now()).expiryDate(LocalDateTime.MAX).displayOrder(1).label("label").createDate(LocalDateTime.now())
                .updateDate(LocalDateTime.now()).createUser("TEST").updateUser("TEST").build();
    }

    protected String dummyPenRequestJson() {
        return "{\"digitalID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"}";
    }

    protected PenRequest getPenRequestEntityFromJsonString() {
        try {
            return new ObjectMapper().readValue(this.dummyPenRequestJson(), PenRequest.class);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
