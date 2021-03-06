package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The type Saga.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Saga {
  private String sagaId;
  private String sagaName;
  private String sagaState;
  private String payload;
  private String status;
  private Boolean sagaCompensated;
  private String mincode;
  private String emailId;
  private String edxUserId;
  private String secureExchangeId;
  private Integer retryCount;
  String createUser;
  String updateUser;
  String createDate;
  String updateDate;
}
