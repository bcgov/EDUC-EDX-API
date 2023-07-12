package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.Data;

@Data
public class CountSecureExchangeCreatedWithInstituteByMonth {
  String month;
  String year;
  Integer count;
}
