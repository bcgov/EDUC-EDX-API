package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseSecureExchangeControllerTest extends BaseSecureExchangeAPITest {

  protected String dummySecureExchangeJson() {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"updateDate\":\"1952-10-31T00:00:00\",\"createDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\"}";
  }

  protected String dummySecureExchangeNoCreateUpdateDateJson() {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\"}";
  }

  protected String dummySecureExchangeNoCreateUpdateDateJsonWithMin(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\"}";
  }

  protected String dummySecureExchangeNoCreateUpdateDateJsonWithMinAndStatusCode(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"isReadByMinistry\":\"true\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\",\"secureExchangeStatusCode\":\"INPROG\"}";
  }

  protected String dummySecureExchangeNoCreateUpdateDateJsonWithMinNoID(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\"}";
  }

  protected String dummySecureExchangeNoCreateUpdateDateJsonWithMinAndComment(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\", \"commentsList\": [{\"staffUserIdentifier\": \"TEST\", \"commentUserName\": \"JACKSON, JAMES\", \"content\": \"This is content\", \"updateUser\":\"TEST\",\"createUser\":\"TEST\"}]}";
  }

  protected String dummySecureExchangeJsonWithInvalidPenReqID() {
	  return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"0a004b01-7027-17b1-8170-27cb21100000\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\",\"ministryOwnershipTeamID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\"}";
  }

  protected String dummySecureExchangeJsonWithInvalidPenReqIDWithMinOwner(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"0a004b01-7027-17b1-8170-27cb21100000\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\"}";
  }

  protected String dummySecureExchangeJsonWithInvalidEmailVerifiedFlag() {
	  return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"updateDate\":\"1952-10-31T00:00:00\",\"createDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"0a004b01-7027-17b1-8170-27cb21100000\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\",\"ministryOwnershipTeamID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\"}";
  }

  protected String dummySecureExchangeJsonWithValidPenReqID(final String penReqId, final String ministryOwnershipTeamID) {
	  return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"" + penReqId + "\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\"}";
  }

  protected String dummySecureExchangeJsonWithInvalidDemogChanged(final String penReqId) {
	  return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"false\",\"isReadByMinistry\":\"false\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"updateDate\":\"1952-10-31T00:00:00\",\"createDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"" + penReqId + "\",\"contactIdentifier\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"secureExchangeContactTypeCode\":\"EDXUSER\"}";
  }

  protected SecureExchange getSecureExchangeEntityFromJsonString() {
    try {
      return new ObjectMapper().readValue(this.dummySecureExchangeJson(), SecureExchange.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected SecureExchange getSecureExchangeEntityFromJsonStringNoCreateUpdateDate() {
    try {
      return new ObjectMapper().readValue(this.dummySecureExchangeNoCreateUpdateDateJson(), SecureExchange.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
