package ca.bc.gov.educ.api.edx.rest;

import ca.bc.gov.educ.api.edx.constants.EventType;
import ca.bc.gov.educ.api.edx.exception.SagaRuntimeException;
import ca.bc.gov.educ.api.edx.filter.FilterOperation;
import ca.bc.gov.educ.api.edx.messaging.MessagePublisher;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.struct.institute.v1.SchoolTombstone;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.JsonUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;

/**
 * This class is used for REST calls
 *
 * @author Marco Villeneuve
 */
@Component
@Slf4j
public class RestUtils {
  public static final String NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID = "Either NATS timed out or the response is null , correlationID :: ";
  private final WebClient chesWebClient;
  private final WebClient webClient;
  private final ApplicationProperties props;
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final MessagePublisher messagePublisher;
  private static final String INSTITUTE_API_TOPIC = "INSTITUTE_API_TOPIC";

  public RestUtils(@Qualifier("chesWebClient") final WebClient chesWebClient, final WebClient webClient, final ApplicationProperties props, final MessagePublisher messagePublisher) {
    this.chesWebClient = chesWebClient;
    this.webClient = webClient;
    this.props = props;
    this.messagePublisher = messagePublisher;
  }


  /**
   * Send email.
   *
   * @param chesEmail the ches email json object as string
   */
  public void sendEmail(final CHESEmail chesEmail) {
    if (this.props.getIsEmailNotificationSwitchedOn() != null && this.props.getIsEmailNotificationSwitchedOn()) {
      this.chesWebClient
          .post()
          .uri(this.props.getChesEndpointURL())
          .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
          .body(Mono.just(chesEmail), CHESEmail.class)
          .retrieve()
          .bodyToMono(String.class)
          .doOnError(error -> this.logError(error, chesEmail))
          .doOnSuccess(success -> this.onSendEmailSuccess(success, chesEmail))
          .block();
    } else {
      log.info("email outbound to CHES is switched off");
    }
  }

  private void logError(final Throwable throwable, final CHESEmail chesEmailEntity) {
    log.error("Error from CHES API call :: {} ", chesEmailEntity, throwable);
  }

  private void onSendEmailSuccess(final String s, final CHESEmail chesEmailEntity) {
    log.info("Email sent success :: {} :: {}", chesEmailEntity, s);
  }

  public CHESEmail getChesEmail(final String emailAddress, final String body, final String subject) {
    final CHESEmail chesEmail = new CHESEmail();
    chesEmail.setBody(body);
    chesEmail.setBodyType("html");
    chesEmail.setDelayTS(0);
    chesEmail.setEncoding("utf-8");
    chesEmail.setFrom("noreply-edx@gov.bc.ca");
    chesEmail.setPriority("normal");
    chesEmail.setSubject(subject);
    chesEmail.setTag("tag");
    chesEmail.getTo().add(emailAddress);
    return chesEmail;
  }

  public CHESEmail getChesEmail(final String fromEmail, final String toEmail, final String body, final String subject) {
    final CHESEmail chesEmail = new CHESEmail();
    chesEmail.setBody(body);
    chesEmail.setBodyType("html");
    chesEmail.setDelayTS(0);
    chesEmail.setEncoding("utf-8");
    chesEmail.setFrom(fromEmail);
    chesEmail.setPriority("normal");
    chesEmail.setSubject(subject);
    chesEmail.setTag("tag");
    chesEmail.getTo().add(toEmail);
    return chesEmail;
  }

  public void sendEmail(final String emailAddress, final String body, final String subject) {
    this.sendEmail(this.getChesEmail(emailAddress, body, subject));
  }

  public void sendEmail(final String fromEmail, final String toEmail, final String body, final String subject) {
    this.sendEmail(this.getChesEmail(fromEmail, toEmail, body, subject));
  }

