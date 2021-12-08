package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.PenRequestMacroEndpoint;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestMacroMapper;
import ca.bc.gov.educ.api.edx.service.v1.PenRequestMacroService;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestMacro;
import ca.bc.gov.educ.api.edx.validator.PenRequestMacroPayloadValidator;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@Slf4j
public class PenRequestMacroController extends BaseController implements PenRequestMacroEndpoint {

  private static final PenRequestMacroMapper mapper = PenRequestMacroMapper.mapper;
  @Getter(PRIVATE)
  private final PenRequestMacroService penRequestMacroService;
  @Getter(PRIVATE)
  private final PenRequestMacroPayloadValidator penRequestMacroPayloadValidator;

  @Autowired
  public PenRequestMacroController(PenRequestMacroService penRequestMacroService, PenRequestMacroPayloadValidator penRequestMacroPayloadValidator) {
    this.penRequestMacroService = penRequestMacroService;
    this.penRequestMacroPayloadValidator = penRequestMacroPayloadValidator;
  }

  @Override
  public List<PenRequestMacro> findPenReqMacros(String macroTypeCode) {
    if (StringUtils.isNotBlank(macroTypeCode)) {
      return getPenRequestMacroService().findMacrosByMacroTypeCode(macroTypeCode).stream().map(mapper::toStructure).collect(Collectors.toList());
    }
    return getPenRequestMacroService().findAllMacros().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public PenRequestMacro findPenReqMacroById(UUID macroId) {
    val result = getPenRequestMacroService().getMacro(macroId);
    if (result.isPresent()) {
      return mapper.toStructure(result.get());
    }
    throw new EntityNotFoundException(PenRequestMacro.class, "macroId", macroId.toString());
  }

  @Override
  public PenRequestMacro createPenReqMacro(PenRequestMacro penRequestMacro) {
    validatePayload(penRequestMacro, true);
    setAuditColumns(penRequestMacro);
    return mapper.toStructure(getPenRequestMacroService().createMacro(mapper.toModel(penRequestMacro)));
  }

  @Override
  public PenRequestMacro updatePenReqMacro(UUID macroId, PenRequestMacro penRequestMacro) {
    validatePayload(penRequestMacro, false);
    setAuditColumns(penRequestMacro);
    return mapper.toStructure(getPenRequestMacroService().updateMacro(macroId, mapper.toModel(penRequestMacro)));
  }

  private void validatePayload(PenRequestMacro penRequestMacro, boolean isCreateOperation) {
    val validationResult = getPenRequestMacroPayloadValidator().validatePayload(penRequestMacro, isCreateOperation);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }
}
