package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.model.v1.*;
import ca.bc.gov.educ.api.edx.repository.*;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivateUser;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityExistsException;
import java.time.LocalDateTime;
import java.util.*;

import static org.springframework.http.HttpStatus.*;

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
  private final EdxUserSchoolRoleRepository edxUserSchoolRoleRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxRoleRepository edxRoleRepository;


  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeRepository edxActivationCodeRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationRoleRepository edxActivationRoleRepository;

  private static final String EDX_USER_ID = "edxUserID";

  private static final String EDX_ACTIVATION_CODE_ID = "edxActivationCodeId";

  private static final String MINCODE = "mincode";

  @Autowired
  public EdxUsersService(final MinistryOwnershipTeamRepository ministryOwnershipTeamRepository, final EdxUserSchoolRepository edxUserSchoolsRepository, final EdxUserRepository edxUserRepository, EdxUserSchoolRoleRepository edxUserSchoolRoleRepository, EdxRoleRepository edxRoleRepository, EdxActivationCodeRepository edxActivationCodeRepository, EdxActivationRoleRepository edxActivationRoleRepository) {
    this.ministryOwnershipTeamRepository = ministryOwnershipTeamRepository;
    this.edxUserSchoolsRepository = edxUserSchoolsRepository;
    this.edxUserRepository = edxUserRepository;
    this.edxUserSchoolRoleRepository = edxUserSchoolRoleRepository;
    this.edxRoleRepository = edxRoleRepository;
    this.edxActivationCodeRepository = edxActivationCodeRepository;
    this.edxActivationRoleRepository = edxActivationRoleRepository;
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

  public List<String> getEdxUserSchoolsList(String permissionName) {
    return this.getEdxUserSchoolsRepository().findSchoolsByPermission(permissionName);
  }

  public List<EdxUserEntity> findEdxUsers(Optional<UUID> digitalId, String mincode, String firstName, String lastName) {
//    List<EdxUserEntity> edxUsers = this.getEdxUserRepository().findEdxUsers(digitalId, mincode,firstName,lastName);

////    Remove districts and other schools info when searching with mincode.
//    if (StringUtils.isNotBlank(mincode)) {
//      edxUsers.stream()
//        .forEach(user -> {
//            var filteredSchools = user.getEdxUserSchoolEntities().stream()
//              .filter(school -> school.getMincode().equals(mincode))
//              .collect(Collectors.toSet());
//
//            user.setEdxUserSchoolEntities(filteredSchools);
//            user.getEdxUserDistrictEntities().clear();
//          }
//        );
//    }

    return this.getEdxUserRepository().findEdxUsers(digitalId, mincode,firstName,lastName);
  }

  public EdxUserEntity createEdxUser(EdxUserEntity edxUserEntity) {
    for(var entity: edxUserEntity.getEdxUserSchoolEntities()){
      entity.setEdxUserEntity(edxUserEntity);
    }
    return this.getEdxUserRepository().save(edxUserEntity);
  }

  public EdxUserSchoolEntity createEdxUserSchool(UUID edxUserID, EdxUserSchoolEntity edxUserSchoolEntity) {
    val entityOptional = getEdxUserRepository().findById(edxUserID);
    val userEntity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString()));
    val optionalSchool = getEdxUserSchoolsRepository().findEdxUserSchoolEntitiesByMincodeAndEdxUserEntity(edxUserSchoolEntity.getMincode(), userEntity);
    if (optionalSchool.isEmpty()) {
      return getEdxUserSchoolsRepository().save(edxUserSchoolEntity);
    } else {
      throw new EntityExistsException("EdxUser to EdxUserSchool association already exists");
    }
  }

  public EdxUserSchoolRoleEntity createEdxUserSchoolRole(UUID edxUserID, UUID edxUserSchoolId, EdxUserSchoolRoleEntity edxUserSchoolRoleEntity) {
    val optionalUserSchoolRoleEntity = getEdxUserSchoolRoleRepository().findEdxUserSchoolRoleEntityByEdxUserSchoolEntity_EdxUserSchoolIDAndEdxRoleEntity_EdxRoleID(edxUserSchoolRoleEntity.getEdxUserSchoolEntity().getEdxUserSchoolID(), edxUserSchoolRoleEntity.getEdxRoleEntity().getEdxRoleID());
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

  public void deleteEdxUserById(UUID edxUserID) {
    val entityOptional = getEdxUserRepository().findById(edxUserID);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString()));
    this.getEdxUserRepository().delete(entity);

  }

  public void deleteEdxSchoolUserById(UUID edxUserID, UUID edxUserSchoolId) {
    val entityOptional = getEdxUserSchoolsRepository().findById(edxUserSchoolId);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserSchoolEntity.class, "edxUserSchoolID", edxUserSchoolId.toString()));
    if (entity.getEdxUserEntity().getEdxUserID().equals(edxUserID)) {
      this.getEdxUserSchoolsRepository().delete(entity);
    } else {
      throw new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxUserID.toString());
    }
  }

  public void deleteEdxSchoolUserRoleById(UUID edxUserID, UUID edxUserSchoolRoleId) {
    val entityOptional = getEdxUserSchoolRoleRepository().findById(edxUserSchoolRoleId);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxUserSchoolRoleEntity.class, "edxUserSchoolRoleID", edxUserSchoolRoleId.toString()));
    if (entity.getEdxUserSchoolEntity() != null && entity.getEdxUserSchoolEntity().getEdxUserEntity().getEdxUserID().equals(edxUserID)) {
      this.getEdxUserSchoolRoleRepository().delete(entity);
    } else {
      throw new EntityNotFoundException(EdxUserSchoolRoleEntity.class, EDX_USER_ID, edxUserID.toString());
    }

  }

  public List<EdxRoleEntity> findAllEdxRoles() {
    return this.getEdxRoleRepository().findAll();
  }

  public EdxUserEntity activateSchoolUser(EdxActivateUser edxActivateUser) {
    val acCodes = Arrays.asList(edxActivateUser.getPersonalActivationCode(), edxActivateUser.getPrimaryEdxCode());
    val activationCodes = edxActivationCodeRepository.findEdxActivationCodeByActivationCodeInAndMincode(acCodes, edxActivateUser.getMincode());
    if (activationCodes.size() == 2) {
      activationCodes.forEach(activationCode -> {
        if (activationCode.getExpiryDate().isBefore(LocalDateTime.now())) {
          ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("This Activation Code has expired").status(BAD_REQUEST).build();
          throw new InvalidPayloadException(error);
        }
      });
      //Activate User
      if (edxActivateUser.getEdxUserId() != null) {
        //relink user logic
        return relinkEdxUser(edxActivateUser, activationCodes);

      } else {
        //activate user logic
        val edxUsers = getEdxUserRepository().findEdxUserEntitiesByDigitalIdentityID(UUID.fromString(edxActivateUser.getDigitalId()));
        if (edxUsers.isEmpty()) {
          //create completely new user
          return createEdxUserDetailsFromActivationCodeDetails(edxActivateUser, activationCodes);
        } else {
          verifyExistingUserMincodeAssociation(edxActivateUser, edxUsers);

          //add the user_school and school_role to user to the edx_user
          return updateEdxUserDetailsFromActivationCodeDetails(edxUsers, activationCodes, edxActivateUser);
        }
      }
    } else {
      throw new EntityNotFoundException(EdxActivationCode.class, EDX_ACTIVATION_CODE_ID, edxActivateUser.getPrimaryEdxCode());
    }
  }

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

  private EdxUserEntity relinkEdxUser(EdxActivateUser edxActivateUser, List<EdxActivationCodeEntity> activationCodes) {
    val edxActivationCodeEntity = activationCodes.get(0);
    val optionalEdxUserEntity = getEdxUserRepository().findById(UUID.fromString(edxActivateUser.getEdxUserId()));
    val edxUserEntity = optionalEdxUserEntity.orElseThrow(() -> new EntityNotFoundException(EdxUserEntity.class, EDX_USER_ID, edxActivateUser.getEdxUserId()));
    val updatedEdxUser = createEdxUserFromActivationCodeDetails(edxUserEntity, edxActivateUser, edxActivationCodeEntity);
    val savedEdxUser = getEdxUserRepository().save(updatedEdxUser);
    expireActivationCodes(activationCodes, edxActivateUser);
    return savedEdxUser;

  }

  private EdxUserEntity updateEdxUserDetailsFromActivationCodeDetails(List<EdxUserEntity> edxUsers, List<EdxActivationCodeEntity> activationCodes, EdxActivateUser edxActivateUser) {
    val edxActivationCodeEntity = activationCodes.get(0);
    val edxUserEntity = getEdxUserRepository().findById(edxUsers.get(0).getEdxUserID()).get();
    val edxUserSchoolEntity = createEdxUserSchoolFromActivationCodeDetails(edxActivationCodeEntity, edxUserEntity, edxActivateUser);
    val edxUserSchoolRoleEntities = createEdxUserSchoolRolesFromActivationCodeDetails(edxActivationCodeEntity.getEdxActivationRoleEntities(), edxUserSchoolEntity, edxActivateUser);
    //updating associations
    updateEdxUserAssociations(edxUserEntity, edxUserSchoolEntity, edxUserSchoolRoleEntities);
    updateAuditColumnsForEdxUserEntityUpdate(edxUserEntity, edxActivateUser);
    val updatedUser = edxUserRepository.save(edxUserEntity);
    expireActivationCodes(activationCodes, edxActivateUser);
    return updatedUser;
  }

  private EdxUserEntity createEdxUserDetailsFromActivationCodeDetails(EdxActivateUser edxActivateUser, List<EdxActivationCodeEntity> activationCodes) {
    val edxActivationCodeEntity = activationCodes.get(0);
    EdxUserEntity edxUser = new EdxUserEntity();
    val edxUserEntity = createEdxUserFromActivationCodeDetails(edxUser, edxActivateUser, edxActivationCodeEntity);
    val edxUserSchoolEntity = createEdxUserSchoolFromActivationCodeDetails(edxActivationCodeEntity, edxUserEntity, edxActivateUser);
    val edxUserSchoolRoleEntities = createEdxUserSchoolRolesFromActivationCodeDetails(edxActivationCodeEntity.getEdxActivationRoleEntities(), edxUserSchoolEntity, edxActivateUser);
    //updating associations
    updateEdxUserAssociations(edxUserEntity, edxUserSchoolEntity, edxUserSchoolRoleEntities);
    val savedEntity = edxUserRepository.save(edxUserEntity);
    //expire the activationCodes
    expireActivationCodes(activationCodes, edxActivateUser);
    return savedEntity;
  }

  private void updateEdxUserAssociations(EdxUserEntity edxUserEntity, EdxUserSchoolEntity edxUserSchoolEntity, Set<EdxUserSchoolRoleEntity> edxUserSchoolRoleEntities) {
    for (EdxUserSchoolRoleEntity edxUserSchoolRole : edxUserSchoolRoleEntities) {
      edxUserSchoolRole.setEdxUserSchoolEntity(edxUserSchoolEntity);
      edxUserSchoolEntity.getEdxUserSchoolRoleEntities().add(edxUserSchoolRole);
    }
    edxUserSchoolEntity.setEdxUserEntity(edxUserEntity);
    edxUserEntity.getEdxUserSchoolEntities().add(edxUserSchoolEntity);
  }

  private void expireActivationCodes(List<EdxActivationCodeEntity> activationCodes, EdxActivateUser edxActivateUser) {
    activationCodes.forEach(edxActivationCode -> {
      val optionalEdxActivationCodeEntity = getEdxActivationCodeRepository().findById(edxActivationCode.getEdxActivationCodeId());
      val activationCodeEntity = optionalEdxActivationCodeEntity.orElseThrow(() -> new EntityNotFoundException(EdxActivationCodeEntity.class, EDX_ACTIVATION_CODE_ID, edxActivationCode.getEdxActivationCodeId().toString()));
      activationCodeEntity.setExpiryDate(LocalDateTime.now());
      activationCodeEntity.setUpdateUser(edxActivateUser.getUpdateUser());
      activationCodeEntity.setUpdateDate(LocalDateTime.now());
      getEdxActivationCodeRepository().save(activationCodeEntity);
    });
  }

  private Set<EdxUserSchoolRoleEntity> createEdxUserSchoolRolesFromActivationCodeDetails(Set<EdxActivationRoleEntity> edxActivationRoleEntities, EdxUserSchoolEntity edxUserSchoolEntity, EdxActivateUser edxActivateUser) {
    Set<EdxUserSchoolRoleEntity> schoolRoleEntitySet = new HashSet<>();
    edxActivationRoleEntities.forEach(edxActivationRoleEntity -> {
      EdxUserSchoolRoleEntity schoolRoleEntity = new EdxUserSchoolRoleEntity();
      schoolRoleEntity.setEdxUserSchoolEntity(edxUserSchoolEntity);
      schoolRoleEntity.setEdxRoleEntity(getEdxRoleRepository().getById(edxActivationRoleEntity.getEdxRoleId()));
      updateAuditColumnsForEdxUserSchoolRoleEntity(edxActivateUser, schoolRoleEntity);
      schoolRoleEntitySet.add(schoolRoleEntity);
    });
    return schoolRoleEntitySet;
  }

  private EdxUserSchoolEntity createEdxUserSchoolFromActivationCodeDetails(EdxActivationCodeEntity activationCode, EdxUserEntity edxUser, EdxActivateUser edxActivateUser) {
    EdxUserSchoolEntity userSchoolEntity = new EdxUserSchoolEntity();
    userSchoolEntity.setEdxUserEntity(edxUser);
    userSchoolEntity.setMincode(activationCode.getMincode());
    updateAuditColumnsForEdxUserSchoolEntity(edxActivateUser, userSchoolEntity);
    return userSchoolEntity;
  }

  private EdxUserEntity createEdxUserFromActivationCodeDetails(EdxUserEntity edxUser, EdxActivateUser edxActivateUser, EdxActivationCodeEntity activationCode) {
    edxUser.setFirstName(activationCode.getFirstName());
    edxUser.setLastName(activationCode.getLastName());
    edxUser.setEmail(activationCode.getEmail());
    edxUser.setDigitalIdentityID(UUID.fromString(edxActivateUser.getDigitalId()));
    updateAuditColumnsForEdxUserEntityCreate(edxUser, edxActivateUser);
    return edxUser;
  }

  private void updateAuditColumnsForEdxUserEntityCreate(EdxUserEntity edxUser, EdxActivateUser edxActivateUser) {
    edxUser.setCreateUser(edxActivateUser.getCreateUser());
    edxUser.setCreateDate(LocalDateTime.now());
    updateAuditColumnsForEdxUserEntityUpdate(edxUser, edxActivateUser);
  }

  private void updateAuditColumnsForEdxUserEntityUpdate(EdxUserEntity edxUser, EdxActivateUser edxActivateUser) {
    edxUser.setUpdateUser(edxActivateUser.getUpdateUser());
    edxUser.setUpdateDate(LocalDateTime.now());
  }

  private void updateAuditColumnsForEdxUserSchoolEntity(EdxActivateUser edxActivateUser, EdxUserSchoolEntity userSchoolEntity) {
    userSchoolEntity.setCreateUser(edxActivateUser.getCreateUser());
    userSchoolEntity.setUpdateUser(edxActivateUser.getUpdateUser());
    userSchoolEntity.setCreateDate(LocalDateTime.now());
    userSchoolEntity.setUpdateDate(LocalDateTime.now());
  }

  private void updateAuditColumnsForEdxUserSchoolRoleEntity(EdxActivateUser edxActivateUser, EdxUserSchoolRoleEntity schoolRoleEntity) {
    schoolRoleEntity.setCreateUser(edxActivateUser.getCreateUser());
    schoolRoleEntity.setUpdateUser(edxActivateUser.getUpdateUser());
    schoolRoleEntity.setCreateDate(LocalDateTime.now());
    schoolRoleEntity.setUpdateDate(LocalDateTime.now());
  }

  public void expireUserActivationUrl(UUID userActivationValidationCode) {
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
      getEdxActivationCodeRepository().save(activationCode);
    });
  }

  public EdxActivationCodeEntity createEdxActivationCode(EdxActivationCodeEntity edxActivationCodeEntity) {
    edxActivationCodeEntity.setValidationCode(UUID.randomUUID());
    if (!CollectionUtils.isEmpty(edxActivationCodeEntity.getEdxActivationRoleEntities())) {
      List<UUID> roleIdList = new ArrayList<>();
      for (val activationRole : edxActivationCodeEntity.getEdxActivationRoleEntities()) {
        activationRole.setEdxActivationCodeEntity(edxActivationCodeEntity); //bi-directional association
        roleIdList.add(activationRole.getEdxRoleId());
      }
      val roleListFromDB = getEdxRoleRepository().findAllById(roleIdList);
      if (roleListFromDB.size() != roleIdList.size()) {
        ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("The Role Id provided in the payload does not exist.").status(BAD_REQUEST).build();
        throw new InvalidPayloadException(error);
      }
    }
    return getEdxActivationCodeRepository().save(edxActivationCodeEntity);
  }

  public void deleteActivationCode(UUID activationCodeId) {
    val entityOptional = getEdxActivationCodeRepository().findById(activationCodeId);
    val entity = entityOptional.orElseThrow(() -> new EntityNotFoundException(EdxActivationCodeEntity.class,EDX_ACTIVATION_CODE_ID, activationCodeId.toString()));
    this.getEdxActivationCodeRepository().delete(entity);
  }

  public EdxActivationCodeEntity findPrimaryEdxActivationCode(String mincode) {
    Optional<EdxActivationCodeEntity> primaryEdxActivationCode = getEdxActivationCodeRepository().findEdxActivationCodeEntitiesByMincodeAndIsPrimaryTrue(mincode);
    if (!primaryEdxActivationCode.isPresent()) {
      throw new EntityNotFoundException(EdxActivationCodeEntity.class, MINCODE, mincode);
    }
    return primaryEdxActivationCode.get();
  }

  public EdxActivationCodeEntity generateOrRegeneratePrimaryEdxActivationCode(String mincode) {
    EdxActivationCodeEntity primaryEdxActivationCode = getEdxActivationCodeRepository().findEdxActivationCodeEntitiesByMincodeAndIsPrimaryTrue(mincode).orElseGet(() -> this.newPrimaryActivationCode(mincode));
    try {
      primaryEdxActivationCode.setActivationCode(this.generateActivationCode());
    } catch (NoSuchAlgorithmException e) {
      ApiError.builder().timestamp(LocalDateTime.now()).message("Unable to generate an activation code.").status(INTERNAL_SERVER_ERROR).build();
    }
    return this.getEdxActivationCodeRepository().save(primaryEdxActivationCode);
  }

  private String generateActivationCode() throws NoSuchAlgorithmException {
    int byteLength = 6; //Base64 encoding an input of 6 bytes will generate a string of 8 characters.
    byte[] randomBytes = new byte[byteLength];
    SecureRandom.getInstanceStrong().nextBytes(randomBytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes).toUpperCase();
  }

  private EdxActivationCodeEntity newPrimaryActivationCode(String mincode) {
    EdxActivationCodeEntity toReturn = new EdxActivationCodeEntity();
    LocalDateTime currentTime = LocalDateTime.now();
    toReturn.setMincode(mincode);
    toReturn.setIsPrimary(true);
    toReturn.setExpiryDate(currentTime);
    toReturn.setCreateUser("EDX-API");
    toReturn.setCreateDate(currentTime);
    toReturn.setUpdateUser("EDX-API");
    toReturn.setUpdateDate(currentTime);
    toReturn.setFirstName("");
    toReturn.setLastName("");
    toReturn.setEmail("");
    toReturn.setValidationCode(UUID.randomUUID());
    toReturn.setIsUrlClicked(false);
    return toReturn;
  }
}
