package ca.bc.gov.educ.api.edx.model;

import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocRequirement;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@AutoConfigureJsonTesters
@SpringBootTest
@ActiveProfiles("test")
class SecureExchangeDocRequirementEntityJsonTests {
    @Autowired
    private JacksonTester<SecureExchangeDocRequirement> jsonTester;

    @Test
    void requirementSerializeTest() throws Exception {
        int maxSize = 100;
        List<String> extensions = new ArrayList<String>(Arrays.asList("jpg", "png", "pdf"));
        SecureExchangeDocRequirement requirement = new SecureExchangeDocRequirement(maxSize, extensions);

        JsonContent<SecureExchangeDocRequirement> json = this.jsonTester.write(requirement);

        assertThat(json).extractingJsonPathNumberValue("@.maxSize")
            .isEqualTo(maxSize);

        assertThat(json).extractingJsonPathNumberValue("@.extensions.length()")
            .isEqualTo(extensions.size());
        assertThat(json).extractingJsonPathStringValue("@.extensions[0]")
            .isEqualToIgnoringCase(extensions.get(0));
    }

    @Test
    void documentDeserializeTest() throws Exception {
        SecureExchangeDocRequirement document = this.jsonTester.readObject("requirement.json");
        assertThat(document.getMaxSize()).isEqualTo(20);
        assertThat(document.getExtensions()).hasSize(2);
        assertThat(document.getExtensions().get(0)).isEqualTo("pdf");
    }


}
