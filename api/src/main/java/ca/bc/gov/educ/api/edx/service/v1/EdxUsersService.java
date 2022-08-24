package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.constants.InstituteTypeCode;
import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivateUser;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxPrimaryActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import ca.bc.gov.educ.api.edx.utils.TransformUtil;
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
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityExistsException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.http.HttpStatus.*;

/**
 * The type Edx users service.
 */
@Service
@Slf4j
public class EdxUsersService {

  /**
   * The Ministry ownership team repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository;

  /**
   * The Edx user repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxUserRepository edxUserRepository;

  /**
   * The Edx user schools repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxUserSchoolRepository edxUserSchoolsRepository;

  /**
   * The Edx user school role repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxUserSchoolRoleRepository edxUserSchoolRoleRepository;

  /**
   * The Edx role repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxRoleRepository edxRoleRepository;


  /**
   * The Edx activation code repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeRepository edxActivationCodeRepository;

  /**
   * The Edx activation role repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationRoleRepository edxActivationRoleRepository;

  /**
   * The constant EDX_USER_ID.
   */
  private static final String EDX_USER_ID = "edxUserID";

  /**
   * The constant EDX_ACTIVATION_CODE_ID.
   */
  private static final String EDX_ACTIVATION_CODE_ID = "edxActivationCodeId";

  /**
   * The constant INSTITUTE_IDENTIFIER.
   */
  private static final String INSTITUTE_IDENTIFIER = "institute_identifier";
  /**
   * The Props.
   */
  @Getter(AccessLevel.PRIVATE)
  private final ApplicationProperties props;

  /**
   * Instantiates a new Edx users service.
   *
   * @param ministryOwnershipTeamRepository the ministry ownership team repository
   * @param edxUserSchoolsRepository        the edx user schools repository
   * @param edxUserRepository               the edx user repository
   * @param edxUserSchoolRoleRepository     the edx user school role repository
   * @param edxRoleRepository               the edx role repository
   * @param edxActivationCodeRepository     the edx activation code repository
   * @param edxActivationRoleRepository     the edx activation role repository
   * @param props                           the props
   */
  @Autowired
  public EdxUsersService(final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository, final EdxUserSchoolRepository edxUserSchoolsRepository, final EdxUserRepository edxUserRepository, EdxUserSchoolRoleRepository edxUserSchoolRoleRepository, EdxRoleRepository edxRoleRepository, EdxActivationCodeRepository edxActivationCodeRepository, EdxActivationRoleRepository edxActivationRoleRepository, ApplicationProperties props) {
    this.ministryOwnershipTeamRepository = ministryOwnershipTeamRepository;
    this.edxUserSchoolsRepository = edxUserSchoolsRepository;
    this.edxUserRepository = edxUserRepository;
    this.edxUserSchoolRoleRepository = edxUserSchoolRoleRepository;
    this.edxRoleRepository = edxRoleRepository;
    this.edxActivationCodeRepository = edxActivationCodeRepository;
    this.edxActivationRoleRepository = edxActivationRoleRepository;
    this.props = props;
  }

  /**
   * Gets ministry teams list.
   *
   * @return the ministry teams list
   */
  public List<MinistryOwnershipTeamEntity> getMinistryTeamsList() {
    return this.getMinistryOwnershipTeamRepository().findAll();
  }

  /**
   * Gets edx user schools list.
   *
   * @return the edx user schools list
   */
  public List<EdxUserSchoolEntity> getEdxUserSchoolsList() {
    return this.getEdxUserSchoolsRepository().findAll();
  }

  /**
   * Retrieve edx user by id edx user entity.
   *
   * @param edxUserID the edx user id
   * @return the edx user entity
   */
  public EdxUserEntity retrieveEdxUserByID(final UUID edxUserID) {
    var res = this.getEdxUserRepository().findById(edxUserID);
    if (res.isPresent()) {
      return res.get();
    } else {
      throw new EntityNotFoundException(EdxUser.class, EDX_USER_ID, edxUserID.toString());
    }
  }

