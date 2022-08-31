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
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import ca.bc.gov.educ.api.edx.validator.EdxActivationCodeSagaDataPayLoadValidator;
import ca.bc.gov.educ.api.edx.validator.SecureExchangePayloadValidator;
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

/**
 * The type Edx saga controller.
 */
@RestController
@Slf4j
public class EdxSagaController implements EdxSagaEndpoint {

  /**
   * The Edx activation code saga data pay load validator.
   */
  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeSagaDataPayLoadValidator edxActivationCodeSagaDataPayLoadValidator;

  /**
   * The Secure exchange payload validator.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangePayloadValidator secureExchangePayloadValidator;

  /**
   * The Saga service.
   */
  @Getter(PRIVATE)
  private final SagaService sagaService;

  /**
   * The Orchestrator map.
   */
  @Getter(PRIVATE)
  private final Map<String, Orchestrator> orchestratorMap = new HashMap<>();

  private static final SagaDataMapper SAGA_DATA_MAPPER = SagaDataMapper.mapper;

  /**
   * Instantiates a new Edx saga controller.
   *
   * @param edxActivationCodeSagaDataPayLoadValidator the edx activation code saga data pay load validator
   * @param sagaService                               the saga service
   * @param orchestrators                             the orchestrators
   * @param secureExchangePayloadValidator            the secure exchange payload validator
   */
  public EdxSagaController(EdxActivationCodeSagaDataPayLoadValidator edxActivationCodeSagaDataPayLoadValidator, SagaService sagaService, List<Orchestrator> orchestrators, SecureExchangePayloadValidator secureExchangePayloadValidator) {
    this.edxActivationCodeSagaDataPayLoadValidator = edxActivationCodeSagaDataPayLoadValidator;
    this.sagaService = sagaService;
    this.secureExchangePayloadValidator = secureExchangePayloadValidator;
    orchestrators.forEach(orchestrator -> this.orchestratorMap.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", this.orchestratorMap.keySet()));
  }

  /**
   * Edx school user activation invite response entity.
   *
   * @param edxUserActivationInviteSagaData the edx user activation invite saga data
   * @return the response entity
   */
  @Override
  public ResponseEntity<String> edxSchoolUserActivationInvite(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
    validatePayload(() -> getEdxActivationCodeSagaDataPayLoadValidator().validateEdxActivationCodeSagaDataPayload(edxUserActivationInviteSagaData));
    RequestUtil.setAuditColumnsForCreate(edxUserActivationInviteSagaData);
    return this.processEdxSchoolUserActivationLinkSaga(EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA, edxUserActivationInviteSagaData);
  }
  @Override
  public ResponseEntity<String> edxSchoolUserActivationRelink(EdxUserActivationRelinkSagaData edxUserActivationRelinkSagaData) {
    validatePayload(() -> getEdxActivationCodeSagaDataPayLoadValidator().validateEdxActivationCodeRelinkSagaDataPayload(edxUserActivationRelinkSagaData));
    RequestUtil.setAuditColumnsForCreate(edxUserActivationRelinkSagaData);
    return this.processEdxSchoolUserActivationLinkSaga(EDX_SCHOOL_USER_ACTIVATION_RELINK_SAGA, edxUserActivationRelinkSagaData);
  }

  /**
   * Create new secure exchange response entity.
   *
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   * @return the response entity
   */
  @Override
  public ResponseEntity<String> createNewSecureExchange(SecureExchangeCreateSagaData secureExchangeCreateSagaData) {
    validatePayload(() -> getSecureExchangePayloadValidator().validatePayload(secureExchangeCreateSagaData.getSecureExchangeCreate(),true));
    RequestUtil.setAuditColumnsForCreate(secureExchangeCreateSagaData);
    return this.processNewSecureExchangeMessageSaga(NEW_SECURE_EXCHANGE_SAGA, secureExchangeCreateSagaData);

  }

  /**
   * Create secure exchange comment response entity.
   *
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @return the response entity
   */
  @Override
  public ResponseEntity<String> createSecureExchangeComment(SecureExchangeCommentSagaData secureExchangeCommentSagaData) {
    RequestUtil.setAuditColumnsForCreate(secureExchangeCommentSagaData);
    return this.processSecureExchangeCommentSaga(SECURE_EXCHANGE_COMMENT_SAGA,secureExchangeCommentSagaData);
  }

  @Override
  public ResponseEntity<String> edxDistrictUserActivationInvite(EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    validatePayload(() -> getEdxActivationCodeSagaDataPayLoadValidator().validateDistrictUserEdxActivationCodeSagaDataPayload(edxDistrictUserActivationInviteSagaData));
    RequestUtil.setAuditColumnsForCreate(edxDistrictUserActivationInviteSagaData);
    return this.processEdxDistrictUserActivationLinkSaga(EDX_DISTRICT_USER_ACTIVATION_INVITE_SAGA, edxDistrictUserActivationInviteSagaData);
  }

  /**
   * Process secure exchange comment saga response entity.
   *
   * @param sagaName                      the saga name
   * @param secureExchangeCommentSagaData the secure exchange comment saga data
   * @return the response entity
   */
  private ResponseEntity<String> processSecureExchangeCommentSaga(SagaEnum sagaName, SecureExchangeCommentSagaData secureExchangeCommentSagaData) {
    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(sagaName),secureExchangeCommentSagaData);
      return processServicesSaga(sagaName,sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }
  }

  /**
   * Process edx school user activation link saga response entity.
   *
   * @param sagaName                        the saga name
   * @param edxUserActivationInviteSagaData the edx user activation invite saga data
   * @return the response entity
   */
  private ResponseEntity<String> processEdxSchoolUserActivationLinkSaga(final SagaEnum sagaName, final EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
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


  private ResponseEntity<String> processEdxDistrictUserActivationLinkSaga(final SagaEnum sagaName, final EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData) {
    final var sagaInProgress = this.getSagaService().findAllActiveUserActivationInviteSagasByDistrictIDAndEmailId(UUID.fromString(edxDistrictUserActivationInviteSagaData.getDistrictID()), edxDistrictUserActivationInviteSagaData.getEmail(), sagaName.toString(), this.getActiveStatusesFilter());
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

  /**
   * Process new secure exchange message saga response entity.
   *
   * @param sagaName                     the saga name
   * @param secureExchangeCreateSagaData the secure exchange create saga data
   * @return the response entity
   */
  private ResponseEntity<String> processNewSecureExchangeMessageSaga(final SagaEnum sagaName, final SecureExchangeCreateSagaData secureExchangeCreateSagaData) {

    try {
      val sagaEntity = SAGA_DATA_MAPPER.toModel(String.valueOf(sagaName),secureExchangeCreateSagaData);
      return processServicesSaga(sagaName,sagaEntity);
    } catch (JsonProcessingException e) {
      throw new SagaRuntimeException(e);
    }

  }

  /**
   * Validate payload.
   *
   * @param validator the validator
   */
  private void validatePayload(Supplier<List<FieldError>> validator) {
    val validationResult = validator.get();
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

  /**
   * Gets active statuses filter.
   *
   * @return the active statuses filter
   */
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
}
