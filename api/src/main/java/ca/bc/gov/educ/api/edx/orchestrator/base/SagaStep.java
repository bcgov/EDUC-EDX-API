package ca.bc.gov.educ.api.edx.orchestrator.base;


import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.struct.v1.Event;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * The interface Saga step.
 *
 * @param <T> the type parameter
 */
@FunctionalInterface
public interface SagaStep<T> {
  /**
   * Apply.
   *
   * @param event    the event
   * @param saga     the saga
   * @param sagaData the saga data
   * @throws InterruptedException the interrupted exception
   * @throws TimeoutException     the timeout exception
   * @throws IOException          the io exception
   */
  void apply(Event event, SagaEntity saga, T sagaData) throws InterruptedException, TimeoutException, IOException;
}
