package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.EdxUsersEndpoint;
import ca.bc.gov.educ.api.edx.mappers.v1.*;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.UUIDUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

  private static final EdxUserSchoolMapper USER_SCHOOL_MAPPER = EdxUserSchoolMapper.mapper;
  private static final EdxUserSchoolRoleMapper USER_SCHOOL_ROLE_MAPPER = EdxUserSchoolRoleMapper.mapper;

  private static final EdxUserDistrictMapper EDX_USER_DISTRICT_MAPPER = EdxUserDistrictMapper.mapper;
  private static final EdxUserDistrictRoleMapper EDX_USER_DISTRICT_ROLE_MAPPER = EdxUserDistrictRoleMapper.mapper;
  private static final EdxRoleMapper EDX_ROLE_MAPPER = EdxRoleMapper.mapper;


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

  @Override
  public EdxUser createEdxUser(EdxUser edxUser) {
    return userMapper.toStructure(getService().createEdxUser(userMapper.toModel(edxUser)));
  }

  @Override
  public ResponseEntity<Void> deleteEdxUserById(UUID edxUserId) {

    getService().deleteEdxUserById(edxUserId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public EdxUserSchool createEdxSchoolUser(UUID edxUserId, EdxUserSchool edxUserSchool) {
    return USER_SCHOOL_MAPPER.toStructure(getService().createEdxUserSchool(edxUserId,USER_SCHOOL_MAPPER.toModel(edxUserSchool)));
  }

  @Override
  public ResponseEntity<Void> deleteEdxSchoolUserById(UUID edxUserId, UUID edxUserSchoolId) {

   getService().deleteEdxSchoolUserById(edxUserId, edxUserSchoolId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public EdxUserSchoolRole createEdxSchoolUserRole(UUID edxUserId, EdxUserSchoolRole edxUserSchoolRole) {
    return USER_SCHOOL_ROLE_MAPPER.toStructure(getService().createEdxUserSchoolRole(edxUserId,USER_SCHOOL_ROLE_MAPPER.toModel(edxUserSchoolRole)));
  }

  @Override
  public ResponseEntity<Void> deleteEdxSchoolUserRoleById(UUID id, UUID edxUserSchoolRoleId) {
    getService().deleteEdxSchoolUserRoleById(id,edxUserSchoolRoleId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public List<EdxRole> findAllEdxRoles() {
    return getService().findAllEdxRoles().stream().map(EDX_ROLE_MAPPER::toStructure).collect(Collectors.toList());
  }

  /*@Override
  public EdxUserDistrict createEdxDistrictUser(EdxUserDistrict edxUserDistrict) {
    return edxUserDistrictMapper.toStructure(getService().createEdxUserDistrict(edxUserDistrictMapper.toModel(edxUserDistrict)));  }

  @Override
  public ResponseEntity<Void> deleteEdxDistrictUserById(UUID id) {
    return null;
  }

  @Override
  public EdxUserDistrictRole createEdxDistrictUserRole(EdxUserDistrictRole edxUserDistrictRoleDistrict) {
    return null;
  }

  @Override
  public ResponseEntity<Void> deleteEdxDistrictUserRoleById(UUID id) {
    return null;
  }*/
}

