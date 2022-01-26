package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.PenRequestCommentEndpoint;
import ca.bc.gov.educ.api.edx.config.mappers.v1.PenRequestCommentsMapper;
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
public class PenRequestCommentsController extends BaseController implements PenRequestCommentEndpoint {

  private static final PenRequestCommentsMapper mapper = PenRequestCommentsMapper.mapper;
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeCommentService secureExchangeCommentService;

  PenRequestCommentsController(@Autowired final SecureExchangeCommentService secureExchangeCommentService) {
    this.secureExchangeCommentService = secureExchangeCommentService;
  }

  @Override
  public List<SecureExchangeComments> retrieveComments(String penRequestId) {
    return this.getSecureExchangeCommentService().retrieveComments(UUID.fromString(penRequestId)).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public SecureExchangeComments save(String penRequestId, SecureExchangeComments secureExchangeComments) {
    setAuditColumns(secureExchangeComments);
    return mapper.toStructure(this.getSecureExchangeCommentService().save(UUID.fromString(penRequestId), mapper.toModel(secureExchangeComments)));
  }
}
