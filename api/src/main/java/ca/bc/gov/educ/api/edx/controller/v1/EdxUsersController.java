package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.EdxUsersEndpoint;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.mappers.v1.*;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import ca.bc.gov.educ.api.edx.utils.UUIDUtil;
import ca.bc.gov.educ.api.edx.validator.EdxActivationCodePayLoadValidator;
import ca.bc.gov.educ.api.edx.validator.EdxUserPayLoadValidator;

import java.util.Optional;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@Slf4j
public class EdxUsersController extends BaseController implements EdxUsersEndpoint {

  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService service;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserPayLoadValidator edxUserPayLoadValidator;

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodePayLoadValidator edxActivationCodePayLoadValidator;
  private static final MinistryTeamMapper mapper = MinistryTeamMapper.mapper;
  private static final EdxUserMapper userMapper = EdxUserMapper.mapper;

  private static final EdxUserSchoolMapper USER_SCHOOL_MAPPER = EdxUserSchoolMapper.mapper;
  private static final EdxUserSchoolRoleMapper USER_SCHOOL_ROLE_MAPPER = EdxUserSchoolRoleMapper.mapper;
  private static final EdxRoleMapper EDX_ROLE_MAPPER = EdxRoleMapper.mapper;

  private static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;


  @Autowired
  EdxUsersController(final EdxUsersService secureExchange, EdxUserPayLoadValidator edxUserPayLoadValidator, EdxActivationCodePayLoadValidator edxActivationCodePayLoadValidator) {
    this.service = secureExchange;
    this.edxUserPayLoadValidator = edxUserPayLoadValidator;
    this.edxActivationCodePayLoadValidator = edxActivationCodePayLoadValidator;
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
  public List<EdxUser> findEdxUsers(Optional<UUID> digitalId, String mincode, String firstName, String lastName) {
    return getService().findEdxUsers(digitalId, mincode,firstName,lastName).stream().map(userMapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public EdxUser createEdxUser(EdxUser edxUser) {
    validatePayload(() -> getEdxUserPayLoadValidator().validateCreateEdxUserPayload(edxUser));
    RequestUtil.setAuditColumnsForCreate(edxUser);
    if(edxUser.getEdxUserSchools() != null) {
      for (var entity : edxUser.getEdxUserSchools()) {
        RequestUtil.setAuditColumnsForCreate(entity);
      }
    }
    if(edxUser.getEdxUserDistricts() != null) {
      for (var entity : edxUser.getEdxUserDistricts()) {
        RequestUtil.setAuditColumnsForCreate(entity);
      }
    }
    return userMapper.toStructure(getService().createEdxUser(userMapper.toModel(edxUser)));
  }

  @Override
  public ResponseEntity<Void> deleteEdxUserById(UUID id) {
    getService().deleteEdxUserById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  public EdxUserSchool createEdxSchoolUser(UUID id, EdxUserSchool edxUserSchool) {
    validatePayload(() -> getEdxUserPayLoadValidator().validateCreateEdxUserSchoolPayload(id, edxUserSchool));
    RequestUtil.setAuditColumnsForCreate(edxUserSchool);
    return USER_SCHOOL_MAPPER.toStructure(getService().createEdxUserSchool(id, USER_SCHOOL_MAPPER.toModel(edxUserSchool)));
  }

  @Override
  public ResponseEntity<Void> deleteEdxSchoolUserById(UUID id, UUID edxUserSchoolId) {
    getService().deleteEdxSchoolUserById(id, edxUserSchoolId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public EdxUserSchoolRole createEdxSchoolUserRole(UUID id, UUID edxUserSchoolId, EdxUserSchoolRole edxUserSchoolRole) {
    validatePayload(() -> getEdxUserPayLoadValidator().validateCreateEdxUserSchoolRolePayload(edxUserSchoolId, edxUserSchoolRole));
    RequestUtil.setAuditColumnsForCreate(edxUserSchoolRole);
    return USER_SCHOOL_ROLE_MAPPER.toStructure(getService().createEdxUserSchoolRole(id, edxUserSchoolId, USER_SCHOOL_ROLE_MAPPER.toModel(edxUserSchoolRole)));
  }

  @Override
  public ResponseEntity<Void> deleteEdxSchoolUserRoleById(UUID id, UUID edxUserSchoolRoleId) {
    getService().deleteEdxSchoolUserRoleById(id, edxUserSchoolRoleId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public List<EdxRole> findAllEdxRoles() {
    return getService().findAllEdxRoles().stream().map(EDX_ROLE_MAPPER::toStructure).collect(Collectors.toList());
  }

  @Override
  public EdxUser activateUser(EdxActivateUser edxActivateUser) {
    RequestUtil.setAuditColumnsForCreateIfBlank(edxActivateUser);
    return userMapper.toStructure(getService().activateSchoolUser(edxActivateUser));
  }

  @Override
  public ResponseEntity<Void> updateIsUrlClicked(EdxActivationCode edxActivationCode) {
    getService().expireUserActivationUrl(UUID.fromString(edxActivationCode.getValidationCode()));
    return ResponseEntity.ok().build();
  }

  @Override
  public EdxActivationCode createActivationCode(EdxActivationCode edxActivationCode) {
    validatePayload(() -> getEdxActivationCodePayLoadValidator().validateEdxActivationCodePayload(edxActivationCode));
    RequestUtil.setAuditColumnsForCreate(edxActivationCode);
    if (!CollectionUtils.isEmpty(edxActivationCode.getEdxActivationRoles())) {
      edxActivationCode.getEdxActivationRoles().forEach(RequestUtil::setAuditColumnsForCreate);
    }
    return EDX_ACTIVATION_CODE_MAPPER.toStructure(getService().createEdxActivationCode(EDX_ACTIVATION_CODE_MAPPER.toModel(edxActivationCode)));
  }

  @Override
  public ResponseEntity<Void> deleteActivationCode(UUID activationCodeId) {
    getService().deleteActivationCode(activationCodeId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public EdxActivationCode findPrimaryEdxActivationCode(String mincode) {
    return EDX_ACTIVATION_CODE_MAPPER.toStructure(getService().findPrimaryEdxActivationCode(mincode));
  }

  @Override
  public EdxActivationCode generateOrRegeneratePrimaryEdxActivationCode(EdxPrimaryActivationCode edxPrimaryActivationCode) {
    return EDX_ACTIVATION_CODE_MAPPER.toStructure(getService().generateOrRegeneratePrimaryEdxActivationCode(edxPrimaryActivationCode));
  }

  private void validatePayload(Supplier<List<FieldError>> validator) {
    val validationResult = validator.get();
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }
}
