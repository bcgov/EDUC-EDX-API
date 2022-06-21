package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxActivationCodeMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxUserActivationInviteSagaDataMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.repository.EdxActivationCodeRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EmailNotification;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class EdxSchoolUserActivationInviteOrchestratorService {

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeRepository edxActivationCodeRepository;
  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService edxUsersService;

  @Getter(AccessLevel.PRIVATE)
  private final EmailProperties emailProperties;

  @Getter(AccessLevel.PRIVATE)
  private final ApplicationProperties props;

  @Getter(AccessLevel.PRIVATE)
  private final EmailNotificationService emailNotificationService;

  protected static final EdxUserActivationInviteSagaDataMapper EDX_USER_ACTIVATION_INVITE_SAGA_DATA_MAPPER = EdxUserActivationInviteSagaDataMapper.mapper;

  protected static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;

  public EdxSchoolUserActivationInviteOrchestratorService(EdxActivationCodeRepository edxActivationCodeRepository, EdxUsersService edxUsersService, EmailProperties emailProperties, ApplicationProperties props, EmailNotificationService emailNotificationService) {
    this.edxActivationCodeRepository = edxActivationCodeRepository;
    this.edxUsersService = edxUsersService;
    this.emailProperties = emailProperties;
    this.props = props;
    this.emailNotificationService = emailNotificationService;
  }

  public boolean checkIfSagaDataExists(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
    boolean flag = false;
    Optional<EdxActivationCodeEntity> existingPersonalCode = getEdxActivationCodeRepository().findEdxActivationCodeEntityByEmailAndMincodeAndIsPrimaryIsFalse(edxUserActivationInviteSagaData.getEmail(), edxUserActivationInviteSagaData.getMincode());

    if (existingPersonalCode.isPresent()) {

      EdxActivationCodeEntity personalCode = existingPersonalCode.get();
      if (Boolean.FALSE.equals(personalCode.getIsUrlClicked()) && personalCode.getExpiryDate().isAfter(LocalDateTime.now())) {
        //data for this step of saga already exists in DB use the data and go to next step
        edxUserActivationInviteSagaData.setPersonalActivationCode(personalCode.getActivationCode());
        edxUserActivationInviteSagaData.setValidationCode(personalCode.getValidationCode().toString());
        flag = true;
      }
    }
    return flag;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createPersonalActivationCode(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
    EdxActivationCode edxActivationCode = EDX_USER_ACTIVATION_INVITE_SAGA_DATA_MAPPER.toEdxActivationCode(edxUserActivationInviteSagaData);
    RequestUtil.setAuditColumnsForCreate(edxActivationCode);
    if (!CollectionUtils.isEmpty(edxActivationCode.getEdxActivationRoles())) {
      edxActivationCode.getEdxActivationRoles().forEach(RequestUtil::setAuditColumnsForCreate);
    }
    try {
      EdxActivationCodeEntity personalActivationCodeEntity = getEdxUsersService().createPersonalEdxActivationCode(EDX_ACTIVATION_CODE_MAPPER.toModel(edxActivationCode));
      updateSagaData(edxUserActivationInviteSagaData, personalActivationCodeEntity);

    } catch (NoSuchAlgorithmException e) {
      throw new SagaRuntimeException(e);
    }

  }

  private void updateSagaData(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData, EdxActivationCodeEntity personalActivationCodeEntity) {
    edxUserActivationInviteSagaData.setEdxActivationCodeId(personalActivationCodeEntity.getEdxActivationCodeId().toString());
    edxUserActivationInviteSagaData.setValidationCode(personalActivationCodeEntity.getValidationCode().toString());
    edxUserActivationInviteSagaData.setExpiryDate(personalActivationCodeEntity.getExpiryDate());
    edxUserActivationInviteSagaData.setPersonalActivationCode(personalActivationCodeEntity.getActivationCode());
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendEmail(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
    final var subject = emailProperties.getEdxSchoolUserActivationInviteEmailSubject();
    final var from = emailProperties.getEdxSchoolUserActivationInviteEmailFrom();
    final var emailNotification = EmailNotification.builder()
      .fromEmail(from)
      .toEmail(edxUserActivationInviteSagaData.getEmail())
      .subject(subject)
      .templateName("edx.school.user.activation.invite")
      .emailFields(Map.of("schoolUserFirstName", edxUserActivationInviteSagaData.getFirstName(), "schoolName", edxUserActivationInviteSagaData.getSchoolName(), "activationLink", createUserActivationLink(edxUserActivationInviteSagaData), "personalActivationCode", edxUserActivationInviteSagaData.getPersonalActivationCode()))
      .build();

    this.getEmailNotificationService().sendEmail(emailNotification);

  }

  private String createUserActivationLink(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
    return props.getEdxApplicationBaseUrl() +
      props.getEdxSchoolUserActivationInviteAppendUrl() +
      edxUserActivationInviteSagaData.getValidationCode();
  }
}
