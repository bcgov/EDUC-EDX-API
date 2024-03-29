package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@SuppressWarnings("squid:S1700")
public class SecureExchangeStatusCode implements Serializable {

  private static final long serialVersionUID = -8596549361135591976L;

  String secureExchangeStatusCode;

  @NotNull(message = "label cannot be null")
  String label;

  @NotNull(message = "description cannot be null")
  String description;

  @NotNull(message = "displayOrder cannot be null")
  Integer displayOrder;

  @NotNull(message = "effectiveDate cannot be null")
  String effectiveDate;

  @NotNull(message = "expiryDate cannot be null")
  String expiryDate;
}
