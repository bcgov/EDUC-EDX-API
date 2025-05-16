package ca.bc.gov.educ.api.edx.service.v1.event;

import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.model.v1.EdxEvent;
import ca.bc.gov.educ.api.edx.repository.EdxEventRepository;
import ca.bc.gov.educ.api.edx.service.v1.BaseService;
import ca.bc.gov.educ.api.edx.service.v1.EdxUsersService;
import ca.bc.gov.educ.api.edx.struct.gradschool.v1.GradSchool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class SchoolUpdateEventDelegatorService extends BaseService<GradSchool> {

    private final EdxUsersService edxUsersService;
    private final EdxEventRepository eventRepository;

    public SchoolUpdateEventDelegatorService(EdxUsersService edxUsersService, EdxEventRepository eventRepository) {
        super(eventRepository);
        this.edxUsersService = edxUsersService;
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processEvent(final GradSchool gradSchool, EdxEvent event) {
        log.info("Received and processing event: " + event.getEventId());
        if(gradSchool.getCanIssueTranscripts().equalsIgnoreCase("N")) {
            edxUsersService.removeGradAdminRoleIfExists(gradSchool);
        }
        updateEvent(event);
    }

    @Override
    public String getEventType() {
        return EventType.UPDATE_GRAD_SCHOOL.toString();
    }
}
