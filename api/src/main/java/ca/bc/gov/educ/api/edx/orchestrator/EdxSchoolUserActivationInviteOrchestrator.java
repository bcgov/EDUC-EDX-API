package ca.bc.gov.educ.api.edx.orchestrator;

import ca.bc.gov.educ.api.edx.mappers.v1.EdxActivationCodeMapper;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.service.v1.EdxSchoolUserActivationInviteOrchestratorService;
import ca.bc.gov.educ.api.edx.service.v1.SagaService;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationInviteSagaData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static ca.bc.gov.educ.api.edx.constants.EventOutcome.EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT;
import static ca.bc.gov.educ.api.edx.constants.EventOutcome.PERSONAL_ACTIVATION_CODE_CREATED;
import static ca.bc.gov.educ.api.edx.constants.EventType.CREATE_PERSONAL_ACTIVATION_CODE;
import static ca.bc.gov.educ.api.edx.constants.EventType.SEND_EDX_USER_ACTIVATION_EMAIL;
import static ca.bc.gov.educ.api.edx.constants.SagaEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA;
import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.EDX_SCHOOL_USER_ACTIVATION_INVITE_TOPIC;
import static lombok.AccessLevel.PRIVATE;

@Component
@Slf4j
public class EdxSchoolUserActivationInviteOrchestrator extends SchoolUserActivationBaseOrchestrator<EdxUserSchoolActivationInviteSagaData> {

  protected static final EdxActivationCodeMapper EDX_ACTIVATION_CODE_MAPPER = EdxActivationCodeMapper.mapper;

  @Getter(PRIVATE)
  private final EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService;

  /**
   * Instantiates a new Base orchestrator.
   *
   * @param sagaService                                      the saga service
   * @param messagePublisher                                 the message publisher
   * @param edxSchoolUserActivationInviteOrchestratorService the service
   */
  protected EdxSchoolUserActivationInviteOrchestrator(SagaService sagaService, MessagePublisher messagePublisher, EdxSchoolUserActivationInviteOrchestratorService edxSchoolUserActivationInviteOrchestratorService) {
    super(sagaService, messagePublisher, EdxUserSchoolActivationInviteSagaData.class, EDX_SCHOOL_USER_ACTIVATION_INVITE_SAGA.toString(), EDX_SCHOOL_USER_ACTIVATION_INVITE_TOPIC.toString(),edxSchoolUserActivationInviteOrchestratorService);
    this.edxSchoolUserActivationInviteOrchestratorService = edxSchoolUserActivationInviteOrchestratorService;
  }

  @Override
  public void populateStepsToExecuteMap() {
    this.stepBuilder()
      .begin(CREATE_PERSONAL_ACTIVATION_CODE, this::createPersonalActivationCode)
      .step(CREATE_PERSONAL_ACTIVATION_CODE, PERSONAL_ACTIVATION_CODE_CREATED, SEND_EDX_USER_ACTIVATION_EMAIL, this::sendEdxUserActivationEmail)
      .end(SEND_EDX_USER_ACTIVATION_EMAIL, EDX_SCHOOL_USER_ACTIVATION_EMAIL_SENT);
  }
}
