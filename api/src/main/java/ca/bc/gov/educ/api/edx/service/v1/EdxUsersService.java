package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.rest.RestUtils;
import ca.bc.gov.educ.api.edx.struct.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.TransformUtil;
import com.google.common.primitives.Chars;
import jakarta.persistence.EntityExistsException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.*;

/**
 * The type Edx users service.
 */
@Service
@Slf4j
public class EdxUsersService {

  @Getter(AccessLevel.PRIVATE)
  private final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserRepository edxUserRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserSchoolRepository edxUserSchoolsRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserDistrictRoleRepository edxUserDistrictRoleRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserDistrictRepository edxUserDistrictRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserSchoolRoleRepository edxUserSchoolRoleRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxRoleRepository edxRoleRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeRepository edxActivationCodeRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationRoleRepository edxActivationRoleRepository;

  private final RestUtils restUtils;

  private static final String EDX_USER_ID = "edxUserID";

  private static final String EDX_ACTIVATION_CODE_ID = "edxActivationCodeId";

  private static final String INSTITUTE_IDENTIFIER = "institute_identifier";

  @Getter(AccessLevel.PRIVATE)
  private final ApplicationProperties props;

  private static final String EDX_USER_DISTRICT_ID="edxUserDistrictID";

  private static final List<String> INDEPENDENT_SCHOOL_CATEGORIES = Arrays.asList("INDEPEND", "INDP_FNS", "FED_BAND");
  private static final String GRAD_SCHOOL_ADMIN_ROLE = "GRAD_SCH_ADMIN";
  private static final String SECURE_EXCHANGE_SCHOOL_ROLE = "SECURE_EXCHANGE_SCHOOL";
  private static final List<String> ALLOWED_ROLES_FOR_CLOSED_TRANSCRIPT_ELIG_SCH = Arrays.asList(SECURE_EXCHANGE_SCHOOL_ROLE);

  @Autowired
  public EdxUsersService(final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository, final EdxUserSchoolRepository edxUserSchoolsRepository, final EdxUserRepository edxUserRepository, EdxUserDistrictRoleRepository edxUserDistrictRoleRepository, EdxUserDistrictRepository edxUserDistrictRepository, EdxUserSchoolRoleRepository edxUserSchoolRoleRepository, EdxRoleRepository edxRoleRepository, EdxActivationCodeRepository edxActivationCodeRepository, EdxActivationRoleRepository edxActivationRoleRepository, RestUtils restUtils, ApplicationProperties props) {
    this.ministryOwnershipTeamRepository = ministryOwnershipTeamRepository;
    this.edxUserSchoolsRepository = edxUserSchoolsRepository;
    this.edxUserRepository = edxUserRepository;
    this.edxUserDistrictRoleRepository = edxUserDistrictRoleRepository;
    this.edxUserDistrictRepository = edxUserDistrictRepository;
    this.edxUserSchoolRoleRepository = edxUserSchoolRoleRepository;
    this.edxRoleRepository = edxRoleRepository;
    this.edxActivationCodeRepository = edxActivationCodeRepository;
    this.edxActivationRoleRepository = edxActivationRoleRepository;
    this.restUtils = restUtils;
    this.props = props;
  }

  public List<MinistryOwnershipTeamEntity> getMinistryTeamsList() {
    return this.getMinistryOwnershipTeamRepository().findAll();
  }

  public List<EdxUserSchoolEntity> getEdxUserSchoolsList() {
    return this.getEdxUserSchoolsRepository().findAll();
  }

  public EdxUserEntity retrieveEdxUserByID(final UUID edxUserID) {
    var res = this.getEdxUserRepository().findById(edxUserID);
    if (res.isPresent()) {
      return res.get();
    } else {
      throw new EntityNotFoundException(EdxUser.class, EDX_USER_ID, edxUserID.toString());
    }
  }

  public List<String> getEdxUserSchoolsList(String permissionCode) {
    List<EdxUserSchoolEntity> schoolIDBytes;
    if(permissionCode != null) {
      schoolIDBytes = this.getEdxUserSchoolsRepository().findSchoolsByPermission(permissionCode);
    }else{
      schoolIDBytes = this.getEdxUserSchoolsRepository().findAll();
    }
    return schoolIDBytes.stream().map(school -> school.getSchoolID().toString()).distinct().toList();
  }

  public List<EdxActivationCodeEntity> getEdxUserInvitations(String instituteType) {
    if(instituteType.equalsIgnoreCase(InstituteTypeCode.DISTRICT.toString())){
      return edxActivationCodeRepository.findAllByDistrictIDIsNotNullAndIsPrimaryIsFalse();
    }else if(instituteType.equalsIgnoreCase(InstituteTypeCode.SCHOOL.toString())){
      return edxActivationCodeRepository.findAllBySchoolIDIsNotNullAndIsPrimaryIsFalse();
    }
    return edxActivationCodeRepository.findAll();
  }

  public List<String> getEdxUserDistrictsList(String permissionCode) {
    List<EdxUserDistrictEntity> districtIDs;
    if(permissionCode != null) {
      districtIDs = edxUserDistrictRepository.findDistrictsByPermission(permissionCode);
    }else{
      districtIDs = edxUserDistrictRepository.findAll();
    }
    return districtIDs.stream().map(district -> district.getDistrictID().toString()).distinct().toList();
  }

  public List<EdxUserEntity> findEdxUsers(Optional<UUID> digitalId, Optional<UUID> schoolID, String firstName, String lastName, Optional<UUID> districtID) {
    return this.getEdxUserRepository().findEdxUsers(digitalId, schoolID, firstName, lastName, districtID);
  }

  private List<UUID> getFilteredDistrictSchools(String districtID){
    var districtSchoolIDsMap = restUtils.getDistrictSchoolsMap();
    var fullSchooList = districtSchoolIDsMap.get(districtID);
    var filteredSchoolList = new ArrayList<UUID>();
    LocalDateTime currentDate = LocalDateTime.now();

    fullSchooList.forEach(school -> {
      if (INDEPENDENT_SCHOOL_CATEGORIES.contains(school.getSchoolCategoryCode())) {
        return;
      }
      if (school.getClosedDate() != null) {
        LocalDateTime closedDate = LocalDateTime.parse(school.getClosedDate());
        if (currentDate.isAfter(closedDate)) {
          return;
        }
      }
      filteredSchoolList.add(UUID.fromString(school.getSchoolId()));
    });
    return filteredSchoolList;
  }

  public List<EdxSchool> findAllDistrictEdxUsers(String districtID) {
    List<EdxUserSchoolEntity> edxUserSchoolEntities = this.getEdxUserSchoolsRepository().findAllBySchoolIDIn(getFilteredDistrictSchools(districtID));
    Map<String, EdxSchool> edxSchools = new HashMap<>();
    edxUserSchoolEntities.stream().forEach(edxUserSchoolEntity -> {
      String schoolID = edxUserSchoolEntity.getSchoolID().toString();
      if(!edxSchools.containsKey(schoolID)){
        EdxSchool school = new EdxSchool();
        school.setSchoolID(schoolID);
        school.setEdxDistrictSchoolUsers(new ArrayList<>());
        edxSchools.put(schoolID, school);
      }

      EdxDistrictSchoolUserTombstone tomb = new EdxDistrictSchoolUserTombstone();
      var edxUserEntity = edxUserSchoolEntity.getEdxUserEntity();
      tomb.setEdxUserID(edxUserEntity.getEdxUserID().toString());
      tomb.setEdxUserSchoolID(edxUserSchoolEntity.getEdxUserSchoolID().toString());
      tomb.setExpiryDate(edxUserSchoolEntity.getExpiryDate() != null ? edxUserSchoolEntity.getExpiryDate().toString() : null);
      tomb.setDigitalIdentityID(edxUserEntity.getDigitalIdentityID().toString());
      tomb.setEmail(edxUserEntity.getEmail());
      tomb.setFullName(edxUserEntity.getFirstName() + " " + edxUserEntity.getLastName());
      var rolesMap = getEdxRolesMap();
      tomb.setSchoolRoles(edxUserSchoolEntity.getEdxUserSchoolRoleEntities().stream().map(edxSchool -> rolesMap.get(edxSchool.getEdxRoleCode()).getLabel()).toList());
      tomb.setSchoolRoleCodes(edxUserSchoolEntity.getEdxUserSchoolRoleEntities().stream().map(edxSchool -> rolesMap.get(edxSchool.getEdxRoleCode()).getEdxRoleCode()).toList());

      edxSchools.get(schoolID).getEdxDistrictSchoolUsers().add(tomb);
    });

    return edxSchools.values().stream().toList();
  }

