package ca.bc.gov.educ.api.edx.service.v1.event;

import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.orchestrator.base.EventHandler;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.gradschool.v1.GradSchool;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.GRAD_SCHOOL_EVENTS_TOPIC;

@Service
@Slf4j
public class SchoolUpdateEventDelegatorService implements EventHandler {

    private final EdxUsersService edxUsersService;
    public static final String PAYLOAD_LOG = "payload is :: {}";

    public SchoolUpdateEventDelegatorService(EdxUsersService edxUsersService) {
        this.edxUsersService = edxUsersService;
    }

    @Async("subscriberExecutor")
    @Override
    public void handleEvent(Event event) {
      try {
        if(event.getEventType().equalsIgnoreCase(EventType.UPDATE_GRAD_SCHOOL.toString())) {
           log.info("Received UPDATE_GRAD_SCHOOL event :: {}", event.getSagaId());
           log.trace(PAYLOAD_LOG, event.getEventPayload());
           this.handleGradSchoolUpdateEvent(event);
        } else {
            log.info("silently ignoring other events.");
        }
      } catch (final Exception e) {
         log.error("Exception", e);
      }
    }

    @Override
    public String getTopicToSubscribe() {
        return GRAD_SCHOOL_EVENTS_TOPIC.toString();
    }

    public void handleGradSchoolUpdateEvent(Event event) throws IOException {
     var school = JsonUtil.getJsonObjectFromBytes(GradSchool.class, event.getEventPayload().getBytes());
         if(school.getCanIssueTranscripts().equalsIgnoreCase("N")) {
            edxUsersService.removeGradAdminRoleIfExists(school);
     }
    }
}
