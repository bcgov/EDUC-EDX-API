package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeContactTypeCodeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.RequestUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.UUID;

public abstract class BaseSagaControllerTest extends BaseSecureExchangeControllerTest {

  protected String secureExchangeCreateJsonWithMinAndComment(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"secureExchangeStatusCode\":\"OPEN\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\", \"commentsList\": [{\"staffUserIdentifier\": \"TEST\", \"commentUserName\": \"JACKSON, JAMES\", \"content\": \"This is content\", \"updateUser\":\"TEST\",\"createUser\":\"TEST\"}]}";
  }

  protected String secureExchangeCommentJson(final String secureExchangeID) {
    return "{\n" +
      "  \"secureExchangeID\": \"" + secureExchangeID + "\",\n" +
      "  \"content\": \"" + "comment1" + "\",\n" +
      "  \"commentUserName\": \"" + "user1" + "\",\n" +
      "  \"createUser\": \"" + "user1" + "\",\n" +
      "  \"updateUser\": \"" + "user1" + "\",\n" +
      "  \"staffUserIdentifier\": \"" + UUID.randomUUID() + "\",\n" +
      "  \"commentTimestamp\": \"2020-02-09T00:00:00\"\n" +
      "}";
  }

  protected MinistryOwnershipTeamEntity getMinistryOwnershipTeam() {
    MinistryOwnershipTeamEntity entity = new MinistryOwnershipTeamEntity();
    entity.setCreateDate(LocalDateTime.now());
    entity.setUpdateDate(LocalDateTime.now());
    entity.setUpdateUser("JACK");
    entity.setCreateUser("JACK");
    entity.setUpdateDate(LocalDateTime.now());
    entity.setTeamName("JOHN");
    entity.setGroupRoleIdentifier("ABC");
    return entity;
  }

  protected SecureExchangeCreateSagaData createSecureExchangeCreateSagaData(SecureExchangeCreate secureExchangeCreate, String mincode, String schoolName, String ministryTeamName) {
    SecureExchangeCreateSagaData sagaData = new SecureExchangeCreateSagaData();
    sagaData.setSecureExchangeCreate(secureExchangeCreate);
    sagaData.setSecureExchangeCreate(secureExchangeCreate);
    sagaData.setMincode(mincode);
    sagaData.setSchoolName(schoolName);
    sagaData.setMinistryTeamName(ministryTeamName);
    return sagaData;
  }

  protected SecureExchangeContactTypeCodeEntity createContactType() {
    final SecureExchangeContactTypeCodeEntity entity = new SecureExchangeContactTypeCodeEntity();
    entity.setSecureExchangeContactTypeCode("EDXUSER");
    entity.setDescription("Initial Review");
    entity.setDisplayOrder(1);
    entity.setEffectiveDate(LocalDateTime.now());
    entity.setLabel("Initial Review");
    entity.setCreateDate(LocalDateTime.now());
    entity.setCreateUser("TEST");
    entity.setUpdateUser("TEST");
    entity.setUpdateDate(LocalDateTime.now());
    entity.setExpiryDate(LocalDateTime.from(new GregorianCalendar(2099, Calendar.FEBRUARY, 1).toZonedDateTime()));
    return entity;
  }

  protected ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity createNewStatus() {
    final ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity entity = new ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStatusCodeEntity();
    entity.setSecureExchangeStatusCode("OPEN");
    entity.setDescription("Initial Review");
    entity.setDisplayOrder(1);
    entity.setEffectiveDate(LocalDateTime.now());
    entity.setLabel("Initial Review");
    entity.setCreateDate(LocalDateTime.now());
    entity.setCreateUser("TEST");
    entity.setUpdateUser("TEST");
    entity.setUpdateDate(LocalDateTime.now());
    entity.setExpiryDate(LocalDateTime.from(new GregorianCalendar(2099, Calendar.FEBRUARY, 1).toZonedDateTime()));
    return entity;
  }

  protected SecureExchangeCommentSagaData createSecureExchangeCommentSagaData(SecureExchangeComment secureExchangeComment, String mincode, String schoolName, String ministryTeamName, UUID secureExchangeID, String sequence) {
    SecureExchangeCommentSagaData secureExchangeCommentSagaData = new SecureExchangeCommentSagaData();
    secureExchangeCommentSagaData.setSecureExchangeComment(secureExchangeComment);
    secureExchangeCommentSagaData.setMincode(mincode);
    secureExchangeCommentSagaData.setSchoolName(schoolName);
    secureExchangeCommentSagaData.setMinistryTeamName(ministryTeamName);
    secureExchangeCommentSagaData.setSecureExchangeId(secureExchangeID);
    secureExchangeCommentSagaData.setSequenceNumber(sequence);
    return secureExchangeCommentSagaData;
  }


  protected SecureExchange getSecureExchangeEntityFromJsonString(String id) {
    try {
      SecureExchange secureExchange =  objectMapper.readValue(this.secureExchangeCreateJsonWithMinAndComment(id), SecureExchange.class);
      RequestUtil.setAuditColumnsForCreateIfBlank(secureExchange);
      return secureExchange;
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