  public Map<String, SchoolTombstone> getSchoolMap() {
    Map<String, SchoolTombstone> schoolMap = new ConcurrentHashMap<>();
    for (val school : this.getSchools()) {
      schoolMap.put(school.getSchoolId(), school);
    }

    return schoolMap;
  }

  public Map<String, SchoolTombstone> getSchoolMincodeMap() {
    Map<String, SchoolTombstone> schoolMap = new ConcurrentHashMap<>();
    for (val school : this.getSchools()) {
      schoolMap.put(school.getMincode(), school);
    }

    return schoolMap;
  }

  public Map<String, List<SchoolTombstone>> getDistrictSchoolsMap() {
    Map<String, List<SchoolTombstone>> districtSchoolsMap = new ConcurrentHashMap<>();
    for (val school : this.getSchools()) {
      if(!districtSchoolsMap.containsKey(school.getDistrictId())){
        districtSchoolsMap.put(school.getDistrictId(),new ArrayList<>());
      }

      districtSchoolsMap.get(school.getDistrictId()).add(school);
    }

    return districtSchoolsMap;
  }

  public List<SchoolTombstone> getSchools() {
    log.info("Calling Institute api to get list of schools");
    return this.webClient.get()
        .uri(this.props.getInstituteApiURL() + "/school")
        .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .bodyToFlux(SchoolTombstone.class)
        .collectList()
        .block();
  }

  public Map<String, District> getDistrictMap() {
    Map<String, District> districtMap = new ConcurrentHashMap<>();
    for (val district : this.getDistricts()) {
      districtMap.put(district.getDistrictId(), district);
    }

    return districtMap;
  }

  public Map<String, District> getDistrictNumberMap() {
    Map<String, District> districtMap = new ConcurrentHashMap<>();
    for (val district : this.getDistricts()) {
      districtMap.put(district.getDistrictNumber(), district);
    }

    return districtMap;
  }

