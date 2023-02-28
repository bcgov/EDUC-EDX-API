package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import ca.bc.gov.educ.api.edx.validator.UUIDValidator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode
@Data
public class SecureExchangeStudent extends BaseRequest implements Serializable {
    @Null(message = "secureExchangeId should be null.")
    String secureExchangeId;
    @Null(message = "secureExchangeStudentId should be null.")
    String secureExchangeStudentId;
    @Size(max = 32)
    String staffUserIdentifier;
    String edxUserID;
    @NotNull
    @UUIDValidator
    String studentId;
}
