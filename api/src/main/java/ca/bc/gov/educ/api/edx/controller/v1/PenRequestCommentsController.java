package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.PenRequestCommentEndpoint;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestCommentsMapper;
import ca.bc.gov.educ.api.edx.service.v1.PenRequestCommentService;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestComments;
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
  private final PenRequestCommentService penRequestCommentService;

  PenRequestCommentsController(@Autowired final PenRequestCommentService penRequestCommentService) {
    this.penRequestCommentService = penRequestCommentService;
  }

  @Override
  public List<PenRequestComments> retrieveComments(String penRequestId) {
    return getPenRequestCommentService().retrieveComments(UUID.fromString(penRequestId)).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public PenRequestComments save(String penRequestId, PenRequestComments penRequestComments) {
    setAuditColumns(penRequestComments);
    return mapper.toStructure(getPenRequestCommentService().save(UUID.fromString(penRequestId), mapper.toModel(penRequestComments)));
  }
}