  public EdxUserEntity createEdxUser(EdxUserEntity edxUserEntity) {

    mapEdxUserSchoolAndRole(edxUserEntity);

    mapEdxUserDistrictAndRole(edxUserEntity);

    if(this.getEdxUserRepository().existsByDigitalIdentityID(edxUserEntity.getDigitalIdentityID())){
      throw new EntityExistsException(String.format("digitalIdentityId must be unique. EdxUser with digitalIdentityID: %s already exists", edxUserEntity.getDigitalIdentityID()));
    }

    return this.getEdxUserRepository().save(edxUserEntity);
  }

  private void mapEdxUserDistrictAndRole(EdxUserEntity edxUserEntity) {
    if (!CollectionUtils.isEmpty(edxUserEntity.getEdxUserDistrictEntities())) {
      for (var entity : edxUserEntity.getEdxUserDistrictEntities()) {
        entity.setEdxUserEntity(edxUserEntity);
        if (!CollectionUtils.isEmpty(entity.getEdxUserDistrictRoleEntities())) {
          for(var roleEntity : entity.getEdxUserDistrictRoleEntities()){
            roleEntity.setEdxUserDistrictEntity(entity);
          }
        }
      }
    }
  }

  private  void mapEdxUserSchoolAndRole(EdxUserEntity edxUserEntity) {
    if (!CollectionUtils.isEmpty(edxUserEntity.getEdxUserSchoolEntities())) {
      for (var entity : edxUserEntity.getEdxUserSchoolEntities()) {
        entity.setEdxUserEntity(edxUserEntity);
        if (!CollectionUtils.isEmpty(entity.getEdxUserSchoolRoleEntities())) {
          for (var roleEntity : entity.getEdxUserSchoolRoleEntities()) {
            roleEntity.setEdxUserSchoolEntity(entity);
          }
        }
      }
    }
  }

