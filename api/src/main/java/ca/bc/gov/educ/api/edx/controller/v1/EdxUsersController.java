package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.EdxUsersEndpoint;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.SecureExchangeRuntimeException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.mappers.v1.*;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import ca.bc.gov.educ.api.edx.utils.UUIDUtil;
import ca.bc.gov.educ.api.edx.validator.EdxActivationCodePayLoadValidator;
import ca.bc.gov.educ.api.edx.validator.EdxPrimaryActivationCodeValidator;
import ca.bc.gov.educ.api.edx.validator.EdxUserPayLoadValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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

  @Getter(AccessLevel.PRIVATE)
  private final EdxPrimaryActivationCodeValidator edxPrimaryActivationCodeValidator;

  private final ApplicationProperties props;

  private static final MinistryTeamMapper mapper = MinistryTeamMapper.mapper;
  private static final EdxUserMapper userMapper = EdxUserMapper.mapper;

  private static final EdxUserSchoolMapper USER_SCHOOL_MAPPER = EdxUserSchoolMapper.mapper;

  private static final EdxUserDistrictMapper USER_DISTRICT_MAPPER = EdxUserDistrictMapper.mapper;
  private static final EdxUserSchoolRoleMapper USER_SCHOOL_ROLE_MAPPER = EdxUserSchoolRoleMapper.mapper;

  private static final EdxUserDistrictRoleMapper USER_DISTRICT_ROLE_MAPPER = EdxUserDistrictRoleMapper.mapper;
  private static final EdxRoleMapper EDX_ROLE_MAPPER = EdxRoleMapper.mapper;

  private static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;

  @Autowired
  EdxUsersController(final EdxUsersService secureExchange, EdxUserPayLoadValidator edxUserPayLoadValidator, EdxActivationCodePayLoadValidator edxActivationCodePayLoadValidator, EdxPrimaryActivationCodeValidator edxPrimaryActivationCodeValidator, ApplicationProperties props) {
    this.service = secureExchange;
    this.edxUserPayLoadValidator = edxUserPayLoadValidator;
    this.edxActivationCodePayLoadValidator = edxActivationCodePayLoadValidator;
    this.edxPrimaryActivationCodeValidator = edxPrimaryActivationCodeValidator;
    this.props = props;
  }

  @Override
  public List<MinistryTeam> findAllMinistryTeams() {
    return getService().getMinistryTeamsList().stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public List<String> findAllEdxUserSchoolIDs(String permissionCode) {
    return getService().getEdxUserSchoolsList(permissionCode);
  }

  @Override
  public List<String> findAllEdxUserDistrictIDs(String permissionCode) {
    return getService().getEdxUserDistrictsList(permissionCode);
  }

  @Override
  public EdxUser retrieveEdxUser(String id) {
    return userMapper.toStructure(getService().retrieveEdxUserByID(UUIDUtil.fromString(id)));
  }

  @Override
  public List<EdxUser> findEdxUsers(Optional<UUID> digitalId, Optional<UUID> schoolID, String firstName, String lastName, Optional<UUID> districtID) {
    return getService().findEdxUsers(digitalId, schoolID, firstName, lastName, districtID).stream().map(userMapper::toStructure).collect(Collectors.toList());
  }

  @Override
  public EdxUser createEdxUser(EdxUser edxUser) {
    validatePayload(() -> getEdxUserPayLoadValidator().validateCreateEdxUserPayload(edxUser));
    RequestUtil.setAuditColumnsForCreate(edxUser);
    updateAuditColumnsForUserSchoolAndRoles(edxUser);
    updateAuditColumnsForUserDistrictAndRoles(edxUser);
    return userMapper.toStructure(getService().createEdxUser(userMapper.toModel(edxUser)));
  }

  private void updateAuditColumnsForUserDistrictAndRoles(EdxUser edxUser) {
    if (!CollectionUtils.isEmpty(edxUser.getEdxUserDistricts())) {
      for (var edxUserDistrict : edxUser.getEdxUserDistricts()) {
        RequestUtil.setAuditColumnsForCreate(edxUserDistrict);
        if (!CollectionUtils.isEmpty(edxUserDistrict.getEdxUserDistrictRoles())) {
          for (var role : edxUserDistrict.getEdxUserDistrictRoles()) {
            RequestUtil.setAuditColumnsForCreate(role);
          }
        }
      }
    }
  }

  private void updateAuditColumnsForUserSchoolAndRoles(EdxUser edxUser) {
    if (!CollectionUtils.isEmpty(edxUser.getEdxUserSchools())) {
      for (var edxUserSchool : edxUser.getEdxUserSchools()) {
        RequestUtil.setAuditColumnsForCreate(edxUserSchool);
        if (!CollectionUtils.isEmpty(edxUserSchool.getEdxUserSchoolRoles())) {
          for (var role : edxUserSchool.getEdxUserSchoolRoles()) {
            RequestUtil.setAuditColumnsForCreate(role);
          }
        }
      }
    }
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
    if (!CollectionUtils.isEmpty(edxUserSchool.getEdxUserSchoolRoles())) {
      edxUserSchool.getEdxUserSchoolRoles().forEach(RequestUtil::setAuditColumnsForCreate);
    }
    return USER_SCHOOL_MAPPER.toStructure(getService().createEdxUserSchool(id, USER_SCHOOL_MAPPER.toModel(edxUserSchool)));
  }

  @Override
  public EdxUserSchool updateEdxUserSchool(UUID id, EdxUserSchool edxUserSchool) {
    validatePayload(() -> getEdxUserPayLoadValidator().validateEdxUserSchoolPayload(id, edxUserSchool, false));
    RequestUtil.setAuditColumnsForUpdate(edxUserSchool);
    return USER_SCHOOL_MAPPER.toStructure(getService().updateEdxUserSchool(id, USER_SCHOOL_MAPPER.toModel(edxUserSchool)));
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
  public List<EdxRole> findAllEdxRoles(InstituteTypeCode instituteTypeCode) {
    if (instituteTypeCode == null) {
      return getService().findAllEdxRoles().stream().map(EDX_ROLE_MAPPER::toStructure).collect(Collectors.toList());
    } else {
      return getService().findAllEdxRolesForInstituteTypeCode(instituteTypeCode).stream().filter(edxRoleEntity -> {
        var allowRolesList = props.getAllowRolesList();
        return allowRolesList.contains(edxRoleEntity.getEdxRoleCode());
      }).map(EDX_ROLE_MAPPER::toStructure).collect(Collectors.toList());
    }
  }

  @Override
  public EdxUser activateUser(EdxActivateUser edxActivateUser) {
    validatePayload(() -> getEdxActivationCodePayLoadValidator().validateEdxActivateUserPayload(edxActivateUser));
    RequestUtil.setAuditColumnsForCreateIfBlank(edxActivateUser);
    return userMapper.toStructure(getService().activateEdxUser(edxActivateUser));
  }

  @Override
  public InstituteTypeCode updateIsUrlClicked(EdxActivationCode edxActivationCode) {
    return getService().expireUserActivationUrl(UUID.fromString(edxActivationCode.getValidationCode()));
  }

  @Override
  public EdxActivationCode createActivationCode(EdxActivationCode edxActivationCode)  {
    validatePayload(() -> getEdxActivationCodePayLoadValidator().validateEdxActivationCodePayload(edxActivationCode));
    RequestUtil.setAuditColumnsForCreate(edxActivationCode);
    if (!CollectionUtils.isEmpty(edxActivationCode.getEdxActivationRoles())) {
      edxActivationCode.getEdxActivationRoles().forEach(RequestUtil::setAuditColumnsForCreate);
    }
    try {
      return EDX_ACTIVATION_CODE_MAPPER.toStructure(getService().createEdxActivationCode(EDX_ACTIVATION_CODE_MAPPER.toModel(edxActivationCode)));
    } catch (NoSuchAlgorithmException e) {
      throw new SecureExchangeRuntimeException(e.getMessage());
    }
  }

  @Override
  public ResponseEntity<Void> deleteActivationCodesByUserId(UUID edxUserId) {
    getService().deleteActivationCodesByUserId(edxUserId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public ResponseEntity<Void> deleteActivationCode(UUID activationCodeId) {
    getService().deleteActivationCode(activationCodeId);
    return ResponseEntity.noContent().build();
  }

  @Override
  public EdxActivationCode findPrimaryEdxActivationCode(InstituteTypeCode instituteType, String instituteIdentifier) {
    return EDX_ACTIVATION_CODE_MAPPER.toStructure(getService().findPrimaryEdxActivationCode(instituteType, instituteIdentifier));
  }

  @Override
  public EdxActivationCode generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode instituteType, String instituteIdentifier, EdxPrimaryActivationCode edxPrimaryActivationCode) {
    validatePayload(() -> getEdxPrimaryActivationCodeValidator().validateEdxPrimaryActivationCode(instituteType, instituteIdentifier, edxPrimaryActivationCode));
    return EDX_ACTIVATION_CODE_MAPPER.toStructure(getService().generateOrRegeneratePrimaryEdxActivationCode(instituteType, instituteIdentifier, edxPrimaryActivationCode));
  }

  @Override
  public EdxUserDistrict createEdxDistrictUser(UUID id, EdxUserDistrict edxUserDistrict) {
    validatePayload(() -> getEdxUserPayLoadValidator().validateCreateEdxUserDistrictPayload(id, edxUserDistrict));
    RequestUtil.setAuditColumnsForCreate(edxUserDistrict);
    if (!CollectionUtils.isEmpty(edxUserDistrict.getEdxUserDistrictRoles())) {
      edxUserDistrict.getEdxUserDistrictRoles().forEach(RequestUtil::setAuditColumnsForCreate);
    }
    return USER_DISTRICT_MAPPER.toStructure(getService().createEdxUserDistrict(id, USER_DISTRICT_MAPPER.toModel(edxUserDistrict)));
  }

  @Override
  public EdxUserDistrict updateEdxUserDistrict(UUID id, EdxUserDistrict edxUserDistrict) {
    validatePayload(() -> getEdxUserPayLoadValidator().validateEdxUserDistrictPayload(id, edxUserDistrict, false));
    RequestUtil.setAuditColumnsForUpdate(edxUserDistrict);
    return USER_DISTRICT_MAPPER.toStructure(getService().updateEdxUserSchool(id, USER_DISTRICT_MAPPER.toModel(edxUserDistrict)));
  }

  @Override
  public ResponseEntity<Void> deleteEdxDistrictUserByID(UUID id, UUID edxUserDistrictID) {
    getService().deleteEdxDistrictUserById(id, edxUserDistrictID);
    return ResponseEntity.noContent().build();
  }

  @Override
  public EdxUserDistrictRole createEdxDistrictUserRole(UUID id, UUID edxUserDistrictID, EdxUserDistrictRole edxUserDistrictRole) {
    validatePayload(() -> getEdxUserPayLoadValidator().validateCreateEdxUserDistrictRolePayload(edxUserDistrictID, edxUserDistrictRole));
    RequestUtil.setAuditColumnsForCreate(edxUserDistrictRole);
    return USER_DISTRICT_ROLE_MAPPER.toStructure(getService().createEdxUserDistrictRole(id, edxUserDistrictID, USER_DISTRICT_ROLE_MAPPER.toModel(edxUserDistrictRole)));

  }

  @Override
  public ResponseEntity<Void> deleteEdxDistrictUserRoleByID(UUID id, UUID edxUserDistrictRoleID) {
    getService().deleteEdxDistrictUserRoleById(id, edxUserDistrictRoleID);
    return ResponseEntity.noContent().build();
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
