package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.constants.SagaEnum;
import ca.bc.gov.educ.api.edx.constants.SagaStatusEnum;
import ca.bc.gov.educ.api.edx.endpoint.v1.EdxSagaEndpoint;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.orchestrator.base.Orchestrator;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import ca.bc.gov.educ.api.edx.validator.EdxActivationCodeSagaDataPayLoadValidator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.aspectj.weaver.ast.Or;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Supplier;

import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@Slf4j
public class EdxSagaController implements EdxSagaEndpoint {

  @Getter(AccessLevel.PRIVATE)
  private final EdxActivationCodeSagaDataPayLoadValidator edxActivationCodeSagaDataPayLoadValidator;

  @Getter(PRIVATE)
  private final SagaService sagaService;

  @Getter(PRIVATE)
  private final Map<String, Orchestrator> orchestratorMap = new HashMap<>();

  public EdxSagaController(EdxActivationCodeSagaDataPayLoadValidator edxActivationCodeSagaDataPayLoadValidator, SagaService sagaService, List<Orchestrator> orchestrators) {
    this.edxActivationCodeSagaDataPayLoadValidator = edxActivationCodeSagaDataPayLoadValidator;
    this.sagaService = sagaService;
    orchestrators.forEach(orchestrator -> this.orchestratorMap.put(orchestrator.getSagaName(), orchestrator));
    log.info("'{}' Saga Orchestrators are loaded.", String.join(",", this.orchestratorMap.keySet()));
  }

  @Override
  public ResponseEntity<String> edxSchoolUserActivationInvite(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
    validatePayload(() -> getEdxActivationCodeSagaDataPayLoadValidator().validateEdxActivationCodeSagaDataPayload(edxUserActivationInviteSagaData));
    RequestUtil.setAuditColumnsForCreate(edxUserActivationInviteSagaData);
    return this.processEdxSchoolUserActivationLinkSaga(EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA, edxUserActivationInviteSagaData);
  }

  private ResponseEntity<String> processEdxSchoolUserActivationLinkSaga(final SagaEnum sagaName, final EdxUserActivationInviteSagaData edxUserActivationInviteSagaData) {
    final var sagaInProgress = this.getSagaService().findAllActiveUserActivationInviteSagasByMincodeAndEmailId(edxUserActivationInviteSagaData.getMincode(), edxUserActivationInviteSagaData.getEmail(), sagaName.toString(), this.getActiveStatusesFilter());
    if (sagaInProgress.isPresent()) {
      return ResponseEntity.status(HttpStatus.CONFLICT).build();
    } else {
      return processServicesSaga(sagaName, edxUserActivationInviteSagaData, edxUserActivationInviteSagaData.getCreateUser(), edxUserActivationInviteSagaData.getMincode(), edxUserActivationInviteSagaData.getEmail(),null,null);
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

  private ResponseEntity<String> processServicesSaga(final SagaEnum sagaName, final Object sagaPayload, final String createUser,final String mincode, final String emailId,final UUID edxUserId,final UUID secureExchangeId) {
    try {

      final String payload = JsonUtil.getJsonStringFromObject(sagaPayload);
      final var orchestrator = this.getOrchestratorMap().get(sagaName.toString());
      final var saga = this.getOrchestratorMap()
        .get(sagaName.toString())
        .createSaga(payload, edxUserId,createUser,mincode,emailId,secureExchangeId);
      orchestrator.startSaga(saga);
      return ResponseEntity.status(HttpStatus.ACCEPTED).body(saga.getSagaId().toString());
    } catch (final Exception e) {
      throw new SagaRuntimeException(e.getMessage());
    }
  }
}
