package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.service.v1.EdxDistrictUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserDistrictActivationInviteSagaData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.EDX_DISTRICT_USER_ACTIVATION_EMAIL_SENT;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.PERSONAL_ACTIVATION_CODE_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_PERSONAL_ACTIVATION_CODE;
import static ca.bc.gov.educ.api.edx.constants.EventType.SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_DISTRICT_USER_ACTIVATION_INVITE_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_DISTRICT_USER_ACTIVATION_INVITE_TOPIC;
import static lombok.AccessLevel.PRIVATE;

/**
 * The type Edx district user activation invite orchestrator.
 */
@Component
@Slf4j
public class EdxDistrictUserActivationInviteOrchestrator extends DistrictUserActivationBaseOrchestrator<EdxUserDistrictActivationInviteSagaData> {


  /**
   * The Edx district user activation invite orchestrator service.
   */
  @Getter(PRIVATE)
  private final EdxDistrictUserActivationInviteOrchestratorService edxDistrictUserActivationInviteOrchestratorService;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                                        the saga service
   * @param messagePublisher                                   the message publisher
   * @param edxDistrictUserActivationInviteOrchestratorService the edx district user activation invite orchestrator service
   */
  protected EdxDistrictUserActivationInviteOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, EdxDistrictUserActivationInviteOrchestratorService edxDistrictUserActivationInviteOrchestratorService) {
    super(sagaService, messagePublisher, EdxUserDistrictActivationInviteSagaData.class, EDX_DISTRICT_USER_ACTIVATION_INVITE_SAGA.toString(), EDX_DISTRICT_USER_ACTIVATION_INVITE_TOPIC.toString(), edxDistrictUserActivationInviteOrchestratorService);
    this.edxDistrictUserActivationInviteOrchestratorService = edxDistrictUserActivationInviteOrchestratorService;
  }

  /**
   * Populate steps to execute map.
   */
  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_PERSONAL_ACTIVATION_CODE, this::createPersonalActivationCode)
      .step(CREATE_PERSONAL_ACTIVATION_CODE, PERSONAL_ACTIVATION_CODE_CREATED, SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL, this::sendEdxUserActivationEmail)
      .end(SEND_EDX_DISTRICT_USER_ACTIVATION_EMAIL, EDX_DISTRICT_USER_ACTIVATION_EMAIL_SENT);
  }

}
