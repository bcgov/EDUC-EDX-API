package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.SecureExchangeNoteEndpoint;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeNoteMapper;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeNoteService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeNote;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class SecureExchangeNoteController extends BaseController implements SecureExchangeNoteEndpoint {

  private static final SecureExchangeNoteMapper mapper = SecureExchangeNoteMapper.mapper;
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeNoteService secureExchangeNoteService;

  SecureExchangeNoteController(@Autowired final SecureExchangeNoteService secureExchangeNoteService) {
    this.secureExchangeNoteService = secureExchangeNoteService;
  }

  @Override
  public List<SecureExchangeNote> retrieveNotes(String secureExchangeId) {
    return this.getSecureExchangeNoteService().retrieveNotes(UUID.fromString(secureExchangeId)).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public SecureExchangeNote save(String secureExchangeId, SecureExchangeNote secureExchangeNote) {
    setAuditColumns(secureExchangeNote);
    return mapper.toStructure(this.getSecureExchangeNoteService().save(UUID.fromString(secureExchangeId), mapper.toModel(secureExchangeNote)));
  }
}
