package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeNoteMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeNoteEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestNoteRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeNote;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import javax.transaction.Transactional;
import java.util.HashSet;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class SecureExchangeNoteControllerTest extends BaseSecureExchangeControllerTest {

    private static final SecureExchangeNoteMapper noteMapper = SecureExchangeNoteMapper.mapper;
    private static final SecureExchangeEntityMapper exchangeMapper = SecureExchangeEntityMapper.mapper;

    private static String testExchangeID;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    SecureExchangeRequestRepository secureExchangeRequestRepository;

    @Autowired
    SecureExchangeRequestNoteRepository secureExchangeRequestNoteRepository;

    @Before
    public void setup(){
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(exchangeMapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        testExchangeID = entity.getSecureExchangeID().toString();
    }

    // *** add notes tests

    @Test
    public void testAddNote_GivenInvalidTimestampField_ExpectReturns400ValidationError() throws Exception {
        final String noteJson = this.createDummyNoteJson(testExchangeID, "test content", "Chris", "2020-02-09T00:00:00r");
        this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_NOTES, testExchangeID)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(
                        MockMvcResultMatchers.jsonPath("$.subErrors[0].message",
                        Matchers.containsString("Expected pattern is yyyy-mm-ddTHH:MM:SS"))
                );
    }

    // test add note with invalid exchangeid returns 404
    @Test
    public void testAddNote_GivenInvalidExchangeId_ExpectReturns404NotFoundError() throws Exception {
        final String noteJson = this.createDummyNoteJson(testExchangeID, "test content", "Chris", "2020-02-09T00:00:00");
        this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_NOTES, UUID.randomUUID().toString())
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    // test add note returns 201 created
    @Test
    public void testAddNote_GivenValidExchangeAndValidNote_ExpectReturns201Created() throws Exception {
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(exchangeMapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        String testId = entity.getSecureExchangeID().toString();
        final String noteJson = this.createDummyNoteJson(testId, "test content", "Chris", "2020-02-09T00:00:00");
        this.mockMvc.perform(post(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_NOTES, testId)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_SECURE_EXCHANGE")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(noteJson)
                .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    // *** test get notes

    // test get notes with invalid exhangeid returns 404
    @Test
    public void testGetNotes_GivenInvalidExchangeId_ExpectReturns404NotFound() throws Exception {
        this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_NOTES, UUID.randomUUID())
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isNotFound());
    }

    // test get notes with valid exchangeid returns notes
    @Test
    @Transactional
    public void testGetNotes_GivenValidExchangeId_ExpectReturns200OkWithNotes() throws Exception {
        ObjectMapper objectMapper1 = new ObjectMapper();
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(exchangeMapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        SecureExchangeNoteEntity note = noteMapper.toModel(objectMapper1.readValue(
                this.createDummyNoteJsonWithAuditColumns(entity.getSecureExchangeID().toString(), "Test", "CDITCHER", "2020-02-09T00:00:00"),
                SecureExchangeNote.class
        ));
        note.setSecureExchangeEntity(entity);
        this.secureExchangeRequestNoteRepository.save(note);
        entity.setSecureExchangeNotes(new HashSet<>());
        entity.getSecureExchangeNotes().add(note);
        this.secureExchangeRequestRepository.save(entity);
        this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_NOTES, entity.getSecureExchangeID().toString())
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isOk());
    }

    // test getting a valid exchange that contains no notes, expect no content
    @Test
    public void testGetNotes_GivenValidExchangeIdWithNoNotes_ExpectReturns204NoContent() throws Exception {
        SecureExchangeEntity entity = this.secureExchangeRequestRepository.save(exchangeMapper.toModel(this.getSecureExchangeEntityFromJsonString()));
        String exchangeId = entity.getSecureExchangeID().toString();
        this.mockMvc.perform(get(URL.BASE_URL_SECURE_EXCHANGE+"/" +URL.SECURE_EXCHANGE_ID_NOTES, exchangeId)
                .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_SECURE_EXCHANGE"))))
                .andDo(print()).andExpect(status().isNoContent());
    }

    private String createDummyNoteJson(String secureExchangeID, String content, String user, String timeStamp){
        return "{\n" +
                "  \"secureExchangeID\": \"" + secureExchangeID + "\",\n" +
                "  \"content\": \"" + content + "\",\n" +
                "  \"staffUserIdentifier\": \"" + user + "\",\n" +
                "  \"staffUserName\": \"" + user + "\",\n" +
                "  \"noteTimestamp\": \"" + timeStamp + "\"\n" +
                "}";
    }

    private String createDummyNoteJsonWithAuditColumns(String secureExchangeID, String content, String user, String timeStamp){
        return "{\n" +
                "  \"secureExchangeID\": \"" + secureExchangeID + "\",\n" +
                "  \"content\": \"" + content + "\",\n" +
                "  \"staffUserIdentifier\": \"" + user + "\",\n" +
                "  \"noteTimestamp\": \"" + timeStamp + "\",\n" +
                "  \"createUser\": \"" + user + "\",\n" +
                "  \"updateUser\": \"" + user + "\",\n" +
                "  \"createDate\": \"" + timeStamp + "\",\n" +
                "  \"updateDate\": \"" + timeStamp + "\"\n" +
                "}";
    }

}
