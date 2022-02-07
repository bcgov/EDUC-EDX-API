package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.SecureExchangeCommentEndpoint;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeCommentsMapper;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeCommentService;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComments;
import lombok.AccessLevel;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class SecureExchangeCommentsController extends BaseController implements SecureExchangeCommentEndpoint {

  private static final SecureExchangeCommentsMapper mapper = SecureExchangeCommentsMapper.mapper;
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeCommentService secureExchangeCommentService;

  SecureExchangeCommentsController(@Autowired final SecureExchangeCommentService secureExchangeCommentService) {
    this.secureExchangeCommentService = secureExchangeCommentService;
  }

  @Override
  public List<SecureExchangeComments> retrieveComments(String secureExchangeId) {
    return this.getSecureExchangeCommentService().retrieveComments(UUID.fromString(secureExchangeId)).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public SecureExchangeComments save(String secureExchangeId, SecureExchangeComments secureExchangeComments) {
    setAuditColumns(secureExchangeComments);
    return mapper.toStructure(this.getSecureExchangeCommentService().save(UUID.fromString(secureExchangeId), mapper.toModel(secureExchangeComments)));
  }
}
