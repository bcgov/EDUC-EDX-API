package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestMacroEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestMacroTypeCodeEntity;
import ca.bc.gov.educ.api.edx.repository.PenRequestMacroRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestMacroTypeCodeRepository;
import lombok.Getter;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static lombok.AccessLevel.PRIVATE;

@Service
public class PenRequestMacroService {
  @Getter(PRIVATE)
  private final PenRequestMacroRepository penRequestMacroRepository;
  @Getter(PRIVATE)
  private final PenRequestMacroTypeCodeRepository penRequestMacroTypeCodeRepository;

  @Autowired
  public PenRequestMacroService(PenRequestMacroRepository penRequestMacroRepository, PenRequestMacroTypeCodeRepository penRequestMacroTypeCodeRepository) {
    this.penRequestMacroRepository = penRequestMacroRepository;
    this.penRequestMacroTypeCodeRepository = penRequestMacroTypeCodeRepository;
  }


  public Optional<PenRequestMacroTypeCodeEntity> getMacroTypeCode(String code) {
    return getPenRequestMacroTypeCodeRepository().findById(code);
  }

  public List<PenRequestMacroEntity> findAllMacros() {
    return getPenRequestMacroRepository().findAll();
  }

  public Optional<PenRequestMacroEntity> getMacro(UUID macroId) {
    return getPenRequestMacroRepository().findById(macroId);
  }

  public List<PenRequestMacroEntity> findMacrosByMacroTypeCode(String macroTypeCode) {
    return getPenRequestMacroRepository().findAllByMacroTypeCode(macroTypeCode);
  }

  public PenRequestMacroEntity createMacro(PenRequestMacroEntity entity) {
    return getPenRequestMacroRepository().save(entity);
  }

  public PenRequestMacroEntity updateMacro(UUID macroId, PenRequestMacroEntity entity) {
    val result = getPenRequestMacroRepository().findById(macroId);
    if (result.isPresent()) {
      return getPenRequestMacroRepository().save(entity);
    } else {
      throw new EntityNotFoundException(entity.getClass(),"macroId", macroId.toString());
    }
  }
}
