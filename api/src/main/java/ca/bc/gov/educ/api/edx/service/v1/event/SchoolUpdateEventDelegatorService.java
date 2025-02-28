package ca.bc.gov.educ.api.edx.service.v1.event;

import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.orchestrator.base.EventHandler;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import ca.bc.gov.educ.api.edx.struct.v1.School;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static ca.bc.gov.educ.api.edx.constants.TopicsEnum.INSTITUTE_API_TOPIC;

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
        log.info("Handle Event :: {}", event.getEventType());
      try {
        if(event.getEventType().equalsIgnoreCase(EventType.UPDATE_SCHOOL.toString())) {
           log.info("Received UPDATE_SCHOOL event :: {}", event.getSagaId());
           log.trace(PAYLOAD_LOG, event.getEventPayload());
           this.handleSchoolUpdateEvent(event);
        } else {
            log.info("silently ignoring other events.");
        }
      } catch (final Exception e) {
         log.error("Exception", e);
      }
    }

    @Override
    public String getTopicToSubscribe() {
        return INSTITUTE_API_TOPIC.toString();
    }

    public void handleSchoolUpdateEvent(Event event) throws IOException {
     var school = JsonUtil.getJsonObjectFromBytes(School.class, event.getEventPayload().getBytes());
     if(school.getClosedDate() != null) {
         edxUsersService.setExpiryDateOnUsersOfClosedSchool(school);
     } else if(Boolean.FALSE.equals(school.getCanIssueTranscripts())) {
         edxUsersService.removeGradAdminRoleIfExists(school);
     }
    }
}
