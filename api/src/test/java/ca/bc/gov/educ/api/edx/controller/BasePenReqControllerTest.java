package ca.bc.gov.educ.api.edx.controller;

import ca.bc.gov.educ.api.edx.BasePenRequestAPITest;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import com.fasterxml.jackson.databind.ObjectMapper;

public abstract class BasePenReqControllerTest extends BasePenRequestAPITest {

  protected String dummyPenRequestJson() {
    return "{\"digitalID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\",\"pen\":\"127054021\"}";
  }

  protected String dummyPenRequestJsonWithInitialSubmitDate() {
    return "{\"digitalID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"initialSubmitDate\":\"1952-10-31T00:00:00\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"}";
  }

  protected String dummyPenRequestJsonWithInvalidPenReqID() {
	  return "{\"penRequestID\":\"0a004b01-7027-17b1-8170-27cb21100000\",\"digitalID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"initialSubmitDate\":\"1952-10-31T00:00:00\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"}";
  }

  protected String dummyPenRequestJsonWithInvalidEmailVerifiedFlag() {
	  return "{\"penRequestID\":\"0a004b01-7027-17b1-8170-27cb21100000\",\"digitalID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"Chester\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"initialSubmitDate\":\"1952-10-31T00:00:00\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"n\",\\\"pen\\\":\\\"123456789\\\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\"}";
  }

  protected String dummyPenRequestJsonWithValidPenReqID(final String penReqId) {
	  return "{\"penRequestID\":\"" + penReqId + "\",\"digitalID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"OM\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"initialSubmitDate\":\"1952-10-31T00:00:00\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\", \"penRequestStatusCode\":\"INITREV\", \"demogChanged\":\"Y\"}";
  }

  protected String dummyPenRequestJsonWithInvalidDemogChanged(final String penReqId) {
	  return "{\"penRequestID\":\"" + penReqId + "\",\"digitalID\":\"b1e0788a-7dab-4b92-af86-c678e411f1e3\",\"legalFirstName\":\"OM\",\"legalMiddleNames\":\"Grestie\",\"legalLastName\":\"Baulk\",\"dob\":\"1952-10-31\",\"initialSubmitDate\":\"1952-10-31T00:00:00\",\"genderCode\":\"M\",\"email\":\"cbaulk0@bluehost.com\",\"emailVerified\":\"N\",\"currentSchool\":\"Xanthoparmelia wyomingica (Gyel.) Hale\", \"demogChanged\":\"E\"}";
  }

  protected SecureExchange getPenRequestEntityFromJsonString() {
    try {
      return new ObjectMapper().readValue(this.dummyPenRequestJson(), SecureExchange.class);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }
}