  public EdxUserSchoolEntity createEdxUserSchool(UUID edxUserID, EdxUserSchoolEntity edxUserSchoolEntity) {
    val entityOptional = getEdxUserRepository().findById(edxUserID);
    val userEntity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString()));
    val optionalSchool = getEdxUserSchoolsRepository().findEdxUserSchoolEntitiesBySchoolIDAndEdxUserEntity(edxUserSchoolEntity.getSchoolID(), userEntity);
    if (optionalSchool.isEmpty()) {
      edxUserSchoolEntity.getEdxUserSchoolRoleEntities().forEach(schoolRole -> schoolRole.setEdxUserSchoolEntity(edxUserSchoolEntity));
      return getEdxUserSchoolsRepository().save(edxUserSchoolEntity);
    } else {
      throw new EntityExistsException("EdxUser to EdxUserSchool association already exists");
    }
  }

  public EdxUserSchoolEntity updateEdxUserSchool(EdxUserSchoolEntity edxUserSchoolEntity) {
    //check for school
    val optionalSchool = getEdxUserSchoolsRepository().findById(edxUserSchoolEntity.getEdxUserSchoolID());
    if (optionalSchool.isPresent()) {
      EdxUserSchoolEntity currentEdxUserSchoolEntity = optionalSchool.get();
      logUpdatesEdxUserSchool(currentEdxUserSchoolEntity, edxUserSchoolEntity);
      BeanUtils.copyProperties(edxUserSchoolEntity, currentEdxUserSchoolEntity, "edxUserSchoolRoleEntities", "createUser", "createDate");

      currentEdxUserSchoolEntity.getEdxUserSchoolRoleEntities().clear();
      currentEdxUserSchoolEntity.getEdxUserSchoolRoleEntities().addAll(edxUserSchoolEntity.getEdxUserSchoolRoleEntities());

      //If we add a new role, we need to set the audit fields
      for (var schoolRole : currentEdxUserSchoolEntity.getEdxUserSchoolRoleEntities()) {
        if (schoolRole.getEdxUserSchoolRoleID() == null) {
          schoolRole.setCreateDate(LocalDateTime.now());
          schoolRole.setCreateUser(edxUserSchoolEntity.getUpdateUser());
          schoolRole.setUpdateDate(LocalDateTime.now());
          schoolRole.setUpdateUser(edxUserSchoolEntity.getUpdateUser());

          //since we are adding a new role, we need to link the role entity to the school entity (follows pattern from creating Edx User)
          schoolRole.setEdxUserSchoolEntity(currentEdxUserSchoolEntity);
        }
      }

      return getEdxUserSchoolsRepository().save(currentEdxUserSchoolEntity);
    } else {
      throw new EntityNotFoundException(EdxUserSchoolEntity.class, "EdxUserSchoolEntity", edxUserSchoolEntity.getEdxUserSchoolID().toString());
    }
  }

  /**
   * Log updates edx user school.
   *
   * @param currentEdxUserSchoolEntity the current edx user school entity
   * @param newEdxUserSchoolEntity     the new edx user school entity
   */
  private void logUpdatesEdxUserSchool(final EdxUserSchoolEntity currentEdxUserSchoolEntity, final EdxUserSchoolEntity newEdxUserSchoolEntity) {
    if (log.isDebugEnabled()) {
      log.debug("Edx User update, current :: {}, new :: {}", currentEdxUserSchoolEntity, newEdxUserSchoolEntity);
    }
  }


  private void logUpdatesEdxUserDistrict(final EdxUserDistrictEntity currentEdxUserDistrictEntity, final EdxUserDistrictEntity newEdxUserDistrictEntity) {
    if (log.isDebugEnabled()) {
      log.debug("Edx User District update, current :: {}, new :: {}", currentEdxUserDistrictEntity, newEdxUserDistrictEntity);
    }
  }
  /**
   * Create edx user school role edx user school role entity.
   *
   * @param edxUserID               the edx user id
   * @param edxUserSchoolId         the edx user school id
   * @param edxUserSchoolRoleEntity the edx user school role entity
   * @return the edx user school role entity
   */
  public EdxUserSchoolRoleEntity createEdxUserSchoolRole(UUID edxUserID, UUID edxUserSchoolId, EdxUserSchoolRoleEntity edxUserSchoolRoleEntity) {
    val optionalUserSchoolRoleEntity = getEdxUserSchoolRoleRepository().findEdxUserSchoolRoleEntityByEdxUserSchoolEntity_EdxUserSchoolIDAndEdxRoleCode(edxUserSchoolRoleEntity.getEdxUserSchoolEntity().getEdxUserSchoolID(), edxUserSchoolRoleEntity.getEdxRoleCode());
    if (optionalUserSchoolRoleEntity.isEmpty()) {
      val optionalEdxUserSchoolEntity = getEdxUserSchoolsRepository().findById(edxUserSchoolRoleEntity.getEdxUserSchoolEntity().getEdxUserSchoolID());
      optionalEdxUserSchoolEntity.orElseThrow(() -> new EntityNotFoundException(EdxUserSchoolEntity.class, "edxUserSchoolId", edxUserSchoolId.toString()));
      EdxUserSchoolEntity schoolEntity = optionalEdxUserSchoolEntity.get();
      val optionEdxUserEntity = getEdxUserRepository().findById(schoolEntity.getEdxUserEntity().getEdxUserID());
      optionEdxUserEntity.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, "edxUserSchoolId", edxUserSchoolId.toString()));
      if (edxUserID.equals(schoolEntity.getEdxUserEntity().getEdxUserID())) {
        return getEdxUserSchoolRoleRepository().save(edxUserSchoolRoleEntity);
      } else {
        ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This EdxSchoolRole cannot be added for this EdxUser " + edxUserID).status(BAD_REQUEST).build();
        throw new InvalidPayloadException(error);
      }

    } else {
      throw new EntityExistsException("EdxUserSchoolRole to EdxUserSchool association already exists");
    }
  }

  /**
   * Delete edx user by id.
   *
   * @param edxUserID the edx user id
   */
  public void deleteEdxUserById(UUID edxUserID) {
    val entityOptional = getEdxUserRepository().findById(edxUserID);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString()));
    this.getEdxUserRepository().delete(entity);

  }

  /**
   * Delete edx school user by id.
   *
   * @param edxUserID       the edx user id
   * @param edxUserSchoolId the edx user school id
   */
  public void deleteEdxSchoolUserById(UUID edxUserID, UUID edxUserSchoolId) {
    val entityOptional = getEdxUserSchoolsRepository().findById(edxUserSchoolId);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserSchoolEntity.class, "edxUserSchoolID", edxUserSchoolId.toString()));
    if (entity.getEdxUserEntity().getEdxUserID().equals(edxUserID)) {
      this.getEdxUserSchoolsRepository().delete(entity);
    } else {
      throw new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString());
    }
  }

  /**
   * Delete edx school user role by id.
   *
   * @param edxUserID           the edx user id
   * @param edxUserSchoolRoleId the edx user school role id
   */
  public void deleteEdxSchoolUserRoleById(UUID edxUserID, UUID edxUserSchoolRoleId) {
    val entityOptional = getEdxUserSchoolRoleRepository().findById(edxUserSchoolRoleId);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserSchoolRoleEntity.class, "edxUserSchoolRoleID", edxUserSchoolRoleId.toString()));
    if (entity.getEdxUserSchoolEntity() != null && entity.getEdxUserSchoolEntity().getEdxUserEntity().getEdxUserID().equals(edxUserID)) {
      this.getEdxUserSchoolRoleRepository().delete(entity);
    } else {
      throw new EntityNotFoundException(EdxUserSchoolRoleEntity.class, EDX_USER_ID, edxUserID.toString());
    }

  }

  /**
   * Find all edx roles list.
   *
   * @return the list
   */
  public List<EdxRoleEntity> findAllEdxRoles() {
    return this.getEdxRoleRepository().findAll();
  }

  public Map<String, EdxRoleEntity> getEdxRolesMap() {
    return this.getEdxRoleRepository().findAll().stream().collect(Collectors.toMap(EdxRoleEntity::getEdxRoleCode, item -> item));
  }

  /**
   * Activate school user edx user entity.
   *
   * @param edxActivateUser the edx activate user
   * @return the edx user entity
   */
  public EdxUserEntity activateEdxUser(EdxActivateUser edxActivateUser) {
    val acCodes = Arrays.asList(edxActivateUser.getPersonalActivationCode(), edxActivateUser.getPrimaryEdxCode());
    List<EdxActivationCodeEntity> activationCodes;
    if (edxActivateUser.getSchoolID() != null) {
      activationCodes = edxActivationCodeRepository.findEdxActivationCodeByActivationCodeInAndSchoolID(acCodes, edxActivateUser.getSchoolID());
    } else {
      activationCodes = edxActivationCodeRepository.findEdxActivationCodeByActivationCodeInAndDistrictID(acCodes, edxActivateUser.getDistrictID());
    }
    validateIncomingActivationCodeTypes(edxActivateUser, activationCodes);
    if (!CollectionUtils.isEmpty(activationCodes) && activationCodes.size() == 2) {
      EdxActivationCodeEntity userCodeEntity = validateExpiryAndSetEdxUserIdForPersonalActivationCode(edxActivateUser, activationCodes);

      //Activate User
      return activateUser(edxActivateUser, userCodeEntity);
    } else {
      throw new EntityNotFoundException(EdxActivationCode.class, EDX_ACTIVATION_CODE_ID, edxActivateUser.getPrimaryEdxCode());
    }
  }

  private void validateIncomingActivationCodeTypes(EdxActivateUser user, List<EdxActivationCodeEntity> activationCodes){
    for (val activationCode : activationCodes) {
      if((activationCode.getIsPrimary() && !activationCode.getActivationCode().equalsIgnoreCase(user.getPrimaryEdxCode())) ||
              (!activationCode.getIsPrimary() && !activationCode.getActivationCode().equalsIgnoreCase(user.getPersonalActivationCode()))){
        ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Invalid code provided.").status(BAD_REQUEST).build();
        throw new InvalidPayloadException(error);
      }
    }
  }

  private EdxActivationCodeEntity validateExpiryAndSetEdxUserIdForPersonalActivationCode(EdxActivateUser edxActivateUser, List<EdxActivationCodeEntity> activationCodes) {
    EdxActivationCodeEntity userCodeEntity = null;
    for (val activationCode : activationCodes) {
      if (!activationCode.getIsPrimary()) {
        checkPersonalActivationCodeExpiry(activationCode);
        userCodeEntity = activationCode;
        if (activationCode.getEdxUserId() != null) {
          edxActivateUser.setEdxUserId(activationCode.getEdxUserId().toString());
        }
      }
    }
    return userCodeEntity;
  }

  private void checkPersonalActivationCodeExpiry(EdxActivationCodeEntity activationCode) {
    if (activationCode.getExpiryDate() != null && activationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This Activation Code has expired").status(BAD_REQUEST).build();
      throw new InvalidPayloadException(error);
    }
  }

  private EdxUserEntity activateUser(EdxActivateUser edxActivateUser, EdxActivationCodeEntity userCodeEntity) {
    if (StringUtils.isNotBlank(edxActivateUser.getEdxUserId())) {
      //relink user logic
      return relinkEdxUser(edxActivateUser, userCodeEntity);
    } else {
      //activate user logic
      val edxUsers = getEdxUserRepository().findEdxUserEntitiesByDigitalIdentityID(UUID.fromString(edxActivateUser.getDigitalId()));
      if (edxUsers.isEmpty()) {
        //create completely new user
        return createEdxUserDetailsFromActivationCodeDetails(edxActivateUser, userCodeEntity);
      } else {

        verifyExistingUserSchoolOrDistrictAssociation(edxActivateUser, edxUsers);

        //add the user_school and school_role to user to the edx_user
        return updateEdxUserDetailsFromActivationCodeDetails(edxUsers, userCodeEntity, edxActivateUser);
      }
    }
  }

  /**
   * Verify existing user schoolID association.
   *
   * @param edxActivateUser the edx activate user
   * @param edxUsers        the edx users
   */
  private void verifyExistingUserSchoolOrDistrictAssociation(EdxActivateUser edxActivateUser, List<EdxUserEntity> edxUsers) {
    val existingUser = edxUsers.get(0);
    if (edxActivateUser.getSchoolID() != null) {
      verifyExistingUserForSchoolAssociation(edxActivateUser, existingUser);
    } else {
      verifyExistingUserForDistrictAssociation(edxActivateUser, existingUser);
    }

  }

  private void verifyExistingUserForDistrictAssociation(EdxActivateUser edxActivateUser, EdxUserEntity existingUser) {
    if (!CollectionUtils.isEmpty(existingUser.getEdxUserDistrictEntities())) {
      for (EdxUserDistrictEntity userDistrictEntity : existingUser.getEdxUserDistrictEntities()) {
        if (userDistrictEntity.getDistrictID().equals(edxActivateUser.getDistrictID())) {
          ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This user is already associated to the district").status(CONFLICT).build();
          throw new InvalidPayloadException(error);
        }
      }
    }
  }

  private void verifyExistingUserForSchoolAssociation(EdxActivateUser edxActivateUser, EdxUserEntity existingUser) {
    if (!CollectionUtils.isEmpty(existingUser.getEdxUserSchoolEntities())) {
      for (EdxUserSchoolEntity schoolEntity : existingUser.getEdxUserSchoolEntities()) {
        if (schoolEntity.getSchoolID().equals(edxActivateUser.getSchoolID())) {
          ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This user is already associated to the school").status(CONFLICT).build();
          throw new InvalidPayloadException(error);
        }
      }
    }
  }

  /**
   * Relink edx user edx user entity.
   *
   * @param edxActivateUser         the edx activate user
   * @param edxActivationCodeEntity the edx activation code entity
   * @return the edx user entity
   */
  private EdxUserEntity relinkEdxUser(EdxActivateUser edxActivateUser, EdxActivationCodeEntity edxActivationCodeEntity) {
    EdxUserEntity edxUserEntity;
    val digitalIdentityEdxUserList = getEdxUserRepository().findEdxUserEntitiesByDigitalIdentityID(UUID.fromString(edxActivateUser.getDigitalId()));
    if (digitalIdentityEdxUserList != null && digitalIdentityEdxUserList.size() > 0) {
      edxUserEntity = digitalIdentityEdxUserList.get(0);
    } else {
      val optionalEdxUserEntity = getEdxUserRepository().findById(UUID.fromString(edxActivateUser.getEdxUserId()));
      edxUserEntity = optionalEdxUserEntity.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxActivateUser.getEdxUserId()));
    }

    val updatedEdxUser = createEdxUserFromActivationCodeDetails(edxUserEntity, edxActivateUser, edxActivationCodeEntity);
    val savedEdxUser = getEdxUserRepository().save(updatedEdxUser);
    updateEdxUserDetailsFromActivationCodeDetails(Arrays.asList(edxUserEntity), edxActivationCodeEntity, edxActivateUser);
    return savedEdxUser;
  }

  /**
   * Update edx user details from activation code details edx user entity.
   *
   * @param edxUsers                the edx users
   * @param edxActivationCodeEntity the edx activation code entity
   * @param edxActivateUser         the edx activate user
   * @return the edx user entity
   */
  private EdxUserEntity updateEdxUserDetailsFromActivationCodeDetails(List<EdxUserEntity> edxUsers, EdxActivationCodeEntity edxActivationCodeEntity, EdxActivateUser edxActivateUser) {
    val edxUserEntity = getEdxUserRepository().findById(edxUsers.get(0).getEdxUserID()).get();
    createEdxUserDetails(edxActivateUser, edxActivationCodeEntity, edxUserEntity);
    updateAuditColumnsForEdxUserEntityUpdate(edxUserEntity, edxActivateUser);
    EdxUserEntity updatedUser = edxUserRepository.save(edxUserEntity);
    expireActivationCodes(edxActivationCodeEntity, edxActivateUser, updatedUser);
    return updatedUser;
  }

  /**
   * Create edx user details from activation code details edx user entity.
   *
   * @param edxActivateUser         the edx activate user
   * @param edxActivationCodeEntity the edx activation code entity
   * @return the edx user entity
   */
  private EdxUserEntity createEdxUserDetailsFromActivationCodeDetails(EdxActivateUser edxActivateUser, EdxActivationCodeEntity edxActivationCodeEntity) {
    EdxUserEntity edxUser = new EdxUserEntity();
    val edxUserEntity = createEdxUserFromActivationCodeDetails(edxUser, edxActivateUser, edxActivationCodeEntity);
    createEdxUserDetails(edxActivateUser, edxActivationCodeEntity, edxUserEntity);

    EdxUserEntity savedEntity = edxUserRepository.save(edxUserEntity);
    //expire the activationCodes
    expireActivationCodes(edxActivationCodeEntity, edxActivateUser, savedEntity);
    return savedEntity;
  }

  private void createEdxUserDetails(EdxActivateUser edxActivateUser, EdxActivationCodeEntity edxActivationCodeEntity, EdxUserEntity edxUserEntity) {
    if (edxActivateUser.getSchoolID() != null) {
      //set up school user
      val edxUserSchoolEntity = createEdxUserSchoolFromActivationCodeDetails(edxActivationCodeEntity, edxUserEntity, edxActivateUser);
      val edxUserSchoolRoleEntities = createEdxUserSchoolRolesFromActivationCodeDetails(edxActivationCodeEntity.getEdxActivationRoleEntities(), edxUserSchoolEntity, edxActivateUser);
      //updating associations
      updateEdxUserAssociations(edxUserEntity, edxUserSchoolEntity, edxUserSchoolRoleEntities);
    } else {
      //set up district user
      val edxUserDistrictEntity = createEdxUserDistrictFromActivationCodeDetails(edxActivationCodeEntity, edxUserEntity, edxActivateUser);
      val edxUserDistrictRoleEntities = createEdxUserDistrictRolesFromActivationCodeDetails(edxActivationCodeEntity.getEdxActivationRoleEntities(), edxUserDistrictEntity, edxActivateUser);
      //updating associations
      updateEdxUserAssociationsForDistrict(edxUserEntity, edxUserDistrictEntity, edxUserDistrictRoleEntities);
    }
  }

  /**
   * Update edx user associations.
   *
   * @param edxUserEntity             the edx user entity
   * @param edxUserSchoolEntity       the edx user school entity
   * @param edxUserSchoolRoleEntities the edx user school role entities
   */
  private void updateEdxUserAssociations(EdxUserEntity edxUserEntity, EdxUserSchoolEntity edxUserSchoolEntity, Set<EdxUserSchoolRoleEntity> edxUserSchoolRoleEntities) {
    for (EdxUserSchoolRoleEntity edxUserSchoolRole : edxUserSchoolRoleEntities) {
      edxUserSchoolRole.setEdxUserSchoolEntity(edxUserSchoolEntity);
      edxUserSchoolEntity.getEdxUserSchoolRoleEntities().add(edxUserSchoolRole);
    }
    edxUserSchoolEntity.setEdxUserEntity(edxUserEntity);
    edxUserEntity.getEdxUserSchoolEntities().add(edxUserSchoolEntity);
  }

  private void updateEdxUserAssociationsForDistrict(EdxUserEntity edxUserEntity, EdxUserDistrictEntity edxUserDistrictEntity, Set<EdxUserDistrictRoleEntity> edxUserDistrictRoleEntities) {
    for (EdxUserDistrictRoleEntity edxUserDistrictRole : edxUserDistrictRoleEntities) {
      edxUserDistrictRole.setEdxUserDistrictEntity(edxUserDistrictEntity);
      edxUserDistrictEntity.getEdxUserDistrictRoleEntities().add(edxUserDistrictRole);
    }
    edxUserDistrictEntity.setEdxUserEntity(edxUserEntity);
    edxUserEntity.getEdxUserDistrictEntities().add(edxUserDistrictEntity);
  }

  /**
   * Expire activation codes.
   *
   * @param edxActivationCode the edx activation code
   * @param edxActivateUser   the edx activate user
   */
  private void expireActivationCodes(EdxActivationCodeEntity edxActivationCode, EdxActivateUser edxActivateUser, EdxUserEntity user) {
    val optionalEdxActivationCodeEntity = getEdxActivationCodeRepository().findById(edxActivationCode.getEdxActivationCodeId());
    val activationCodeEntity = optionalEdxActivationCodeEntity.orElseThrow(() -> new EntityNotFoundException(EdxActivationCodeEntity.class, EDX_ACTIVATION_CODE_ID, edxActivationCode.getEdxActivationCodeId().toString()));
    if (!activationCodeEntity.getIsPrimary()) {//expire only the personal activation code
      activationCodeEntity.setExpiryDate(LocalDateTime.now());
      activationCodeEntity.setLinkedEdxUserId(user.getEdxUserID());
      activationCodeEntity.setUpdateUser(edxActivateUser.getUpdateUser());
      activationCodeEntity.setUpdateDate(LocalDateTime.now());
      getEdxActivationCodeRepository().save(activationCodeEntity);
    }
  }

  /**
   * Create edx user school roles from activation code details set.
   *
   * @param edxActivationRoleEntities the edx activation role entities
   * @param edxUserSchoolEntity       the edx user school entity
   * @param edxActivateUser           the edx activate user
   * @return the set
   */
  private Set<EdxUserSchoolRoleEntity> createEdxUserSchoolRolesFromActivationCodeDetails(Set<EdxActivationRoleEntity> edxActivationRoleEntities, EdxUserSchoolEntity edxUserSchoolEntity, EdxActivateUser edxActivateUser) {
    Set<EdxUserSchoolRoleEntity> schoolRoleEntitySet = new HashSet<>();
    edxActivationRoleEntities.forEach(edxActivationRoleEntity -> {
      EdxUserSchoolRoleEntity schoolRoleEntity = new EdxUserSchoolRoleEntity();
      schoolRoleEntity.setEdxUserSchoolEntity(edxUserSchoolEntity);
      schoolRoleEntity.setEdxRoleCode(edxActivationRoleEntity.getEdxRoleCode());
      updateAuditColumnsForEdxUserSchoolRoleEntity(edxActivateUser, schoolRoleEntity);
      schoolRoleEntitySet.add(schoolRoleEntity);
    });
    return schoolRoleEntitySet;
  }

  private Set<EdxUserDistrictRoleEntity> createEdxUserDistrictRolesFromActivationCodeDetails(Set<EdxActivationRoleEntity> edxActivationRoleEntities, EdxUserDistrictEntity edxUserDistrictEntity, EdxActivateUser edxActivateUser) {
    Set<EdxUserDistrictRoleEntity> districtRoleEntitySet = new HashSet<>();
    edxActivationRoleEntities.forEach(edxActivationRoleEntity -> {
      EdxUserDistrictRoleEntity districtRoleEntity = new EdxUserDistrictRoleEntity();
      districtRoleEntity.setEdxUserDistrictEntity(edxUserDistrictEntity);
      districtRoleEntity.setEdxRoleCode(edxActivationRoleEntity.getEdxRoleCode());
      updateAuditColumnsForEdxUserDistrictRoleEntity(edxActivateUser, districtRoleEntity);
      districtRoleEntitySet.add(districtRoleEntity);
    });
    return districtRoleEntitySet;
  }

  /**
   * Create edx user school from activation code details edx user school entity.
   *
   * @param activationCode  the activation code
   * @param edxUser         the edx user
   * @param edxActivateUser the edx activate user
   * @return the edx user school entity
   */
  private EdxUserSchoolEntity createEdxUserSchoolFromActivationCodeDetails(EdxActivationCodeEntity activationCode, EdxUserEntity edxUser, EdxActivateUser edxActivateUser) {
    EdxUserSchoolEntity userSchoolEntity = new EdxUserSchoolEntity();
    userSchoolEntity.setEdxUserEntity(edxUser);
    userSchoolEntity.setSchoolID(activationCode.getSchoolID());
    userSchoolEntity.setExpiryDate(activationCode.getEdxUserExpiryDate());
    updateAuditColumnsForEdxUserSchoolEntity(edxActivateUser, userSchoolEntity);
    return userSchoolEntity;
  }

  private EdxUserDistrictEntity createEdxUserDistrictFromActivationCodeDetails(EdxActivationCodeEntity activationCode, EdxUserEntity edxUser, EdxActivateUser edxActivateUser) {
    EdxUserDistrictEntity userDistrictEntity = new EdxUserDistrictEntity();
    userDistrictEntity.setEdxUserEntity(edxUser);
    userDistrictEntity.setDistrictID(activationCode.getDistrictID());
    userDistrictEntity.setExpiryDate(activationCode.getEdxUserExpiryDate());
    updateAuditColumnsForEdxUserDistrictEntity(edxActivateUser, userDistrictEntity);
    return userDistrictEntity;
  }

  /**
   * Create edx user from activation code details edx user entity.
   *
   * @param edxUser         the edx user
   * @param edxActivateUser the edx activate user
   * @param activationCode  the activation code
   * @return the edx user entity
   */
  private EdxUserEntity createEdxUserFromActivationCodeDetails(EdxUserEntity edxUser, EdxActivateUser edxActivateUser, EdxActivationCodeEntity activationCode) {
    edxUser.setFirstName(activationCode.getFirstName());
    edxUser.setLastName(activationCode.getLastName());
    edxUser.setEmail(activationCode.getEmail());
    edxUser.setDigitalIdentityID(UUID.fromString(edxActivateUser.getDigitalId()));
    TransformUtil.uppercaseFields(edxUser);
    updateAuditColumnsForEdxUserEntityCreate(edxUser, edxActivateUser);
    return edxUser;
  }

  /**
   * Update audit columns for edx user entity create.
   *
   * @param edxUser         the edx user
   * @param edxActivateUser the edx activate user
   */
  private void updateAuditColumnsForEdxUserEntityCreate(EdxUserEntity edxUser, EdxActivateUser edxActivateUser) {
    edxUser.setCreateUser(edxActivateUser.getCreateUser());
    edxUser.setCreateDate(LocalDateTime.now());
    updateAuditColumnsForEdxUserEntityUpdate(edxUser, edxActivateUser);
  }

  /**
   * Update audit columns for edx user entity update.
   *
   * @param edxUser         the edx user
   * @param edxActivateUser the edx activate user
   */
  private void updateAuditColumnsForEdxUserEntityUpdate(EdxUserEntity edxUser, EdxActivateUser edxActivateUser) {
    edxUser.setUpdateUser(edxActivateUser.getUpdateUser());
    edxUser.setUpdateDate(LocalDateTime.now());
  }

  /**
   * Update audit columns for edx user school entity.
   *
   * @param edxActivateUser  the edx activate user
   * @param userSchoolEntity the user school entity
   */
  private void updateAuditColumnsForEdxUserSchoolEntity(EdxActivateUser edxActivateUser, EdxUserSchoolEntity userSchoolEntity) {
    userSchoolEntity.setCreateUser(edxActivateUser.getCreateUser());
    userSchoolEntity.setUpdateUser(edxActivateUser.getUpdateUser());
    userSchoolEntity.setCreateDate(LocalDateTime.now());
    userSchoolEntity.setUpdateDate(LocalDateTime.now());
  }

  private void updateAuditColumnsForEdxUserDistrictEntity(EdxActivateUser edxActivateUser, EdxUserDistrictEntity userDistrictEntity) {
    userDistrictEntity.setCreateUser(edxActivateUser.getCreateUser());
    userDistrictEntity.setUpdateUser(edxActivateUser.getUpdateUser());
    userDistrictEntity.setCreateDate(LocalDateTime.now());
    userDistrictEntity.setUpdateDate(LocalDateTime.now());
  }

  /**
   * Update audit columns for edx user school role entity.
   *
   * @param edxActivateUser  the edx activate user
   * @param schoolRoleEntity the school role entity
   */
  private void updateAuditColumnsForEdxUserSchoolRoleEntity(EdxActivateUser edxActivateUser, EdxUserSchoolRoleEntity schoolRoleEntity) {
    schoolRoleEntity.setCreateUser(edxActivateUser.getCreateUser());
    schoolRoleEntity.setUpdateUser(edxActivateUser.getUpdateUser());
    schoolRoleEntity.setCreateDate(LocalDateTime.now());
    schoolRoleEntity.setUpdateDate(LocalDateTime.now());
  }

  private void updateAuditColumnsForEdxUserDistrictRoleEntity(EdxActivateUser edxActivateUser, EdxUserDistrictRoleEntity userDistrictRoleEntity) {
    userDistrictRoleEntity.setCreateUser(edxActivateUser.getCreateUser());
    userDistrictRoleEntity.setUpdateUser(edxActivateUser.getUpdateUser());
    userDistrictRoleEntity.setCreateDate(LocalDateTime.now());
    userDistrictRoleEntity.setUpdateDate(LocalDateTime.now());
  }

  /**
   * Expire user activation url.
   *
   * @param userActivationValidationCode the user activation validation code
   */
  public InstituteTypeCode expireUserActivationUrl(UUID userActivationValidationCode) {
    List<EdxActivationCodeEntity> activationCodeEntities = getEdxActivationCodeRepository().findEdxActivationCodeEntitiesByValidationCode(userActivationValidationCode);
    if (activationCodeEntities.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Invalid Link Provided").status(BAD_REQUEST).build();
      throw new InvalidPayloadException(error);
    }
    if (activationCodeEntities.stream().anyMatch(el -> el.getNumberOfClicks() >= 5)) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This User Activation Link has already expired").status(GONE).build();
      throw new InvalidPayloadException(error);
    }
    if (activationCodeEntities.stream().anyMatch(el -> el.getExpiryDate().isBefore(LocalDateTime.now()))) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This User Activation Link has already expired").status(GONE).build();
      throw new InvalidPayloadException(error);
    }
    activationCodeEntities.forEach(activationCode -> {
      activationCode.setNumberOfClicks(activationCode.getNumberOfClicks() + 1);
      activationCode.setUpdateDate(LocalDateTime.now());
      getEdxActivationCodeRepository().save(activationCode);
    });
    if (activationCodeEntities.get(0).getSchoolID() != null) {
      return InstituteTypeCode.SCHOOL;
    } else {
      return InstituteTypeCode.DISTRICT;
    }
  }

  /**
   * Create edx activation code edx activation code entity.
   *
   * @param edxActivationCodeEntity the edx activation code entity
   * @return the edx activation code entity
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  public EdxActivationCodeEntity createEdxActivationCode(EdxActivationCodeEntity edxActivationCodeEntity) throws NoSuchAlgorithmException {
    edxActivationCodeEntity.setValidationCode(UUID.randomUUID());
    if (!CollectionUtils.isEmpty(edxActivationCodeEntity.getEdxActivationRoleEntities())) {
      List<String> roleCodeList = new ArrayList<>();
      for (val activationRole : edxActivationCodeEntity.getEdxActivationRoleEntities()) {
        activationRole.setEdxActivationCodeEntity(edxActivationCodeEntity); //bi-directional association
        roleCodeList.add(activationRole.getEdxRoleCode());
      }
      val roleListFromDB = getEdxRoleRepository().findAllById(roleCodeList);
      if (roleListFromDB.size() != roleCodeList.size()) {
        ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("The Role Id provided in the payload does not exist.").status(BAD_REQUEST).build();
        throw new InvalidPayloadException(error);
      }
    }
    TransformUtil.uppercaseFields(edxActivationCodeEntity);
    return getEdxActivationCodeRepository().save(edxActivationCodeEntity);
  }

  /**
   * Create personal edx activation code edx activation code entity.
   *
   * @param edxActivationCodeEntity the edx activation code entity
   * @return the edx activation code entity
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  public EdxActivationCodeEntity createPersonalEdxActivationCode(EdxActivationCodeEntity edxActivationCodeEntity) throws NoSuchAlgorithmException {
    edxActivationCodeEntity.setActivationCode(generateActivationCode());
    edxActivationCodeEntity.setExpiryDate(LocalDateTime.now().plusHours(props.getEdxSchoolUserActivationInviteValidity()));
    edxActivationCodeEntity.setNumberOfClicks(0);
    edxActivationCodeEntity.setIsPrimary(Boolean.FALSE);
    return createEdxActivationCode(edxActivationCodeEntity);
  }

  /**
   * Delete all activation codes belonging to a specified user ID.
   *
   * @param edxUserId the user ID
   */
  public void deleteActivationCodesByUserId(UUID edxUserId) {
    val entities = getEdxActivationCodeRepository()
      .findEdxActivationCodeEntitiesByEdxUserId(edxUserId);
    this.getEdxActivationCodeRepository().deleteAll(entities);
  }

  /**
   * Delete activation code.
   *
   * @param activationCodeId the activation code id
   */
  public void deleteActivationCode(UUID activationCodeId) {
    val entityOptional = getEdxActivationCodeRepository().findById(activationCodeId);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxActivationCodeEntity.class, EDX_ACTIVATION_CODE_ID, activationCodeId.toString()));
    this.getEdxActivationCodeRepository().delete(entity);
  }

  /**
   * Find primary edx activation code edx activation code entity.
   *
   * @param instituteType       the instituteType
   * @param instituteIdentifier the instituteIdentifier
   * @return the edx activation code entity
   */
  public EdxActivationCodeEntity findPrimaryEdxActivationCode(InstituteTypeCode instituteType, String instituteIdentifier)
  throws EntityNotFoundException {
    Optional<EdxActivationCodeEntity> primaryEdxActivationCode = this.findPrimaryEdxActivationCodeForInstitute(instituteType, instituteIdentifier);
    if (primaryEdxActivationCode.isEmpty()) {
      throw new EntityNotFoundException(EdxActivationCodeEntity.class, INSTITUTE_IDENTIFIER, instituteIdentifier);
    }
    return primaryEdxActivationCode.get();
  }

  /**
   * Generate or regenerate primary edx activation code edx activation code entity.
   *
   * @param instituteType            the instituteType
   * @param instituteIdentifier      the instituteIdentifier
   * @param edxPrimaryActivationCode the edx primary activation code
   * @return the edx activation code entity
   */
  public EdxActivationCodeEntity generateOrRegeneratePrimaryEdxActivationCode(InstituteTypeCode instituteType, String instituteIdentifier, EdxPrimaryActivationCode edxPrimaryActivationCode) {
    EdxActivationCodeEntity primaryEdxActivationCode = this.findPrimaryEdxActivationCodeForInstitute(instituteType, instituteIdentifier).orElseGet(() -> this.newPrimaryActivationCode(edxPrimaryActivationCode));
    try {
      primaryEdxActivationCode.setActivationCode(this.generateActivationCode());
    } catch (NoSuchAlgorithmException e) {
      ApiError.builder().timestamp(LocalDateTime.now()).message("Unable to generate an activation code.").status(INTERNAL_SERVER_ERROR).build();
    }
    this.updateAuditColumnsForPrimaryEdxActivationCode(edxPrimaryActivationCode, primaryEdxActivationCode);
    return this.getEdxActivationCodeRepository().save(primaryEdxActivationCode);
  }

  private Optional<EdxActivationCodeEntity> findPrimaryEdxActivationCodeForInstitute(InstituteTypeCode instituteType, String contactIdentifier) {
    switch (instituteType) {
      case SCHOOL:
        return getEdxActivationCodeRepository().findEdxActivationCodeEntitiesBySchoolIDAndIsPrimaryTrueAndDistrictIDIsNull(UUID.fromString(contactIdentifier));
      case DISTRICT:
        return getEdxActivationCodeRepository().findEdxActivationCodeEntitiesByDistrictIDAndIsPrimaryTrueAndSchoolIDIsNull(UUID.fromString(contactIdentifier));
      default:
        return Optional.empty();
    }
  }

  /**
   * Generate activation code string.
   *
   * @return the string
   * @throws NoSuchAlgorithmException the no such algorithm exception
   */
  private String generateActivationCode() throws NoSuchAlgorithmException {
    SecureRandom randomGenerator = SecureRandom.getInstance("SHA1PRNG");
    List<Character> validCharactersList = Chars.asList(props.getEdxActivationCodeValidCharacters().toCharArray());
    Collections.shuffle(validCharactersList);
    StringBuilder codeBuffer = new StringBuilder();
    for (int i = 0; i < props.getEdxActivationCodeLength(); i++) {
      char codeCharacter = validCharactersList.get(randomGenerator.nextInt(validCharactersList.size()));
      codeBuffer.append(codeCharacter);
    }
    return codeBuffer.toString();
  }

  /**
   * New primary activation code edx activation code entity.
   *
   * @param edxPrimaryActivationCode the edx primary activation code
   * @return the edx activation code entity
   */
  private EdxActivationCodeEntity newPrimaryActivationCode(EdxPrimaryActivationCode edxPrimaryActivationCode) {
    EdxActivationCodeEntity toReturn = new EdxActivationCodeEntity();
    LocalDateTime currentTime = LocalDateTime.now();
    toReturn.setSchoolID(edxPrimaryActivationCode.getSchoolID());
    if (edxPrimaryActivationCode.getDistrictID() != null) {
      toReturn.setDistrictID(edxPrimaryActivationCode.getDistrictID());
    }
    toReturn.setIsPrimary(true);
    toReturn.setNumberOfClicks(0);
    toReturn.setCreateUser(edxPrimaryActivationCode.getCreateUser());
    toReturn.setCreateDate(currentTime);
    toReturn.setUpdateUser(edxPrimaryActivationCode.getUpdateUser());
    toReturn.setUpdateDate(currentTime);
    toReturn.setValidationCode(UUID.randomUUID());
    return toReturn;
  }

  /**
   * Update audit columns for primary edx activation code.
   *
   * @param edxPrimaryActivationCode the edx primary activation code
   * @param toUpdate                 the to update
   */
  private void updateAuditColumnsForPrimaryEdxActivationCode(EdxPrimaryActivationCode edxPrimaryActivationCode, EdxActivationCodeEntity toUpdate) {
    toUpdate.setUpdateUser(edxPrimaryActivationCode.getUpdateUser());
    toUpdate.setUpdateDate(LocalDateTime.now());
  }

  /**
   * Find edx user email by schoolID and permission code set.
   *
   * @param schoolID        the schoolID
   * @param permissionCode the permission code
   * @return the set
   */
  public Set<String> findEdxUserEmailBySchoolIDAndPermissionCode(UUID schoolID, String permissionCode) {
    return getEdxUserRepository().findEdxUserEmailBySchoolIDAndPermissionCode(schoolID, permissionCode);
  }

  public Set<String> findEdxUserEmailByDistrictIDAndPermissionCode(UUID districtID, String permissionCode) {
    return getEdxUserRepository().findEdxUserEmailByDistrictIDAndPermissionCode(districtID, permissionCode);
  }

  public List<EdxRoleEntity> findAllEdxRolesForInstituteTypeCode(InstituteTypeCode instituteTypeCode) {
    Boolean isDistrictRole = InstituteTypeCode.DISTRICT.equals(instituteTypeCode);
    ExampleMatcher customExampleMatcher = ExampleMatcher.matchingAny()
      .withMatcher("isDistrictRole", ExampleMatcher.GenericPropertyMatchers.exact());
    EdxRoleEntity entity = new EdxRoleEntity();
    entity.setIsDistrictRole(isDistrictRole);
    return this.getEdxRoleRepository().findAll(Example.of(entity, customExampleMatcher));
  }

  public EdxUserDistrictEntity createEdxUserDistrict (UUID edxUserID, EdxUserDistrictEntity edxUserDistrictEntity) {
    val entityOptional = getEdxUserRepository().findById(edxUserID);
    val userEntity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString()));
    val optionalDistrict = getEdxUserDistrictRepository().findEdxUserDistrictEntitiesByDistrictIDAndEdxUserEntity(edxUserDistrictEntity.getDistrictID(), userEntity);
    if (optionalDistrict.isEmpty()) {
      edxUserDistrictEntity.getEdxUserDistrictRoleEntities().forEach(districtRole -> districtRole.setEdxUserDistrictEntity(edxUserDistrictEntity));
      return getEdxUserDistrictRepository().save(edxUserDistrictEntity);
    } else {
      throw new EntityExistsException("EdxUser to EdxUserDistrict association already exists");
    }
  }

  public EdxUserDistrictEntity updateEdxUserSchool(UUID edxUserID, EdxUserDistrictEntity edxUserDistrictEntity) {
    val entityOptional = getEdxUserRepository().findById(edxUserID);
    val userEntity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString()));

    //check for district
    val optionalDistrict = getEdxUserDistrictRepository().findEdxUserDistrictEntitiesByDistrictIDAndEdxUserEntity(edxUserDistrictEntity.getDistrictID(), userEntity);
    if (optionalDistrict.isPresent()) {
      EdxUserDistrictEntity currentEdxUserDistrictEntity = optionalDistrict.get();
      logUpdatesEdxUserDistrict(currentEdxUserDistrictEntity, edxUserDistrictEntity);
      BeanUtils.copyProperties(edxUserDistrictEntity, currentEdxUserDistrictEntity, "edxUserDistrictRoleEntities", "createUser", "createDate");

      currentEdxUserDistrictEntity.getEdxUserDistrictRoleEntities().clear();
      currentEdxUserDistrictEntity.getEdxUserDistrictRoleEntities().addAll(edxUserDistrictEntity.getEdxUserDistrictRoleEntities());

      //If we add a new role, we need to set the audit fields
      for (var districtRole : currentEdxUserDistrictEntity.getEdxUserDistrictRoleEntities()) {
        if (districtRole.getEdxUserDistrictRoleID() == null) {
          districtRole.setCreateDate(LocalDateTime.now());
          districtRole.setCreateUser(edxUserDistrictEntity.getUpdateUser());
          districtRole.setUpdateDate(LocalDateTime.now());
          districtRole.setUpdateUser(edxUserDistrictEntity.getUpdateUser());

          //since we are adding a new role, we need to link the role entity to the district entity (follows pattern from creating Edx User)
          districtRole.setEdxUserDistrictEntity(currentEdxUserDistrictEntity);
        }
      }

      return getEdxUserDistrictRepository().save(currentEdxUserDistrictEntity);
    } else {
      throw new EntityNotFoundException(EdxUserDistrictEntity.class, "EdxUserDistrictEntity", edxUserDistrictEntity.getEdxUserDistrictID().toString());
    }
  }

  public void deleteEdxDistrictUserById(UUID edxUserID, UUID edxUserDistrictID) {
    val entityOptional = getEdxUserDistrictRepository().findById(edxUserDistrictID);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserDistrictEntity.class, EDX_USER_DISTRICT_ID, edxUserDistrictID.toString()));
    if (entity.getEdxUserEntity().getEdxUserID().equals(edxUserID)) {
      this.getEdxUserDistrictRepository().delete(entity);
    } else {
      throw new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString());
    }
  }

  public void deleteEdxDistrictUserRoleById(UUID edxUserID, UUID edxUserDistrictRoleID) {
    val entityOptional = getEdxUserDistrictRoleRepository().findById(edxUserDistrictRoleID);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserDistrictRoleEntity.class, "edxUserDistrictRoleID", edxUserDistrictRoleID.toString()));
    if (entity.getEdxUserDistrictEntity() != null && entity.getEdxUserDistrictEntity().getEdxUserEntity().getEdxUserID().equals(edxUserID)) {
      this.getEdxUserDistrictRoleRepository().delete(entity);
    } else {
      throw new EntityNotFoundException(EdxUserDistrictRoleEntity.class, EDX_USER_ID, edxUserID.toString());
    }

  }

  public EdxUserDistrictRoleEntity createEdxUserDistrictRole(UUID edxUserID, UUID edxUserDistrictID, EdxUserDistrictRoleEntity userDistrictRoleEntity) {
    val optionalUserDistrictRoleEntity = getEdxUserDistrictRoleRepository().findEdxUserDistrictRoleEntityByEdxUserDistrictEntityEdxUserDistrictIDAndEdxRoleCode(userDistrictRoleEntity.getEdxUserDistrictEntity().getEdxUserDistrictID(), userDistrictRoleEntity.getEdxRoleCode());
    if (optionalUserDistrictRoleEntity.isEmpty()) {
      val optionalEdxUserDistrictEntity = getEdxUserDistrictRepository().findById(userDistrictRoleEntity.getEdxUserDistrictEntity().getEdxUserDistrictID());
      optionalEdxUserDistrictEntity.orElseThrow(() -> new EntityNotFoundException(EdxUserDistrictEntity.class, EDX_USER_DISTRICT_ID, edxUserDistrictID.toString()));
      EdxUserDistrictEntity userDistrictEntity = optionalEdxUserDistrictEntity.get();
      val optionEdxUserEntity = getEdxUserRepository().findById(userDistrictEntity.getEdxUserEntity().getEdxUserID());
      optionEdxUserEntity.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_DISTRICT_ID, edxUserDistrictID.toString()));
      if (edxUserID.equals(userDistrictEntity.getEdxUserEntity().getEdxUserID())) {
        return getEdxUserDistrictRoleRepository().save(userDistrictRoleEntity);
      } else {
        ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This EdxUserDistrictRole cannot be added for this EdxUser " + edxUserID).status(BAD_REQUEST).build();
        throw new InvalidPayloadException(error);
      }

    } else {
      throw new EntityExistsException("EdxUserDistrictRole to EdxUserDistrict association already exists");
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateUserRolesForClosedSchools() {
    List<SchoolTombstone> schools = restUtils.getSchools();
    List<EdxUserSchoolEntity> updatedUserSchoolEntities = new ArrayList<>();

//     closed date is not null and transcript eligibility is true and today's date is past closed date + 3 months
//     OR
//     closed date is not null and transcript eligibility is false and today's date is past closed date
    List<UUID> schoolsEligibleForUserRoleRemoval = schools.stream()
            .filter(school ->
                    (StringUtils.isNotBlank(school.getClosedDate())
                            && Boolean.TRUE.equals(school.getCanIssueTranscripts())
                            && LocalDateTime.parse(school.getClosedDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).plusMonths(3).isBefore(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)))
                            ||
                            (StringUtils.isNotBlank(school.getClosedDate())
                            && Boolean.FALSE.equals(school.getCanIssueTranscripts())
                            && LocalDateTime.parse(school.getClosedDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).isBefore(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT)))
            ).map(SchoolTombstone::getSchoolId)
            .map(UUID::fromString).toList();

    log.info("schoolsEligibleForUserRoleRemoval {}", schoolsEligibleForUserRoleRemoval.size());

    if(!schoolsEligibleForUserRoleRemoval.isEmpty()) {
      updatedUserSchoolEntities.addAll(removeUserRoles(schoolsEligibleForUserRoleRemoval));
    }

    //closed date is not null and transcript eligibility is true and today's date is after closed date and before closed date + 3 months
    List<UUID> transcriptEligibleClosedSchools = schools.stream()
            .filter(school -> StringUtils.isNotBlank(school.getClosedDate())
                    && Boolean.TRUE.equals(school.getCanIssueTranscripts())
                    && (LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).isAfter(LocalDateTime.parse(school.getClosedDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    || LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).isEqual(LocalDateTime.parse(school.getClosedDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME)))
                    && LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT).isBefore(LocalDateTime.parse(school.getClosedDate(), DateTimeFormatter.ISO_LOCAL_DATE_TIME).plusMonths(3)))
            .map(SchoolTombstone::getSchoolId)
            .map(UUID::fromString).toList();

    if(!transcriptEligibleClosedSchools.isEmpty()) {
      updatedUserSchoolEntities.addAll(updateUsersForTranscriptEligibleSchools(transcriptEligibleClosedSchools));
    }

    log.info("transcriptEligibleClosedSchools {}", transcriptEligibleClosedSchools.size());

    log.info("users to update {}", updatedUserSchoolEntities.size());
    edxUserSchoolsRepository.saveAll(updatedUserSchoolEntities);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void removeGradAdminRoleIfExists(School school) {
    List<EdxUserSchoolEntity> edxSchoolUsers = edxUserSchoolsRepository.findAllBySchoolID(UUID.fromString(school.getSchoolId()));
    if(!edxSchoolUsers.isEmpty()) {
      List<EdxUserSchoolEntity> usersWithGradRole = edxSchoolUsers.stream().filter(user -> user.getEdxUserSchoolRoleEntities().stream().anyMatch(role -> role.getEdxRoleCode().equalsIgnoreCase(GRAD_SCHOOL_ADMIN_ROLE))).toList();
      if(!usersWithGradRole.isEmpty()) {
        usersWithGradRole.forEach(role -> {
          List<EdxUserSchoolRoleEntity> rolesWithoutGradAdmin = role.getEdxUserSchoolRoleEntities().stream().filter(schoolRole -> !schoolRole.getEdxRoleCode().equalsIgnoreCase(GRAD_SCHOOL_ADMIN_ROLE)).toList();
          role.getEdxUserSchoolRoleEntities().clear();
          role.getEdxUserSchoolRoleEntities().addAll(rolesWithoutGradAdmin);
          edxUserSchoolsRepository.save(role);
        });
      }
    }
  }

  private List<EdxUserSchoolEntity> updateUsersForTranscriptEligibleSchools(List<UUID> transcriptEligibleClosedSchools) {
    List<EdxUserSchoolEntity> usersWithRolesToBeUpdated = new ArrayList<>();
    List<EdxUserSchoolEntity> edxSchoolUsers = edxUserSchoolsRepository.findAllBySchoolIDIn(transcriptEligibleClosedSchools);
    log.info("edxSchoolUsers {}", edxSchoolUsers.size());
    List<EdxUserSchoolEntity> usersWithGradRole = edxSchoolUsers.stream().filter(user -> user.getEdxUserSchoolRoleEntities().stream().anyMatch(role ->
            role.getEdxRoleCode().equalsIgnoreCase(GRAD_SCHOOL_ADMIN_ROLE))).toList();
    List<EdxUserSchoolEntity> usersWithoutGradRole = edxSchoolUsers.stream().filter(user -> user.getEdxUserSchoolRoleEntities().stream().noneMatch(role ->
            role.getEdxRoleCode().equalsIgnoreCase(GRAD_SCHOOL_ADMIN_ROLE))).toList();
    log.info("usersWithRolesToBeUpdated {}", usersWithGradRole.size());
    usersWithGradRole.forEach(schoolUser -> {
      schoolUser.setCreateDate(LocalDateTime.now());
      schoolUser.setCreateUser(ApplicationProperties.CLIENT_ID);
      schoolUser.setUpdateDate(LocalDateTime.now());
      schoolUser.setUpdateUser(ApplicationProperties.CLIENT_ID);

      List<EdxUserSchoolRoleEntity> allowedRoles = schoolUser.getEdxUserSchoolRoleEntities().stream().filter(userSchoolRole ->
        userSchoolRole.getEdxRoleCode().equalsIgnoreCase(GRAD_SCHOOL_ADMIN_ROLE) || userSchoolRole.getEdxRoleCode().equalsIgnoreCase(SECURE_EXCHANGE_SCHOOL_ROLE)
      ).toList();
      schoolUser.getEdxUserSchoolRoleEntities().clear();
      schoolUser.getEdxUserSchoolRoleEntities().addAll(allowedRoles);
    });

    usersWithoutGradRole.forEach(user -> user.getEdxUserSchoolRoleEntities().clear());
    usersWithRolesToBeUpdated.addAll(usersWithGradRole);
    usersWithRolesToBeUpdated.addAll(usersWithoutGradRole);
    return usersWithRolesToBeUpdated;
  }

  private List<EdxUserSchoolEntity> removeUserRoles(List<UUID> schoolIdsForRemovingUserRole) {
    List<EdxUserSchoolEntity> edxClosedSchoolUsers = edxUserSchoolsRepository.findAllBySchoolIDIn(schoolIdsForRemovingUserRole);
    List<EdxUserSchoolEntity> usersWithRoles = edxClosedSchoolUsers.stream().filter(user -> !user.getEdxUserSchoolRoleEntities().isEmpty()).toList();
    usersWithRoles.forEach(user -> user.getEdxUserSchoolRoleEntities().clear());
    return usersWithRoles;
  }
}