  public List<District> getDistricts() {
    log.info("Calling Institute api to get list of districts");
    return this.webClient.get()
        .uri(this.props.getInstituteApiURL() + "/district")
        .header(CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .retrieve()
        .bodyToFlux(District.class)
        .collectList()
        .block();
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public List<School> getSchoolNumberInDistrict(UUID correlationID, String schoolNumber, String districtId) {
    try {
      final SearchCriteria schoolNumberCriteria = this.getCriteria("schoolNumber", FilterOperation.EQUAL, schoolNumber , ValueType.STRING);
      final SearchCriteria districtIdCriteria = this.getCriteria("districtID", FilterOperation.EQUAL, districtId , ValueType.UUID);

      final List<SearchCriteria> criteriaSchoolNumber = new LinkedList<>(Collections.singletonList(schoolNumberCriteria));
      final List<SearchCriteria> criteriaDistrictId = new LinkedList<>(Collections.singletonList(districtIdCriteria));
      final List<Search> searches = new LinkedList<>();
      searches.add(Search.builder().searchCriteriaList(criteriaSchoolNumber).build());
      searches.add(Search.builder().condition(Condition.AND).searchCriteriaList(criteriaDistrictId).build());

      log.debug("Sys Criteria: {}", searches);
      final TypeReference<List<School>> ref = new TypeReference<>() {
      };
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.GET_PAGINATED_SCHOOLS).eventPayload("searchCriteriaList".concat("=").concat(URLEncoder.encode(this.objectMapper.writeValueAsString(searches), StandardCharsets.UTF_8)).concat("&").concat("pageSize").concat("=").concat("100000")).build();
      val responseMessage = this.messagePublisher.requestPaginatedMessage(INSTITUTE_API_TOPIC, JsonUtil.getJsonSBytesFromObject(event)).completeOnTimeout(null, 60, TimeUnit.SECONDS).get();
      if (null != responseMessage) {
        return objectMapper.readValue(responseMessage.getData(), ref);
      } else {
        throw new SagaRuntimeException(NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID + correlationID);
      }

    } catch (final Exception ex) {
      Thread.currentThread().interrupt();
      throw new SagaRuntimeException(NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID + correlationID + ex.getMessage());
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public List<School> getSchoolById(UUID correlationID, String schoolId) {
    try {
      final SearchCriteria schoolIdCriteria = this.getCriteria("schoolId", FilterOperation.EQUAL, schoolId , ValueType.UUID);

      final List<SearchCriteria> criteriaSchoolId = new LinkedList<>(Collections.singletonList(schoolIdCriteria));
      final List<Search> searches = new LinkedList<>();
      searches.add(Search.builder().searchCriteriaList(criteriaSchoolId).build());

      log.debug("Sys Criteria: {}", searches);
      final TypeReference<List<School>> ref = new TypeReference<>() {
      };
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.GET_PAGINATED_SCHOOLS).eventPayload("searchCriteriaList".concat("=").concat(URLEncoder.encode(this.objectMapper.writeValueAsString(searches), StandardCharsets.UTF_8)).concat("&").concat("pageSize").concat("=").concat("100000")).build();
      val responseMessage = this.messagePublisher.requestPaginatedMessage(INSTITUTE_API_TOPIC, JsonUtil.getJsonSBytesFromObject(event)).completeOnTimeout(null, 60, TimeUnit.SECONDS).get();
      if (null != responseMessage) {
        return objectMapper.readValue(responseMessage.getData(), ref);
      } else {
        throw new SagaRuntimeException(NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID + correlationID);
      }

    } catch (final Exception ex) {
      Thread.currentThread().interrupt();
      throw new SagaRuntimeException(NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID + correlationID + ex.getMessage());
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public School createSchool(UUID correlationID,School school) {
    try {
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.CREATE_SCHOOL).eventPayload(objectMapper.writeValueAsString(school)).build();
      val responseMessage = this.messagePublisher.requestPaginatedMessage(INSTITUTE_API_TOPIC, JsonUtil.getJsonSBytesFromObject(event)).get();
      if (null != responseMessage) {
        Event eventPayload = JsonUtil.getJsonObjectFromBytes(Event.class, responseMessage.getData());
        return JsonUtil.getJsonObjectFromBytes(School.class,
                eventPayload.getEventPayload().getBytes());
      } else {
        throw new SagaRuntimeException(NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID + correlationID);
      }
    } catch (final Exception ex) {
      Thread.currentThread().interrupt();
      throw new SagaRuntimeException(NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID + correlationID + ex.getMessage());
    }
  }

  @Retryable(retryFor = {Exception.class}, noRetryFor = {SagaRuntimeException.class}, backoff = @Backoff(multiplier = 2, delay = 2000))
  public School updateSchool(UUID correlationID,School school) {
    try {
      Object event = Event.builder().sagaId(correlationID).eventType(EventType.UPDATE_SCHOOL).eventPayload(objectMapper.writeValueAsString(school)).build();
      val responseMessage = this.messagePublisher.requestPaginatedMessage(INSTITUTE_API_TOPIC, JsonUtil.getJsonSBytesFromObject(event)).get();
      if (null != responseMessage) {
        Event eventPayload = JsonUtil.getJsonObjectFromBytes(Event.class, responseMessage.getData());
        return JsonUtil.getJsonObjectFromBytes(School.class,
                eventPayload.getEventPayload().getBytes());
      } else {
        throw new SagaRuntimeException(NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID + correlationID);
      }
    } catch (final Exception ex) {
      Thread.currentThread().interrupt();
      throw new SagaRuntimeException(NATS_TIMED_OUT_OR_THE_RESPONSE_IS_NULL_CORRELATION_ID + correlationID + ex.getMessage());
    }
  }

  private SearchCriteria getCriteria(final String key, final FilterOperation operation, final String value, final ValueType valueType) {
    return SearchCriteria.builder().key(key).operation(operation).value(value).valueType(valueType).build();
  }
}
