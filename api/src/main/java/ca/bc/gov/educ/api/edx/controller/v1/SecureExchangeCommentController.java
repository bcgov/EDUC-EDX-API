package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.SecureExchangeCommentEndpoint;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeCommentMapper;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeCommentService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComment;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class SecureExchangeCommentController extends BaseController implements SecureExchangeCommentEndpoint {

  private static final SecureExchangeCommentMapper mapper = SecureExchangeCommentMapper.mapper;
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeCommentService secureExchangeCommentService;

  SecureExchangeCommentController(@Autowired final SecureExchangeCommentService secureExchangeCommentService) {
    this.secureExchangeCommentService = secureExchangeCommentService;
  }

  @Override
  public List<SecureExchangeComment> retrieveComments(String secureExchangeId) {
    return this.getSecureExchangeCommentService().retrieveComments(UUID.fromString(secureExchangeId)).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public SecureExchangeComment save(String secureExchangeId, SecureExchangeComment secureExchangeComment) {
    setAuditColumns(secureExchangeComment);
    return mapper.toStructure(this.getSecureExchangeCommentService().save(UUID.fromString(secureExchangeId), mapper.toModel(secureExchangeComment)));
  }
}
