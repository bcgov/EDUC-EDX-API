package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.constants.v1.URL;
import ca.bc.gov.educ.api.edx.controller.v1.SecureExchangeDocumentController;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocMetadata;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocument;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import ca.bc.gov.educ.api.edx.support.DocumentTypeCodeBuilder;
import ca.bc.gov.educ.api.edx.support.SecureExchangeBuilder;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class SecureExchangeDocumentControllerTest extends BaseSecureExchangeAPITest {
  @Autowired
  private MockMvc mvc;

  @Autowired
  SecureExchangeDocumentController secureExchangeDocumentController;

  @Autowired
  private DocumentRepository repository;

  @Autowired
  private SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Autowired
  private DocumentTypeCodeTableRepository documentTypeCodeRepository;

  @Autowired
  private ApplicationProperties props;

  private UUID documentID;

  private UUID penReqID = UUID.randomUUID();

  @Before
  public void setUp() {

    DocumentTypeCodeBuilder.setUpDocumentTypeCodes(this.documentTypeCodeRepository);

    SecureExchangeEntity secureExchange = new SecureExchangeBuilder()
            .withoutSecureExchangeID().build();
    SecureExchangeDocumentEntity document = new DocumentBuilder()
            .withoutDocumentID()
            //.withoutCreateAndUpdateUser()
            .withSecureExchange(secureExchange)
            .withTypeCode("CAPASSPORT")
            .build();
    secureExchange = this.secureExchangeRequestRepository.save(secureExchange);
    document = this.repository.save(document);
    this.penReqID = secureExchange.getSecureExchangeID();
    this.documentID = document.getDocumentID();
  }

  @Test
  public void readDocumentTest() throws Exception {
    this.mvc.perform(get(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS+URL.DOCUMENT_ID, this.penReqID, this.documentID.toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_DOCUMENT")))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.documentID", is(this.documentID.toString())))
            .andExpect(jsonPath("$.documentTypeCode", is("CAPASSPORT")))
            .andExpect(jsonPath("$.documentData", is("TXkgY2FyZCE=")));
  }

  @Test
  public void createDocumentTest() throws Exception {
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(Files.readAllBytes(new ClassPathResource(
                    "../model/document-req.json", SecureExchangeDocumentControllerTest.class).getFile().toPath()))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated())
            .andDo(print())
            .andExpect(jsonPath("$.documentID", not(is(this.documentID.toString()))))
            .andExpect(jsonPath("$.documentTypeCode", is("BCSCPHOTO")))
            .andExpect(jsonPath("$.documentData").doesNotExist())
            .andExpect(jsonPath("$.secureExchangeID").doesNotExist());
  }

  @Test
  public void updateDocumentTest() throws Exception {
    final var result = this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(Files.readAllBytes(new ClassPathResource(
            "../model/document-req.json", SecureExchangeDocumentControllerTest.class).getFile().toPath()))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isCreated())
        .andDo(print())
        .andExpect(jsonPath("$.documentID", not(is(this.documentID.toString()))))
        .andExpect(jsonPath("$.documentTypeCode", is("BCSCPHOTO")))
        .andExpect(jsonPath("$.documentData").doesNotExist())
        .andExpect(jsonPath("$.secureExchangeID").doesNotExist()).andReturn();
    assertThat(result).isNotNull();
    assertThat(result.getResponse().getContentAsString()).isNotBlank();
    assertThat(result.getResponse().getContentType()).isEqualTo("application/json");
    final SecureExchangeDocMetadata secureExchangeDocMetadata = JsonUtil.getJsonObjectFromString(SecureExchangeDocMetadata.class,result.getResponse().getContentAsString());
    secureExchangeDocMetadata.setCreateDate(null);
    secureExchangeDocMetadata.setFileExtension("pdf");
    this.mvc.perform(put(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS+URL.DOCUMENT_ID, this.penReqID, secureExchangeDocMetadata.getDocumentID())
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
        .contentType(MediaType.APPLICATION_JSON)
        .content(JsonUtil.getJsonStringFromObject(secureExchangeDocMetadata))
        .accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andDo(print())
        .andExpect(jsonPath("$.documentID", not(is(this.documentID.toString()))))
        .andExpect(jsonPath("$.documentTypeCode", is("BCSCPHOTO")))
        .andExpect(jsonPath("$.fileExtension", is("pdf")))
        .andExpect(jsonPath("$.documentData").doesNotExist())
        .andExpect(jsonPath("$.secureExchangeID").doesNotExist());
  }


  @Test
  public void testCreateDocument_GivenMandatoryFieldsNullValues_ShouldReturnStatusBadRequest() throws Exception {
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.geNullDocumentJsonAsString())
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(jsonPath("$.subErrors", hasSize(4)));
  }

  @Test
  public void testCreateDocument_GivenDocumentIdInPayload_ShouldReturnStatusBadRequest() throws Exception {
    final SecureExchangeDocument secureExchangeDocument = this.getDummyDocument(UUID.randomUUID().toString());
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.getDummyDocJsonString(secureExchangeDocument))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(jsonPath("$.message", is(notNullValue())));
  }

  @Test
  public void testCreateDocument_GivenInvalidFileExtension_ShouldReturnStatusBadRequest() throws Exception {
    final SecureExchangeDocument secureExchangeDocument = this.getDummyDocument(null);
    secureExchangeDocument.setFileExtension("exe");
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.getDummyDocJsonString(secureExchangeDocument))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(jsonPath("$.message", containsStringIgnoringCase("fileExtension")));
  }

  @Test
  public void testCreateDocument_GivenInvalidDocumentTypeCode_ShouldReturnStatusBadRequest() throws Exception {
    final SecureExchangeDocument secureExchangeDocument = this.getDummyDocument(null);
    secureExchangeDocument.setDocumentTypeCode("doc");
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.getDummyDocJsonString(secureExchangeDocument))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(jsonPath("$.message", containsStringIgnoringCase("documentTypeCode")));
  }

  @Test
  public void testCreateDocument_GivenFileSizeIsMore_ShouldReturnStatusBadRequest() throws Exception {
    final SecureExchangeDocument secureExchangeDocument = this.getDummyDocument(null);
    secureExchangeDocument.setFileSize(99999999);
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.getDummyDocJsonString(secureExchangeDocument))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(jsonPath("$.message", containsStringIgnoringCase("fileSize")));
  }

  @Test
  public void testCreateDocument_GivenDocTypeNotEffective_ShouldReturnStatusBadRequest() throws Exception {
    final SecureExchangeDocument secureExchangeDocument = this.getDummyDocument(null);
    secureExchangeDocument.setDocumentTypeCode("BCeIdPHOTO");
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.getDummyDocJsonString(secureExchangeDocument))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(jsonPath("$.message", containsStringIgnoringCase("documentTypeCode")));
  }

  @Test
  public void testCreateDocument_GivenDocTypeExpired_ShouldReturnStatusBadRequest() throws Exception {
    final SecureExchangeDocument secureExchangeDocument = this.getDummyDocument(null);
    secureExchangeDocument.setDocumentTypeCode("dl");
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(this.getDummyDocJsonString(secureExchangeDocument))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(jsonPath("$.message", containsStringIgnoringCase("documentTypeCode")));
  }

  @Test
  public void createDocumentWithInvalidFileSizeTest() throws Exception {
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(Files.readAllBytes(new ClassPathResource(
                    "../model/document-req-invalid-filesize.json", SecureExchangeDocumentControllerTest.class).getFile().toPath()))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print())
            .andExpect(jsonPath("$.message", containsStringIgnoringCase("documentData")));
  }

  @Test
  public void createDocumentWithoutDocumentDataTest() throws Exception {
    this.mvc.perform(post(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS, this.penReqID)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "WRITE_DOCUMENT")))
            .contentType(MediaType.APPLICATION_JSON)
            .content(Files.readAllBytes(new ClassPathResource(
                    "../model/document-req-without-doc-data.json", SecureExchangeDocumentControllerTest.class).getFile().toPath()))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest())
            .andDo(print());
  }

  @Test
  public void deleteDocumentTest() throws Exception {
    this.mvc.perform(delete(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS+URL.DOCUMENT_ID, this.penReqID, this.documentID.toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "DELETE_DOCUMENT")))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.documentID", is(this.documentID.toString())))
            .andExpect(jsonPath("$.documentTypeCode", is("CAPASSPORT")))
            .andExpect(jsonPath("$.documentData").doesNotExist());


    assertThat(this.repository.findById(this.documentID)).isEmpty();
  }

  @Test
  public void readAllDocumentMetadataTest() throws Exception {
    this.mvc.perform(get(URL.BASE_URL+URL.SECURE_EXCHANGE_ID_DOCUMENTS,this.penReqID.toString())
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_DOCUMENT")))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.length()", is(1)))
            .andExpect(jsonPath("$.[0].documentID", is(this.documentID.toString())))
            .andExpect(jsonPath("$.[0].documentTypeCode", is("CAPASSPORT")))
            .andExpect(jsonPath("$.[0].documentData").doesNotExist());
  }

  @Test
  public void readAllDocumentMetadataWithoutSecureExchangeIDTest() throws Exception {
    this.mvc.perform(get(URL.BASE_URL+URL.ALL_DOCUMENTS)
        .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_DOCUMENT")))
        .accept(MediaType.APPLICATION_JSON))
      .andExpect(status().isOk())
      .andDo(print())
      .andExpect(jsonPath("$.length()", is(1)))
      .andExpect(jsonPath("$.[0].documentID", is(this.documentID.toString())))
      .andExpect(jsonPath("$.[0].secureExchangeID", is(notNullValue())))
      .andExpect(jsonPath("$.[0].digitalID", is(notNullValue())))
      .andExpect(jsonPath("$.[0].documentTypeCode", is("CAPASSPORT")))
      .andExpect(jsonPath("$.[0].documentData").doesNotExist());
  }
  @Test
  public void getDocumentRequirementsTest() throws Exception {
    this.mvc.perform(get(URL.BASE_URL+URL.FILE_REQUIREMENTS)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_DOCUMENT_REQUIREMENTS")))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.maxSize", is(this.props.getMaxFileSize())))
            .andExpect(jsonPath("$.extensions.length()", is(this.props.getFileExtensions().size())))
            .andExpect(jsonPath("$.extensions[0]", is(this.props.getFileExtensions().get(0))));
  }

  @Test
  public void getDocumentTypesTest() throws Exception {
    this.mvc.perform(get(URL.BASE_URL+URL.DOCUMENT_TYPES)
            .with(jwt().jwt((jwt) -> jwt.claim("scope", "READ_DOCUMENT_TYPES")))
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andDo(print())
            .andExpect(jsonPath("$.length()", is(4)));
  }

  private String geNullDocumentJsonAsString() {
    return "{\n" +
            "    \"documentTypeCode\":" + null + ",\n" +
            "    \"fileName\":" + null + ",\n" +
            "    \"fileExtension\":" + null + ",\n" +
            "    \"fileSize\":" + null + ",\n" +
            "    \"documentData\":" + null + "\n" +
            "}";
  }

  private SecureExchangeDocument getDummyDocument(final String documentId) {
    final SecureExchangeDocument secureExchangeDocument = new SecureExchangeDocument();
    secureExchangeDocument.setDocumentID(documentId);
    secureExchangeDocument.setDocumentData("TXkgY2FyZCE=");
    secureExchangeDocument.setDocumentTypeCode("BCSCPHOTO");
    secureExchangeDocument.setFileName("card.jpg");
    secureExchangeDocument.setFileExtension("jpg");
    secureExchangeDocument.setFileSize(8);
    return secureExchangeDocument;
  }

  protected String getDummyDocJsonString(final SecureExchangeDocument secureExchangeDocument) {
    try {
      return new ObjectMapper().writeValueAsString(secureExchangeDocument);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }


}
