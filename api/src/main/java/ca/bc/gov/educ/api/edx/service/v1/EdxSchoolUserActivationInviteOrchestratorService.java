package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxActivationCodeMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserActivationInviteSagaDataMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.repository.EdxActivationCodeRepository;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EdxSchoolUserActivationInviteOrchestratorService {

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeRepository edxActivationCodeRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserRepository edxUserRepository;

  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService edxUsersService;

  private final EmailProperties emailProperties;

  private final ApplicationProperties props;

  @Getter(AccessLevel.PRIVATE)
  private final EmailNotificationService emailNotificationService;

  protected static final EdxUserActivationInviteSagaDataMapper EDX_USER_ACTIVATION_INVITE_SAGA_DATA_MAPPER = EdxUserActivationInviteSagaDataMapper.mapper;

  protected static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;
  protected final SagaService sagaService;

  public EdxSchoolUserActivationInviteOrchestratorService(EdxActivationCodeRepository edxActivationCodeRepository, EdxUserRepository edxUserRepository, EdxUsersService edxUsersService, EmailProperties emailProperties, ApplicationProperties props, EmailNotificationService emailNotificationService, SagaService sagaService) {
    this.edxActivationCodeRepository = edxActivationCodeRepository;
    this.edxUserRepository = edxUserRepository;
    this.edxUsersService = edxUsersService;
    this.emailProperties = emailProperties;
    this.props = props;
    this.emailNotificationService = emailNotificationService;
    this.sagaService = sagaService;
  }

  private EdxActivationCodeEntity createActivationCode(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData){
    EdxActivationCode edxActivationCode = EDX_USER_ACTIVATION_INVITE_SAGA_DATA_MAPPER.toEdxActivationCode(edxUserActivationInviteSagaData);
    RequestUtil.setAuditColumnsForCreate(edxActivationCode);
    if (!CollectionUtils.isEmpty(edxActivationCode.getEdxActivationRoles())) {
      edxActivationCode.getEdxActivationRoles().forEach(RequestUtil::setAuditColumnsForCreate);
    }
    try {
      return getEdxUsersService().createPersonalEdxActivationCode(EDX_ACTIVATION_CODE_MAPPER.toModel(edxActivationCode));
    } catch (NoSuchAlgorithmException e) {
      throw new SagaRuntimeException(e);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createPersonalActivationCodeAndUpdateSagaData(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData, SagaEntity sagaEntity) throws JsonProcessingException {
    EdxActivationCodeEntity personalActivationCodeEntity = createActivationCode(edxUserActivationInviteSagaData);
    updateSagaDataInternal(edxUserActivationInviteSagaData, personalActivationCodeEntity, sagaEntity);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW) // this makes sure it is done in a new transaction when used through proxy, so call on line#84 won't have a new transaction.
  public void updateSagaData(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData, EdxActivationCodeEntity personalActivationCodeEntity, SagaEntity sagaEntity) throws JsonProcessingException {
    updateSagaDataInternal(edxUserActivationInviteSagaData, personalActivationCodeEntity, sagaEntity);
  }

  private void updateSagaDataInternal(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData, EdxActivationCodeEntity personalActivationCodeEntity, SagaEntity sagaEntity) throws JsonProcessingException {
    edxUserActivationInviteSagaData.setEdxActivationCodeId(personalActivationCodeEntity.getEdxActivationCodeId().toString());
    edxUserActivationInviteSagaData.setValidationCode(personalActivationCodeEntity.getValidationCode().toString());
    edxUserActivationInviteSagaData.setExpiryDate(personalActivationCodeEntity.getExpiryDate());
    edxUserActivationInviteSagaData.setPersonalActivationCode(personalActivationCodeEntity.getActivationCode());
    sagaEntity.setPayload(JsonUtil.getJsonStringFromObject(edxUserActivationInviteSagaData)); // update the payload which will be updated in DB.
    this.sagaService.updateSagaRecord(sagaEntity);
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendEmail(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData) {
    final var subject = emailProperties.getEdxSchoolUserActivationInviteEmailSubject();
    final var from = emailProperties.getEdxSchoolUserActivationInviteEmailFrom();
    final var edxAdmins = edxUserRepository.findEdxUserNamesBySchoolIDAndPermissionCode(edxUserActivationInviteSagaData.getSchoolID(), "EDX_USER_SCHOOL_ADMIN").stream().sorted().collect(Collectors.joining(", "));
    final String recipient = (edxUserActivationInviteSagaData.getFirstName()
      + " " + edxUserActivationInviteSagaData.getLastName()).trim();

    final var emailNotification = EmailNotification.builder()
      .fromEmail(from)
      .toEmail(edxUserActivationInviteSagaData.getEmail())
      .subject(subject)
      .templateName("edx.school.user.activation.invite")
      .emailFields(Map.of(
        "recipient", recipient,
        "activationLink", createUserActivationLink(edxUserActivationInviteSagaData),
        "personalActivationCode", edxUserActivationInviteSagaData.getPersonalActivationCode(),
        "edxAdmins", edxAdmins
      ))
      .build();

    this.getEmailNotificationService().sendEmail(emailNotification);
  }

  private String createUserActivationLink(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData) {
    return props.getEdxApplicationBaseUrl() +
      props.getEdxSchoolUserActivationInviteAppendUrl() +
      edxUserActivationInviteSagaData.getValidationCode();
  }

  public EdxActivationCodeEntity getActivationCodeById(UUID edxActivationCodeId) {
    return getEdxActivationCodeRepository().findById(edxActivationCodeId).orElseThrow();
  }
}
