package ca.bc.gov.educ.api.edx.validator;

import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestMacroTypeCodeEntity;
import ca.bc.gov.educ.api.edx.repository.PenRequestMacroRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestMacroTypeCodeRepository;
import ca.bc.gov.educ.api.edx.service.v1.PenRequestMacroService;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestMacro;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PenRequestMacroPayloadValidatorTest extends BasePenRequestAPITest {

  @Autowired
  PenRequestMacroTypeCodeRepository penRequestMacroTypeCodeRepository;

  @Mock
  PenRequestMacroRepository penRequestMacroRepository;

  @Autowired
  PenRequestMacroService penRequestMacroService;
  @InjectMocks
  PenRequestMacroPayloadValidator penRequestMacroPayloadValidator;

  @Before
  public void before() {
    this.penRequestMacroTypeCodeRepository.deleteAll();
    this.penRequestMacroService = new PenRequestMacroService(this.penRequestMacroRepository, this.penRequestMacroTypeCodeRepository);
    this.penRequestMacroPayloadValidator = new PenRequestMacroPayloadValidator(this.penRequestMacroService);
  }

  @Test
  public void testValidatePayload_WhenMacroIdGivenForPost_ShouldAddAnErrorTOTheReturnedList() {
    val errorList = this.penRequestMacroPayloadValidator.validatePayload(this.getPenRequestMacroEntityFromJsonString(), true);
    assertEquals(2, errorList.size());
    assertEquals("macroId should be null for post operation.", errorList.get(0).getDefaultMessage());
  }
  @Test
  public void testValidatePayload_WhenMacroTypeCodeIsInvalid_ShouldAddAnErrorTOTheReturnedList() {
    val entity = this.getPenRequestMacroEntityFromJsonString();
    entity.setMacroId(null);
    val errorList = this.penRequestMacroPayloadValidator.validatePayload(entity, true);
    assertEquals(1, errorList.size());
    assertEquals("macroTypeCode Invalid.", errorList.get(0).getDefaultMessage());
  }

  @Test
  public void testValidatePayload_WhenMacroTypeCodeIsNotEffective_ShouldAddAnErrorTOTheReturnedList() {
    val macroTypeCode = this.createPenReqMacroTypeCode();
    macroTypeCode.setEffectiveDate(LocalDate.MAX);
    this.penRequestMacroTypeCodeRepository.save(macroTypeCode);
    val entity = this.getPenRequestMacroEntityFromJsonString();
    val errorList = this.penRequestMacroPayloadValidator.validatePayload(entity, false);
    assertEquals(1, errorList.size());
    assertEquals("macroTypeCode is not yet effective.", errorList.get(0).getDefaultMessage());
  }
  @Test
  public void testValidatePayload_WhenMacroTypeCodeIsExpired_ShouldAddAnErrorTOTheReturnedList() {
    final PenRequestMacroTypeCodeEntity macroTypeCode = this.createPenReqMacroTypeCode();
    macroTypeCode.setEffectiveDate(LocalDate.now());
    macroTypeCode.setExpiryDate(LocalDate.now().minusDays(1));
    this.penRequestMacroTypeCodeRepository.save(macroTypeCode);
    val entity = this.getPenRequestMacroEntityFromJsonString();
    val errorList = this.penRequestMacroPayloadValidator.validatePayload(entity, false);
    assertEquals(1, errorList.size());
    assertEquals("macroTypeCode is expired.", errorList.get(0).getDefaultMessage());
  }
  private PenRequestMacroTypeCodeEntity createPenReqMacroTypeCode() {
    return PenRequestMacroTypeCodeEntity.builder()
            .createDate(LocalDateTime.now())
            .updateDate(LocalDateTime.now())
            .createUser("TEST")
            .updateUser("TEST")
            .description("TEST")
            .displayOrder(1)
            .effectiveDate(LocalDate.MIN)
            .expiryDate(LocalDate.MAX)
            .label("TEST")
            .macroTypeCode("REJECT")
            .build();
  }

  protected String dummyPenRequestMacroJson() {
    return " {\n" +
            "    \"createUser\": \"om\",\n" +
            "    \"updateUser\": \"om\",\n" +
            "    \"macroId\": \"7f000101-7151-1d84-8171-5187006c0000\",\n" +
            "    \"macroCode\": \"hi\",\n" +
            "    \"macroTypeCode\": \"REJECT\",\n" +
            "    \"macroText\": \"hello\"\n" +
            "  }";
  }

  protected PenRequestMacro getPenRequestMacroEntityFromJsonString() {
    try {
      return new ObjectMapper().readValue(this.dummyPenRequestMacroJson(), PenRequestMacro.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