  /**
   * Gets edx user schools list.
   *
   * @param permissionCode the permission code
   * @return the edx user schools list
   */
  public List<String> getEdxUserSchoolsList(String permissionCode) {
    return this.getEdxUserSchoolsRepository().findSchoolsByPermission(permissionCode);
  }

  /**
   * Find edx users list.
   *
   * @param digitalId the digital id
   * @param mincode   the mincode
   * @param firstName the first name
   * @param lastName  the last name
   * @return the list
   */
  public List<EdxUserEntity> findEdxUsers(Optional<UUID> digitalId, String mincode, String firstName, String lastName) {
    return this.getEdxUserRepository().findEdxUsers(digitalId, mincode, firstName, lastName);
  }

  /**
   * Create edx user edx user entity.
   *
   * @param edxUserEntity the edx user entity
   * @return the edx user entity
   */
  public EdxUserEntity createEdxUser(EdxUserEntity edxUserEntity) {
    for (var entity : edxUserEntity.getEdxUserSchoolEntities()) {
      entity.setEdxUserEntity(edxUserEntity);
    }
    return this.getEdxUserRepository().save(edxUserEntity);
  }

  /**
   * Create edx user school edx user school entity.
   *
   * @param edxUserID           the edx user id
   * @param edxUserSchoolEntity the edx user school entity
   * @return the edx user school entity
   */
  public EdxUserSchoolEntity createEdxUserSchool(UUID edxUserID, EdxUserSchoolEntity edxUserSchoolEntity) {
    val entityOptional = getEdxUserRepository().findById(edxUserID);
    val userEntity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString()));
    val optionalSchool = getEdxUserSchoolsRepository().findEdxUserSchoolEntitiesByMincodeAndEdxUserEntity(edxUserSchoolEntity.getMincode(), userEntity);
    if (optionalSchool.isEmpty()) {
      edxUserSchoolEntity.getEdxUserSchoolRoleEntities().forEach(schoolRole -> schoolRole.setEdxUserSchoolEntity(edxUserSchoolEntity));
      return getEdxUserSchoolsRepository().save(edxUserSchoolEntity);
    } else {
      throw new EntityExistsException("EdxUser to EdxUserSchool association already exists");
    }
  }

  /**
   * Update edx user school edx user school entity.
   *
   * @param edxUserID           the edx user id
   * @param edxUserSchoolEntity the edx user school entity
   * @return the edx user school entity
   */
  public EdxUserSchoolEntity updateEdxUserSchool(UUID edxUserID, EdxUserSchoolEntity edxUserSchoolEntity) {
    val entityOptional = getEdxUserRepository().findById(edxUserID);
    val userEntity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString()));

    //check for school
    val optionalSchool = getEdxUserSchoolsRepository().findEdxUserSchoolEntitiesByMincodeAndEdxUserEntity(edxUserSchoolEntity.getMincode(), userEntity);
    if (optionalSchool.isPresent()) {
      EdxUserSchoolEntity currentEdxUserSchoolEntity = optionalSchool.get();
      logUpdatesEdxUserSchool(currentEdxUserSchoolEntity, edxUserSchoolEntity);
      BeanUtils.copyProperties(edxUserSchoolEntity, currentEdxUserSchoolEntity, "edxUserSchoolRoleEntities", "createUser", "createDate");

      currentEdxUserSchoolEntity.getEdxUserSchoolRoleEntities().clear();
      currentEdxUserSchoolEntity.getEdxUserSchoolRoleEntities().addAll(edxUserSchoolEntity.getEdxUserSchoolRoleEntities());

      //If we add a new role, we need to set the audit fields
      for(var schoolRole: currentEdxUserSchoolEntity.getEdxUserSchoolRoleEntities()) {
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

  /**
   * Activate school user edx user entity.
   *
   * @param edxActivateUser the edx activate user
   * @return the edx user entity
   */
  public EdxUserEntity activateSchoolUser(EdxActivateUser edxActivateUser) {
    val acCodes = Arrays.asList(edxActivateUser.getPersonalActivationCode(), edxActivateUser.getPrimaryEdxCode());
    val activationCodes = edxActivationCodeRepository.findEdxActivationCodeByActivationCodeInAndMincode(acCodes, edxActivateUser.getMincode());
    if (activationCodes.size() == 2) {
      EdxActivationCodeEntity userCodeEntity = null;
      for(val activationCode: activationCodes){
        if(!activationCode.getIsPrimary()){
          userCodeEntity = activationCode;
          if(activationCode.getEdxUserId() != null) {
            edxActivateUser.setEdxUserId(activationCode.getEdxUserId().toString());
          }
        }
        if (activationCode.getExpiryDate() != null && activationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
          ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This Activation Code has expired").status(BAD_REQUEST).build();
          throw new InvalidPayloadException(error);
        }
      }

      //Activate User
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
          verifyExistingUserMincodeAssociation(edxActivateUser, edxUsers);

          //add the user_school and school_role to user to the edx_user
          return updateEdxUserDetailsFromActivationCodeDetails(edxUsers, userCodeEntity, edxActivateUser);
        }
      }
    } else {
      throw new EntityNotFoundException(EdxActivationCode.class, EDX_ACTIVATION_CODE_ID, edxActivateUser.getPrimaryEdxCode());
    }
  }

  /**
   * Verify existing user mincode association.
   *
   * @param edxActivateUser the edx activate user
   * @param edxUsers        the edx users
   */
  private void verifyExistingUserMincodeAssociation(EdxActivateUser edxActivateUser, List<EdxUserEntity> edxUsers) {
    val existingUser = edxUsers.get(0);
    if (!CollectionUtils.isEmpty(existingUser.getEdxUserSchoolEntities())) {
      for (EdxUserSchoolEntity schoolEntity : existingUser.getEdxUserSchoolEntities()) {
        if (schoolEntity.getMincode().equalsIgnoreCase(edxActivateUser.getMincode())) {
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
    if(digitalIdentityEdxUserList != null && digitalIdentityEdxUserList.size() > 0){
      edxUserEntity = digitalIdentityEdxUserList.get(0);
    }else{
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
    val edxUserSchoolEntity = createEdxUserSchoolFromActivationCodeDetails(edxActivationCodeEntity, edxUserEntity, edxActivateUser);
    val edxUserSchoolRoleEntities = createEdxUserSchoolRolesFromActivationCodeDetails(edxActivationCodeEntity.getEdxActivationRoleEntities(), edxUserSchoolEntity, edxActivateUser);
    //updating associations
    updateEdxUserAssociations(edxUserEntity, edxUserSchoolEntity, edxUserSchoolRoleEntities);
    updateAuditColumnsForEdxUserEntityUpdate(edxUserEntity, edxActivateUser);
    val updatedUser = edxUserRepository.save(edxUserEntity);
    expireActivationCodes(edxActivationCodeEntity, edxActivateUser);
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
    val edxUserSchoolEntity = createEdxUserSchoolFromActivationCodeDetails(edxActivationCodeEntity, edxUserEntity, edxActivateUser);
    val edxUserSchoolRoleEntities = createEdxUserSchoolRolesFromActivationCodeDetails(edxActivationCodeEntity.getEdxActivationRoleEntities(), edxUserSchoolEntity, edxActivateUser);
    //updating associations
    updateEdxUserAssociations(edxUserEntity, edxUserSchoolEntity, edxUserSchoolRoleEntities);
    val savedEntity = edxUserRepository.save(edxUserEntity);
    //expire the activationCodes
    expireActivationCodes(edxActivationCodeEntity, edxActivateUser);
    return savedEntity;
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

  /**
   * Expire activation codes.
   *
   * @param edxActivationCode the edx activation code
   * @param edxActivateUser   the edx activate user
   */
  private void expireActivationCodes(EdxActivationCodeEntity edxActivationCode, EdxActivateUser edxActivateUser) {
    val optionalEdxActivationCodeEntity = getEdxActivationCodeRepository().findById(edxActivationCode.getEdxActivationCodeId());
    val activationCodeEntity = optionalEdxActivationCodeEntity.orElseThrow(() -> new EntityNotFoundException(EdxActivationCodeEntity.class, EDX_ACTIVATION_CODE_ID, edxActivationCode.getEdxActivationCodeId().toString()));
    if (!activationCodeEntity.getIsPrimary()) {//expire only the personal activation code
      activationCodeEntity.setExpiryDate(LocalDateTime.now());
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
    userSchoolEntity.setMincode(activationCode.getMincode());
    updateAuditColumnsForEdxUserSchoolEntity(edxActivateUser, userSchoolEntity);
    return userSchoolEntity;
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
    if (activationCodeEntities.stream().anyMatch(el -> el.getIsUrlClicked().equals(Boolean.TRUE))) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This User Activation Link has already expired").status(GONE).build();
      throw new InvalidPayloadException(error);
    }
    if (activationCodeEntities.stream().anyMatch(el -> el.getExpiryDate().isBefore(LocalDateTime.now()))) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This User Activation Link has already expired").status(GONE).build();
      throw new InvalidPayloadException(error);
    }
    activationCodeEntities.forEach(activationCode -> {
      activationCode.setIsUrlClicked(Boolean.TRUE);
      activationCode.setUpdateDate(LocalDateTime.now());
      getEdxActivationCodeRepository().save(activationCode);
    });
    if(activationCodeEntities.get(0).getMincode() != null){
      return  InstituteTypeCode.SCHOOL;
    }else{
      return  InstituteTypeCode.DISTRICT;
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
    edxActivationCodeEntity.setIsUrlClicked(Boolean.FALSE);
    edxActivationCodeEntity.setIsPrimary(Boolean.FALSE);
    return createEdxActivationCode(edxActivationCodeEntity);
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
   * @param instituteType the instituteType
   * @param instituteIdentifier the instituteIdentifier
   * @return the edx activation code entity
   */
  public EdxActivationCodeEntity findPrimaryEdxActivationCode(InstituteTypeCode instituteType, String instituteIdentifier) {
    Optional<EdxActivationCodeEntity> primaryEdxActivationCode = this.findPrimaryEdxActivationCodeForInstitute(instituteType, instituteIdentifier);
    if (primaryEdxActivationCode.isEmpty()) {
      throw new EntityNotFoundException(EdxActivationCodeEntity.class, INSTITUTE_IDENTIFIER, instituteIdentifier);
    }
    return primaryEdxActivationCode.get();
  }

  /**
   * Generate or regenerate primary edx activation code edx activation code entity.
   *
   * @param instituteType the instituteType
   * @param instituteIdentifier the instituteIdentifier
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
        return getEdxActivationCodeRepository().findEdxActivationCodeEntitiesByMincodeAndIsPrimaryTrue(contactIdentifier);
      case DISTRICT:
        return getEdxActivationCodeRepository().findEdxActivationCodeEntitiesByDistrictCodeAndIsPrimaryTrue(contactIdentifier);
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
    int byteLength = 6; //Base64 encoding an input of 6 bytes will generate a string of 8 characters.
    byte[] randomBytes = new byte[byteLength];
    SecureRandom.getInstance("SHA1PRNG").nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).toUpperCase();
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
    toReturn.setMincode(edxPrimaryActivationCode.getMincode());
    toReturn.setDistrictCode(edxPrimaryActivationCode.getDistrictCode());
    toReturn.setIsPrimary(true);
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
   * Find edx user email by mincode and permission code set.
   *
   * @param mincode        the mincode
   * @param permissionCode the permission code
   * @return the set
   */
  public Set<String> findEdxUserEmailByMincodeAndPermissionCode(String mincode, String permissionCode){
    return getEdxUserRepository().findEdxUserEmailByMincodeAndPermissionCode(mincode, permissionCode);
  }

  public List<EdxRoleEntity> findAllEdxRolesForInstituteTypeCode(InstituteTypeCode instituteTypeCode) {
    Boolean isDistrictRole = InstituteTypeCode.DISTRICT.equals(instituteTypeCode);
    ExampleMatcher customExampleMatcher = ExampleMatcher.matchingAny()
      .withMatcher("isDistrictRole", ExampleMatcher.GenericPropertyMatchers.exact());
    EdxRoleEntity entity = new EdxRoleEntity();
    entity.setIsDistrictRole(isDistrictRole);
    return this.getEdxRoleRepository().findAll(Example.of(entity, customExampleMatcher));
  }
}
