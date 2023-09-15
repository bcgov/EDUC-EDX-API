package ca.bc.gov.educ.api.edx.service;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeNoteEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestNoteRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeNoteService;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.fail;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

class SecureExchangeNoteServiceGetDeleteTests extends BaseSecureExchangeAPITest {
    @Autowired
    SecureExchangeNoteService secureExchangeNoteService;

    @Autowired
    SecureExchangeRequestRepository secureExchangeRequestRepository;

    @Autowired
    SecureExchangeRequestNoteRepository secureExchangeRequestNoteRepository;

    @Test
    @Transactional
    void testDeleteNoteFromExchange() {
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(
                addNoteToSecureExchangeEntity(createSecureExchange())
        );
        Set<SecureExchangeNoteEntity> notes = entity.getSecureExchangeNotes();
        assertThat(notes).hasSize(1);
        SecureExchangeNoteEntity note = notes.stream()
                .findAny()
                .orElse(null);
        if(note == null){
            fail("Note not found");
        }

        this.secureExchangeNoteService.deleteNoteFromExchange(note.getSecureExchangeNoteID());
        SecureExchangeNoteEntity noteThatShouldBeNull = secureExchangeRequestNoteRepository.findById(note.getSecureExchangeNoteID()).orElse(null);
        MatcherAssert.assertThat(noteThatShouldBeNull, equalTo(null));
    }

    private SecureExchangeEntity createSecureExchange(){
        return new SecureExchangeBuilder().withoutSecureExchangeID().build();
    }

    private SecureExchangeEntity addNoteToSecureExchangeEntity(SecureExchangeEntity secureExchange){
        if(secureExchange.getSecureExchangeNotes() == null){
            secureExchange.setSecureExchangeNotes(new HashSet<>());
        }
        SecureExchangeNoteEntity note = new SecureExchangeNoteEntity();
        note.setSecureExchangeEntity(secureExchange);
        note.setContent("Regression Test Note Content");
        note.setCreateUser(ApplicationProperties.CLIENT_ID);
        note.setStaffUserIdentifier(ApplicationProperties.CLIENT_ID);
        note.setNoteTimestamp(LocalDateTime.now());
        note.setUpdateDate(LocalDateTime.now());
        note.setUpdateUser(ApplicationProperties.CLIENT_ID);
        note.setCreateDate(LocalDateTime.now());
        secureExchange.getSecureExchangeNotes().add(note);
        return secureExchange;
    }
}
