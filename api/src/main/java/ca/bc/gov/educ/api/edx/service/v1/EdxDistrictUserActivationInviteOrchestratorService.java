package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxActivationCodeMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.EdxDistrictUserActivationInviteSagaDataMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.props.EmailProperties;
import ca.bc.gov.educ.api.edx.repository.EdxActivationCodeRepository;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxDistrictUserActivationInviteSagaData;
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

@Service
@Slf4j
public class EdxDistrictUserActivationInviteOrchestratorService {

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeRepository edxActivationCodeRepository;
  @Getter(AccessLevel.PRIVATE)
  private final EdxUsersService edxUsersService;

  private final EmailProperties emailProperties;

  private final ApplicationProperties props;

  @Getter(AccessLevel.PRIVATE)
  private final EmailNotificationService emailNotificationService;

  protected static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;
  protected final SagaService sagaService;

  protected static final EdxDistrictUserActivationInviteSagaDataMapper EDX_DISTRICT_USER_ACTIVATION_INVITE_SAGA_DATA_MAPPER = EdxDistrictUserActivationInviteSagaDataMapper.mapper;

  public EdxDistrictUserActivationInviteOrchestratorService(EdxActivationCodeRepository edxActivationCodeRepository, EdxUsersService edxUsersService, EmailProperties emailProperties, ApplicationProperties props, EmailNotificationService emailNotificationService, SagaService sagaService) {
    this.edxActivationCodeRepository = edxActivationCodeRepository;
    this.edxUsersService = edxUsersService;
    this.emailProperties = emailProperties;
    this.props = props;
    this.emailNotificationService = emailNotificationService;
    this.sagaService = sagaService;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void createPersonalActivationCodeAndUpdateSagaData(EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData, SagaEntity sagaEntity) {
    EdxActivationCode edxActivationCode = EDX_DISTRICT_USER_ACTIVATION_INVITE_SAGA_DATA_MAPPER.toEdxActivationCode(edxDistrictUserActivationInviteSagaData);
    RequestUtil.setAuditColumnsForCreate(edxActivationCode);
    if (!CollectionUtils.isEmpty(edxActivationCode.getEdxActivationRoles())) {
      edxActivationCode.getEdxActivationRoles().forEach(RequestUtil::setAuditColumnsForCreate);
    }
    try {
      EdxActivationCodeEntity personalActivationCodeEntity = getEdxUsersService().createPersonalEdxActivationCode(EDX_ACTIVATION_CODE_MAPPER.toModel(edxActivationCode));
      updateSagaDataInternal(edxDistrictUserActivationInviteSagaData, personalActivationCodeEntity, sagaEntity);
    } catch (NoSuchAlgorithmException | JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW) // this makes sure it is done in a new transaction when used through proxy, so call on line#84 won't have a new transaction.
  public void updateSagaData(EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData, EdxActivationCodeEntity personalActivationCodeEntity, SagaEntity sagaEntity) throws JsonProcessingException {
    updateSagaDataInternal(edxDistrictUserActivationInviteSagaData, personalActivationCodeEntity, sagaEntity);
  }

  private void updateSagaDataInternal(EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData, EdxActivationCodeEntity personalActivationCodeEntity, SagaEntity sagaEntity) throws JsonProcessingException {
    edxDistrictUserActivationInviteSagaData.setEdxActivationCodeId(personalActivationCodeEntity.getEdxActivationCodeId().toString());
    edxDistrictUserActivationInviteSagaData.setValidationCode(personalActivationCodeEntity.getValidationCode().toString());
    edxDistrictUserActivationInviteSagaData.setExpiryDate(personalActivationCodeEntity.getExpiryDate());
    edxDistrictUserActivationInviteSagaData.setPersonalActivationCode(personalActivationCodeEntity.getActivationCode());
    sagaEntity.setPayload(JsonUtil.getJsonStringFromObject(edxDistrictUserActivationInviteSagaData)); // update the payload which will be updated in DB.
    this.sagaService.updateSagaRecord(sagaEntity); // save updated payload to DB again.
  }

  private String createUserActivationLink(EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    return props.getEdxApplicationBaseUrl() +
      props.getEdxSchoolUserActivationInviteAppendUrl() +
      edxDistrictUserActivationInviteSagaData.getValidationCode();
  }

  public EdxActivationCodeEntity getActivationCodeById(UUID edxActivationCodeId) {
    return getEdxActivationCodeRepository().findById(edxActivationCodeId).orElseThrow();
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void sendEmail(EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    final var subject = emailProperties.getEdxSchoolUserActivationInviteEmailSubject();
    final var from = emailProperties.getEdxSchoolUserActivationInviteEmailFrom();
    final var emailNotification = EmailNotification.builder()
      .fromEmail(from)
      .toEmail(edxDistrictUserActivationInviteSagaData.getEmail())
      .subject(subject)
      .templateName("edx.district.user.activation.invite")
      .emailFields(Map.of("firstName", edxDistrictUserActivationInviteSagaData.getFirstName(), "districtName", edxDistrictUserActivationInviteSagaData.getDistrictName(), "activationLink", createUserActivationLink(edxDistrictUserActivationInviteSagaData), "personalActivationCode", edxDistrictUserActivationInviteSagaData.getPersonalActivationCode()))
      .build();

    this.getEmailNotificationService().sendEmail(emailNotification);

  }

}
