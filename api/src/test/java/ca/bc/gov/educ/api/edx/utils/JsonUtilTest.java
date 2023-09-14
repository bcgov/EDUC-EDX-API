package ca.bc.gov.educ.api.edx.utils;

import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolActivationInviteSagaData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonUtilTest {

  @Test
  void getJsonStringFromObject() throws JsonProcessingException {
    EdxUserSchoolActivationInviteSagaData sagaData = new EdxUserSchoolActivationInviteSagaData();
    assertThat(JsonUtil.getJsonStringFromObject(sagaData)).isNotEmpty();
  }

  @Test
  void getJsonObjectFromString() throws JsonProcessingException {
    EdxUserSchoolActivationInviteSagaData sagaData = new EdxUserSchoolActivationInviteSagaData();
    assertThat(JsonUtil.getJsonObjectFromString(EdxUserSchoolActivationInviteSagaData.class, JsonUtil.getJsonStringFromObject(sagaData))).isNotNull();
  }

  @Test
  void getJsonBytesFromObject() throws JsonProcessingException {
    EdxUserSchoolActivationInviteSagaData sagaData = new EdxUserSchoolActivationInviteSagaData();
    assertThat(JsonUtil.getJsonSBytesFromObject(sagaData)).isNotEmpty();
  }

  @Test
  void getJsonBytesFromObjectThrowJsonProcessingException() throws IOException {
    EdxUserSchoolActivationInviteSagaData sagaData = new EdxUserSchoolActivationInviteSagaData();
    assertThat(JsonUtil.getJsonSBytesFromObject(sagaData)).isNotEmpty();
    assertThat(JsonUtil.getJsonObjectFromBytes(EdxUserSchoolActivationInviteSagaData.class, JsonUtil.getJsonSBytesFromObject(sagaData))).isNotNull();
  }


}
