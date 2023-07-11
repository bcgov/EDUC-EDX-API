package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.Data;

@Data
public class SchoolWithoutActiveSecureExchangeUser {

  String schoolId;
  String schoolName;
  String mincode;
  String schoolCategory;
  String facilityType;
}
