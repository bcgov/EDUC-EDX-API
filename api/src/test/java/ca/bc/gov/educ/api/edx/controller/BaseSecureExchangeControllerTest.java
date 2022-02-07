package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.BaseSecureExchangeAPITest;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BaseSecureExchangeControllerTest extends BaseSecureExchangeAPITest {

  protected String dummySecureExchangeJson() {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"updateDate\":\"1952-10-31T00:00:00\",\"createDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\",\"pen\":\"127054021\"}";
  }

  protected String dummySecureExchangeNoCreateUpdateDateJson() {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\",\"pen\":\"127054021\"}";
  }

  protected String dummySecureExchangeNoCreateUpdateDateJsonWithMin(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\",\"pen\":\"127054021\"}";
  }

  protected String dummySecureExchangeNoCreateUpdateDateJsonWithMinAndComment(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\",\"pen\":\"127054021\", \"commentsList\": [{\"staffUserIdentifier\": \"TEST\", \"commentUserName\": \"JACKSON, JAMES\", \"content\": \"This is content\", \"updateUser\":\"TEST\",\"createUser\":\"TEST\"}]}";
  }

  protected String dummySecureExchangeJsonWithInvalidPenReqID() {
	  return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"0a004b01-7027-17b1-8170-27cb21100000\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"ministryOwnershipTeamID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"}";
  }

  protected String dummySecureExchangeJsonWithInvalidPenReqIDWithMinOwner(final String ministryOwnershipTeamID) {
    return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"0a004b01-7027-17b1-8170-27cb21100000\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"}";
  }

  protected String dummySecureExchangeJsonWithInvalidEmailVerifiedFlag() {
	  return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"updateDate\":\"1952-10-31T00:00:00\",\"createDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"0a004b01-7027-17b1-8170-27cb21100000\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"ministryOwnershipTeamID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"n\",\\\"pen\\\":\\\"123456789\\\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"}";
  }

  protected String dummySecureExchangeJsonWithValidPenReqID(final String penReqId, final String ministryOwnershipTeamID) {
	  return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"" + penReqId + "\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"ministryOwnershipTeamID\":\"" + ministryOwnershipTeamID + "\",\"legalFirstName\":\"OM\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\", \"secureExchangeStatusCode\":\"INITREV\", \"demogChanged\":\"Y\"}";
  }

  protected String dummySecureExchangeJsonWithInvalidDemogChanged(final String penReqId) {
	  return "{\"updateUser\":\"TEST\",\"createUser\":\"TEST\",\"secureExchangeStatusCode\":\"NEW\",\"isReadByExchangeContact\":\"N\",\"isReadByMinistry\":\"N\",\"statusUpdateDate\":\"1952-10-31T00:00:00\",\"updateDate\":\"1952-10-31T00:00:00\",\"createDate\":\"1952-10-31T00:00:00\",\"subject\":\"Hello Student\",\"secureExchangeID\":\"" + penReqId + "\",\"edxUserID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e4\",\"legalFirstName\":\"OM\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\", \"demogChanged\":\"E\"}";
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
