package ca.bc.gov.educ.api.edx.service.v1;


import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.repository.SagaEventStateRepository;
import ca.bc.gov.educ.api.edx.repository.SagaRepository;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static ca.bc.gov.educ.api.edx.constants.EventType.INITIATED;
import static ca.bc.gov.educ.api.edx.constants.SagaStatusEnum.STARTED;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Saga service.
 */
@Service
@Slf4j
public class SagaService {
  /**
   * The Saga repository.
   */
  @Getter(AccessLevel.PRIVATE)
  private final SagaRepository sagaRepository;
  /**
   * The Saga event repository.
   */
  @Getter(PRIVATE)
  private final SagaEventStateRepository sagaEventStateRepository;

  /**
   * Instantiates a new Saga service.
   *
   * @param sagaRepository           the saga repository
   * @param sagaEventStateRepository the saga event state repository
   */
  @Autowired
  public SagaService(final SagaRepository sagaRepository, final SagaEventStateRepository sagaEventStateRepository) {
    this.sagaRepository = sagaRepository;
    this.sagaEventStateRepository = sagaEventStateRepository;
  }


  /**
   * Create saga record saga.
   *
   * @param saga Entity the saga
   * @return the SagaEntity
   */
  public SagaEntity createSagaRecord(final SagaEntity saga) {
    return this.getSagaRepository().save(saga);
  }

  /**
   * no need to do a get here as it is an attached entity
   * first find the child record, if exist do not add. this scenario may occur in replay process,
   * so dont remove this check. removing this check will lead to duplicate records in the child table.
   *
   * @param saga            the saga object.
   * @param sagaEventStates the saga event
   */
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedSagaWithEvents(final SagaEntity saga, final SagaEventStatesEntity sagaEventStates) {
    saga.setUpdateDate(LocalDateTime.now());
    this.getSagaRepository().save(saga);
    val result = this.getSagaEventStateRepository()
      .findBySagaAndSagaEventOutcomeAndSagaEventStateAndSagaStepNumber(saga, sagaEventStates.getSagaEventOutcome(), sagaEventStates.getSagaEventState(), sagaEventStates.getSagaStepNumber() - 1); //check if the previous step was same and had same outcome, and it is due to replay.
    if (result.isEmpty()) {
      this.getSagaEventStateRepository().save(sagaEventStates);
    }
  }

  /**
   * Find saga by id optional.
   *
   * @param sagaId the saga id
   * @return the optional
   */
  public Optional<SagaEntity> findSagaById(final UUID sagaId) {
    return this.getSagaRepository().findById(sagaId);
  }

  /**
   * Find all saga states list.
   *
   * @param saga the saga
   * @return the list
   */
  public List<SagaEventStatesEntity> findAllSagaStates(final SagaEntity saga) {
    return this.getSagaEventStateRepository().findBySaga(saga);
  }


  /**
   * Update saga record.
   *
   * @param saga the saga
   */
  @Transactional(propagation = Propagation.MANDATORY)
  public void updateSagaRecord(final SagaEntity saga) { // saga here MUST be an attached entity
    this.getSagaRepository().save(saga);
  }


  /**
   * @param mincode
   * @param emailId
   * @param sagaName
   * @param statuses
   * @return
   */
  public Optional<SagaEntity> findAllActiveUserActivationInviteSagasByMincodeAndEmailId(final String mincode, final String emailId, final String sagaName, final List<String> statuses) {
    return this.getSagaRepository().findAllByMincodeAndEmailIdAndSagaNameAndStatusIn(mincode, emailId, sagaName, statuses);
  }

  public Optional<SagaEntity> findAllActiveUserActivationInviteSagasByDistrictIdAndEmailId(final UUID districtId, final String emailId, final String sagaName, final List<String> statuses) {
    return this.getSagaRepository().findAllByDistrictIdAndEmailIdAndSagaNameAndStatusIn(districtId, emailId, sagaName, statuses);
  }

  /**
   * Update attached entity during saga process.
   *
   * @param saga the saga
   */
  @Retryable(value = {Exception.class}, maxAttempts = 5, backoff = @Backoff(multiplier = 2, delay = 2000))
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void updateAttachedEntityDuringSagaProcess(final SagaEntity saga) {
    this.getSagaRepository().save(saga);
  }

  /**
   * Create saga record in db saga.
   *
   * @param sagaName the saga name
   * @param userName the user name
   * @param payload  the payload
   * @return the saga
   */
  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public SagaEntity createSagaRecordInDB(final String sagaName, final String userName, final String payload, final UUID edxUserId, final UUID secureExchangeId, final String mincode, final String emailId,final UUID districtId ) {
    final var saga = SagaEntity
      .builder()
      .payload(payload)
      .sagaName(sagaName)
      .edxUserId(edxUserId)
      .secureExchangeId(secureExchangeId)
      .mincode(mincode)
      .emailId(emailId)
      .status(STARTED.toString())
      .sagaState(INITIATED.toString())
      .districtId(districtId)
      .createDate(LocalDateTime.now())
      .createUser(userName)
      .updateUser(userName)
      .updateDate(LocalDateTime.now())
      .sagaCompensated(false)
      .build();
    return this.createSagaRecord(saga);
  }

}
