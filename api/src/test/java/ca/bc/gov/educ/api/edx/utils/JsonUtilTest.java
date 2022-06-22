package ca.bc.gov.educ.api.edx.utils;

import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.*;


import static org.assertj.core.api.Assertions.assertThat;

public class JsonUtilTest {

  @Test
  public void getJsonStringFromObject() throws JsonProcessingException {
    EdxUserActivationInviteSagaData sagaData = new EdxUserActivationInviteSagaData();
    assertThat(JsonUtil.getJsonStringFromObject(sagaData)).isNotEmpty();
  }

  @Test
  public void getJsonObjectFromString() throws JsonProcessingException {
    EdxUserActivationInviteSagaData sagaData = new EdxUserActivationInviteSagaData();
    assertThat(JsonUtil.getJsonObjectFromString(EdxUserActivationInviteSagaData.class, JsonUtil.getJsonStringFromObject(sagaData))).isNotNull();
  }

  @Test
  public void getJsonBytesFromObject() throws JsonProcessingException {
    EdxUserActivationInviteSagaData sagaData = new EdxUserActivationInviteSagaData();
    assertThat(JsonUtil.getJsonSBytesFromObject(sagaData)).isNotEmpty();
  }

  @Test
  public void getJsonBytesFromObjectThrowJsonProcessingException() throws IOException {
    EdxUserActivationInviteSagaData sagaData = new EdxUserActivationInviteSagaData();
    assertThat(JsonUtil.getJsonSBytesFromObject(sagaData)).isNotEmpty();
    assertThat(JsonUtil.getJsonObjectFromBytes(EdxUserActivationInviteSagaData.class, JsonUtil.getJsonSBytesFromObject(sagaData))).isNotNull();
  }


}
