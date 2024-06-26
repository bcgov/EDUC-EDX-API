package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.edx.endpoint.v1.EdxSagaEndpoint;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.mappers.v1.SagaDataMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.edx.service.v1.EdxFileOnboardingService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import ca.bc.gov.educ.api.edx.validator.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static ca.bc.gov.educ.api.edx.constants.SagaEnum.*;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@Slf4j
public class EdxSagaController implements EdxSagaEndpoint {

  @Getter(AccessLevel.PRIVATE)
  private final EdxUserPayloadValidator edxUserPayLoadValidator;

  @Getter(AccessLevel.PRIVATE)
  private final CreateSchoolSagaPayloadValidator createSchoolSagaPayloadValidator;

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeSagaDataPayloadValidator edxActivationCodeSagaDataPayLoadValidator;

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangePayloadValidator secureExchangePayloadValidator;

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeCommentSagaValidator secureExchangeCommentSagaValidator;

  @Getter(AccessLevel.PRIVATE)
  private final EdxFileOnboardingService edxFileOnboardingService;

  private final CreateSecureExchangeSagaPayloadValidator createSecureExchangeSagaPayloadValidator;

  @Getter(PRIVATE)
  private final SagaService sagaService;

  @Getter(PRIVATE)
  private final Map<String, Orchestrator> orchestratorMap = new HashMap<>();

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;

