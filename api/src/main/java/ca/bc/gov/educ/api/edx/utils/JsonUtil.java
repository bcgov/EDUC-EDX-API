package ca.bc.gov.educ.api.edx.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Optional;

@Slf4j
public class JsonUtil {
  private static final ObjectMapper mapper = new ObjectMapper();
  private JsonUtil(){
  }
  public static String getJsonStringFromObject(Object payload) throws JsonProcessingException {
    return mapper.writeValueAsString(payload);
  }

  public static <T> T getJsonObjectFromString(Class<T> clazz,  String payload) throws JsonProcessingException {
    return mapper.readValue(payload,clazz);
  }

  public static byte[] getJsonSBytesFromObject(Object payload) throws JsonProcessingException {
    return mapper.writeValueAsBytes(payload);
  }

  public static <T> T getJsonObjectFromBytes(Class<T> clazz,  byte[] payload) throws IOException {
    return mapper.readValue(payload,clazz);
  }

  /**
   * Get json string optional.
   *
   * @param payload the payload
   * @return the optional
   */
  public static Optional<String> getJsonString(final Object payload) {
    try {
      return Optional.ofNullable(new ObjectMapper().writeValueAsString(payload));
    } catch (final Exception ex) {
      log.error("Exception while converting object to JSON String :: {}", payload);
    }
    return Optional.empty();
  }
}
