package ca.bc.gov.educ.api.edx.model;

import static org.assertj.core.api.Assertions.assertThat;

import ca.bc.gov.educ.api.edx.model.v1.DocumentEntity;
import ca.bc.gov.educ.api.edx.struct.v1.PenReqDocMetadata;
import ca.bc.gov.educ.api.edx.struct.v1.PenReqDocument;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import ca.bc.gov.educ.api.edx.mappers.v1.DocumentMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.DocumentMapperImpl;
import ca.bc.gov.educ.api.edx.support.DocumentBuilder;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {DocumentMapperImpl.class})
@AutoConfigureJsonTesters
public class PenReqDocumentEntityJsonTests {
    @Autowired
    private JacksonTester<PenReqDocument> jsonTester;

    @Autowired
    private JacksonTester<PenReqDocMetadata> documentMetadataTester;

    @Autowired
    private final DocumentMapper mapper = DocumentMapper.mapper;

    private DocumentEntity document;

    @Before
    public void setUp() {
        this.document = new DocumentBuilder().build();
    }

    @Test
    public void documentSerializeTest() throws Exception {
        JsonContent<PenReqDocument> json = this.jsonTester.write(mapper.toStructure(this.document));

        assertThat(json).hasJsonPathStringValue("@.documentID");
        assertThat(json).extractingJsonPathStringValue("@.documentTypeCode")
            .isEqualToIgnoringCase("BCSCPHOTO");
        assertThat(json).extractingJsonPathStringValue("@.documentData")
            .isEqualToIgnoringCase("TXkgY2FyZCE=");

        assertThat(json).doesNotHaveJsonPathValue("@.penRequest");
    }

    @Test
    public void documentMetadataSerializeTest() throws Exception {
        JsonContent<PenReqDocMetadata> json = this.documentMetadataTester.write(mapper.toMetadataStructure(this.document));

        assertThat(json).hasJsonPathStringValue("@.documentID");
        assertThat(json).extractingJsonPathStringValue("@.documentTypeCode")
            .isEqualToIgnoringCase("BCSCPHOTO");
        assertThat(json).doesNotHaveJsonPathValue("@.documentData").doesNotHaveJsonPathValue("@.penRequest");

    }

    @Test
    public void documentDeserializeTest() throws Exception {
        PenReqDocument penReqDocument = this.jsonTester.readObject("document.json");
        DocumentEntity documentEntity = mapper.toModel(penReqDocument);
        assertThat(documentEntity.getDocumentData()).isEqualTo("My card!".getBytes());
    }

    @Test
    public void documentDeserializeWithExtraTest() throws Exception {
        PenReqDocument penReqDocument = this.jsonTester.readObject("document-extra-properties.json");
        assertThat(penReqDocument.getDocumentData()).isEqualTo("TXkgY2FyZCE=");
    }

}
