package ca.bc.gov.educ.api.edx.orchestrator.base;

import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeoutException;

/**
 * The interface Orchestrator.
 */
public interface Orchestrator {

  /**
   * Gets saga name.
   *
   * @return the saga name
   */
  String getSagaName();

  /**
   * Start saga.
   *
   * @param saga the saga data
   */
  void startSaga(SagaEntity saga);

  SagaEntity createSaga(String payload, final UUID edxUserId, final String userName,final String mincode, final String emailId,final UUID secureExchangeId);

  SagaEntity createSaga(String payload);

  /**
   * Replay saga.
   *
   * @param saga the saga
   * @throws IOException          the io exception
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   */
  void replaySaga(SagaEntity saga) throws IOException, InterruptedException, TimeoutException;
}
