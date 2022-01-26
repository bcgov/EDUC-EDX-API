package ca.bc.gov.educ.api.edx.model;

import static org.assertj.core.api.Assertions.assertThat;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocMetadata;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.edx.config.mappers.v1.DocumentMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.DocumentMapperImpl;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DocumentMapperImpl.class})
@AutoConfigureJsonTesters
public class PenReqSecureExchangeDocumentJsonTests {
    @Autowired
    private JacksonTester<SecureExchangeDocument> jsonTester;

    @Autowired
    private JacksonTester<SecureExchangeDocMetadata> documentMetadataTester;

    @Autowired
    private final DocumentMapper mapper = DocumentMapper.mapper;

    private SecureExchangeDocumentEntity document;

    @Before
    public void setUp() {
        this.document = new DocumentBuilder().build();
    }

    @Test
    public void documentSerializeTest() throws Exception {
        JsonContent<SecureExchangeDocument> json = this.jsonTester.write(mapper.toStructure(this.document));

        assertThat(json).hasJsonPathStringValue("@.documentID");
        assertThat(json).extractingJsonPathStringValue("@.documentTypeCode")
            .isEqualToIgnoringCase("BCSCPHOTO");
        assertThat(json).extractingJsonPathStringValue("@.documentData")
            .isEqualToIgnoringCase("TXkgY2FyZCE=");

        assertThat(json).doesNotHaveJsonPathValue("@.penRequest");
    }

    @Test
    public void documentMetadataSerializeTest() throws Exception {
        JsonContent<SecureExchangeDocMetadata> json = this.documentMetadataTester.write(mapper.toMetadataStructure(this.document));

        assertThat(json).hasJsonPathStringValue("@.documentID");
        assertThat(json).extractingJsonPathStringValue("@.documentTypeCode")
            .isEqualToIgnoringCase("BCSCPHOTO");
        assertThat(json).doesNotHaveJsonPathValue("@.documentData").doesNotHaveJsonPathValue("@.penRequest");

    }

    @Test
    public void documentDeserializeTest() throws Exception {
        SecureExchangeDocument penReqDocument = this.jsonTester.readObject("document.json");
        SecureExchangeDocumentEntity secureExchangeDocument = mapper.toModel(penReqDocument);
        assertThat(secureExchangeDocument.getDocumentData()).isEqualTo("My card!".getBytes());
    }

    @Test
    public void documentDeserializeWithExtraTest() throws Exception {
        SecureExchangeDocument secureExchangeDocument = this.jsonTester.readObject("document-extra-properties.json");
        assertThat(secureExchangeDocument.getDocumentData()).isEqualTo("TXkgY2FyZCE=");
    }

}
