package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.EdxUsersEndpoint;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserSchoolMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.MinistryTeamMapper;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.struct.v1.MinistryTeam;
import ca.bc.gov.educ.api.edx.utils.UUIDUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class EdxUsersController extends BaseController implements EdxUsersEndpoint {

  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService service;
  private static final MinistryTeamMapper mapper = MinistryTeamMapper.mapper;
  private static final EdxUserMapper userMapper = EdxUserMapper.mapper;
  private static final EdxUserSchoolMapper userSchoolMapper = EdxUserSchoolMapper.mapper;

  @Autowired
  EdxUsersController(final EdxUsersService secureExchange) {
    this.service = secureExchange;
  }

  @Override
  public List<MinistryTeam> findAllMinistryTeams() {
    return getService().getMinistryTeamsList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public List<String> findAllEdxUserSchoolMincodes(String permissionName) {
    return getService().getEdxUserSchoolsList(permissionName);
  }

  @Override
  public EdxUser retrieveEdxUser(String id) {
    return userMapper.toStructure(getService().retrieveEdxUserByID(UUIDUtil.fromString(id)));
  }

  @Override
  public List<EdxUser> findEdxUsers(UUID digitalId) {
    return getService().findEdxUsers(digitalId).stream().map(userMapper::toStructure).collect(Collectors.toList());
  }
}