  public EdxSagaController(EdxActivationCodeSagaDataPayloadValidator edxActivationCodeSagaDataPayLoadValidator, SagaService sagaService, List<Orchestrator> orchestrators, CreateSchoolSagaPayloadValidator createSchoolSagaPayloadValidator, SecureExchangePayloadValidator secureExchangePayloadValidator, SecureExchangeCommentSagaValidator secureExchangeCommentSagaValidator, CreateSecureExchangeSagaPayloadValidator createSecureExchangeSagaPayloadValidator, EdxUserPayloadValidator edxUserPayLoadValidator, EdxFileOnboardingService edxFileOnboardingService) {
    this.edxActivationCodeSagaDataPayLoadValidator = edxActivationCodeSagaDataPayLoadValidator;
    this.sagaService = sagaService;
      this.createSchoolSagaPayloadValidator = createSchoolSagaPayloadValidator;
      this.secureExchangePayloadValidator = secureExchangePayloadValidator;
    this.secureExchangeCommentSagaValidator = secureExchangeCommentSagaValidator;
    this.createSecureExchangeSagaPayloadValidator = createSecureExchangeSagaPayloadValidator;
    this.edxUserPayLoadValidator = edxUserPayLoadValidator;
    this.edxFileOnboardingService = edxFileOnboardingService;
    orchestrators.forEach(orchestrator -> this.orchestratorMap.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", this.orchestratorMap.keySet()));
  }

  @Override
  public ResponseEntity<String> edxSchoolUserActivationInvite(EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData) {
    validatePayload(() -> getEdxActivationCodeSagaDataPayLoadValidator().validateEdxSchoolUserActivationCodeSagaDataPayload(edxUserActivationInviteSagaData));
    RequestUtil.setAuditColumnsForCreate(edxUserActivationInviteSagaData);
    return this.processEdxSchoolUserActivationLinkSaga(EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA, edxUserActivationInviteSagaData);
  }

  @Override
  public ResponseEntity<String> edxSchoolUserActivationRelink(EdxUserSchoolActivationRelinkSagaData edxUserActivationRelinkSagaData) {
    validatePayload(() -> getEdxActivationCodeSagaDataPayLoadValidator().validateEdxActivationCodeRelinkSchoolSagaDataPayload(edxUserActivationRelinkSagaData));
    RequestUtil.setAuditColumnsForCreate(edxUserActivationRelinkSagaData);
    return this.processEdxSchoolUserActivationLinkSaga(EDX_SCHOOL_USER_ACTIVATION_RELINK_SAGA, edxUserActivationRelinkSagaData);
  }

  @Override
  public ResponseEntity<String> createNewSecureExchange(SecureExchangeCreateSagaData secureExchangeCreateSagaData) {
    validatePayload(() -> createSecureExchangeSagaPayloadValidator.validatePayload(secureExchangeCreateSagaData,true));
    RequestUtil.setAuditColumnsForCreate(secureExchangeCreateSagaData);
    return this.processNewSecureExchangeMessageSaga(NEW_SECURE_EXCHANGE_SAGA, secureExchangeCreateSagaData);

  }

  @Override
  public ResponseEntity<String> createSecureExchangeComment(SecureExchangeCommentSagaData secureExchangeCommentSagaData) {
    validatePayload(() -> getSecureExchangeCommentSagaValidator().validateSecureExchangeCommentSagaPayload(secureExchangeCommentSagaData));
    RequestUtil.setAuditColumnsForCreate(secureExchangeCommentSagaData);
    return this.processSecureExchangeCommentSaga(SECURE_EXCHANGE_COMMENT_SAGA,secureExchangeCommentSagaData);
  }

  @Override
  public ResponseEntity<String> edxDistrictUserActivationInvite(EdxUserDistrictActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    validatePayload(() -> getEdxActivationCodeSagaDataPayLoadValidator().validateDistrictUserEdxActivationCodeSagaDataPayload(edxDistrictUserActivationInviteSagaData));
    RequestUtil.setAuditColumnsForCreate(edxDistrictUserActivationInviteSagaData);
    return this.processEdxDistrictUserActivationLinkSaga(EDX_DISTRICT_USER_ACTIVATION_INVITE_SAGA, edxDistrictUserActivationInviteSagaData);
  }

  @Override
  public ResponseEntity<String> edxDistrictUserActivationRelink(EdxUserDistrictActivationRelinkSagaData edxUserActivationRelinkSagaData) {
    validatePayload(() -> getEdxActivationCodeSagaDataPayLoadValidator().validateEdxActivationCodeRelinkSchoolSagaDataPayload(edxUserActivationRelinkSagaData));
    RequestUtil.setAuditColumnsForCreate(edxUserActivationRelinkSagaData);
    return this.processEdxDistrictUserActivationLinkSaga(EDX_DISTRICT_USER_ACTIVATION_RELINK_SAGA, edxUserActivationRelinkSagaData);
  }

  @Override
  public ResponseEntity<String> createSchool(CreateSchoolSagaData sagaData) {
    EdxUser edxUser = sagaData.getInitialEdxUser();
    if (edxUser != null) {
      validatePayload(() -> getEdxUserPayLoadValidator().validateEdxUserPayload(edxUser, true));
    }
    validatePayload(() -> getCreateSchoolSagaPayloadValidator().validateCreateSchoolSagaPayload(sagaData));
    return this.processNewSchoolSaga(CREATE_NEW_SCHOOL_SAGA, sagaData);
  }

  @Override
  public ResponseEntity<String> moveSchool(MoveSchoolData moveSchoolData) {
    return this.processMoveSchoolSaga(MOVE_SCHOOL_SAGA, moveSchoolData);
  }

  @Override
  public OnboardingFileProcessResponse processOnboardingFile(OnboardingFileUpload fileUpload) {
    List<SagaEntity> sagaEntities = this.edxFileOnboardingService.processOnboardingFile(Base64.getDecoder().decode(fileUpload.getFileContents()), fileUpload.getCreateUser());
    log.info("Number of onboarded sagas stored is: " + sagaEntities.size());
    List<SagaEntity> first100Sagas = sagaEntities.stream().limit(100).toList();
    first100Sagas.forEach(sagaEntity -> startServicesSaga(sagaEntity.getSagaName().equals(ONBOARD_SCHOOL_USER_SAGA.toString()) ? ONBOARD_SCHOOL_USER_SAGA : ONBOARD_DISTRICT_USER_SAGA, sagaEntity));
    OnboardingFileProcessResponse response = new OnboardingFileProcessResponse();
    response.setProcessedCount(Integer.toString(sagaEntities.size()));
    return response;
  }

  private ResponseEntity<String> processNewSchoolSaga(SagaEnum sagaName, CreateSchoolSagaData newSchoolSagaData) {
    try {
      RequestUtil.setAuditColumnsForCreate(newSchoolSagaData);
      SagaEntity sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(sagaName), newSchoolSagaData);
      return processServicesSaga(sagaName, sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }

  private ResponseEntity<String> processMoveSchoolSaga(SagaEnum sagaName, MoveSchoolData moveSchoolData) {
    try {
      RequestUtil.setAuditColumnsForCreate(moveSchoolData);
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(sagaName),moveSchoolData);
      return processServicesSaga(sagaName,sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }

  private ResponseEntity<String> processSecureExchangeCommentSaga(SagaEnum sagaName, SecureExchangeCommentSagaData secureExchangeCommentSagaData) {
    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(sagaName),secureExchangeCommentSagaData);
      return processServicesSaga(sagaName,sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }

  private ResponseEntity<String> processEdxSchoolUserActivationLinkSaga(final SagaEnum sagaName, final EdxUserSchoolActivationInviteSagaData edxUserActivationInviteSagaData) {
    final var sagaInProgress = this.getSagaService().findAllActiveUserActivationInviteSagasBySchoolIDAndEmailId(edxUserActivationInviteSagaData.getSchoolID(), edxUserActivationInviteSagaData.getEmail(), sagaName.toString(), this.getActiveStatusesFilter());
    if (sagaInProgress.isPresent()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } else {
      try {
        val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(sagaName),edxUserActivationInviteSagaData);
        return processServicesSaga(sagaName,sagaEntity);
      } catch (JsonProcessingException e) {
        throw new SagaRuntimeException(e);
      }
    }
  }

  private ResponseEntity<String> processEdxDistrictUserActivationLinkSaga(final SagaEnum sagaName, final EdxUserDistrictActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    final var sagaInProgress = this.getSagaService().findAllActiveUserActivationInviteSagasByDistrictIDAndEmailId(edxDistrictUserActivationInviteSagaData.getDistrictID(), edxDistrictUserActivationInviteSagaData.getEmail(), sagaName.toString(), this.getActiveStatusesFilter());
    if (sagaInProgress.isPresent()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } else {
      try {
        val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(sagaName),edxDistrictUserActivationInviteSagaData);
        return processServicesSaga(sagaName,sagaEntity);
      } catch (JsonProcessingException e) {
        throw new SagaRuntimeException(e);
      }

    }
  }

  private ResponseEntity<String> processNewSecureExchangeMessageSaga(final SagaEnum sagaName, final SecureExchangeCreateSagaData secureExchangeCreateSagaData) {
    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(sagaName),secureExchangeCreateSagaData);
      return processServicesSaga(sagaName,sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }

  private void validatePayload(Supplier<List<FieldError>> validator) {
    val validationResult = validator.get();
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

  protected List<String> getActiveStatusesFilter() {
    final var statuses = new ArrayList<String>();
    statuses.add(SagaStatusEnum.IN_PROGRESS.toString());
    statuses.add(SagaStatusEnum.STARTED.toString());
    return statuses;
  }

  private ResponseEntity<String> processServicesSaga(final SagaEnum sagaName, SagaEntity sagaEntity) {
    try {
      final var orchestrator = this.getOrchestratorMap().get(sagaName.toString());
      final var saga = this.getOrchestratorMap()
        .get(sagaName.toString())
        .createSaga(sagaEntity);
      orchestrator.startSaga(saga);
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(saga.getSagaId().toString());
    } catch (final Exception e) {
      throw new SagaRuntimeException(e.getMessage());
    }
  }

  private void startServicesSaga(final SagaEnum sagaName, SagaEntity sagaEntity) {
    try {
      this.getOrchestratorMap()
              .get(sagaName.toString())
              .startSaga(sagaEntity);
    } catch (final Exception e) {
      throw new SagaRuntimeException(e.getMessage());
    }
  }
}
