package ca.bc.gov.educ.api.edx.model;

import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeDocumentMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeDocumentMapperImpl;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocMetadata;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocument;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {SecureExchangeDocumentMapperImpl.class})
@AutoConfigureJsonTesters
@SpringBootTest
class SecureExchangeDocumentJsonTests {
    @Autowired
    private JacksonTester<SecureExchangeDocument> jsonTester;

    @Autowired
    private JacksonTester<SecureExchangeDocMetadata> documentMetadataTester;

    @Autowired
    private final SecureExchangeDocumentMapper mapper = SecureExchangeDocumentMapper.mapper;

    private SecureExchangeDocumentEntity document;

    @BeforeEach
    public void setUp() {
        this.document = new DocumentBuilder().build();
    }

    @Test
    void documentSerializeTest() throws Exception {
        JsonContent<SecureExchangeDocument> json = this.jsonTester.write(mapper.toStructure(this.document));

        assertThat(json).hasJsonPathStringValue("@.documentID");
        assertThat(json).extractingJsonPathStringValue("@.documentTypeCode")
            .isEqualToIgnoringCase("BCSCPHOTO");
        assertThat(json).extractingJsonPathStringValue("@.documentData")
            .isEqualToIgnoringCase("TXkgY2FyZCE=");

        assertThat(json).doesNotHaveJsonPathValue("@.secureExchange");
    }

    @Test
    void documentMetadataSerializeTest() throws Exception {
        JsonContent<SecureExchangeDocMetadata> json = this.documentMetadataTester.write(mapper.toMetadataStructure(this.document));

        assertThat(json).hasJsonPathStringValue("@.documentID");
        assertThat(json).extractingJsonPathStringValue("@.documentTypeCode")
            .isEqualToIgnoringCase("BCSCPHOTO");
        assertThat(json).doesNotHaveJsonPathValue("@.documentData").doesNotHaveJsonPathValue("@.secureExchange");

    }

    @Test
    void documentDeserializeTest() throws Exception {
        SecureExchangeDocument penReqDocument = this.jsonTester.readObject("document.json");
        SecureExchangeDocumentEntity secureExchangeDocument = mapper.toModel(penReqDocument);
        assertThat(secureExchangeDocument.getDocumentData()).isEqualTo("My card!".getBytes());
    }

    @Test
    void documentDeserializeWithExtraTest() throws Exception {
        SecureExchangeDocument secureExchangeDocument = this.jsonTester.readObject("document-extra-properties.json");
        assertThat(secureExchangeDocument.getDocumentData()).isEqualTo("TXkgY2FyZCE=");
    }

}
