package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.validator.UUIDValidator;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.io.Serializable;

@EqualsAndHashCode
@Data
public class SecureExchangeStudent implements Serializable {

    @Null(message = "secureExchangeId should be null.")
    String secureExchangeId;
    @Null(message = "secureExchangeStudentId should be null.")
    String secureExchangeStudentId;
    @NotNull
    @UUIDValidator
    String studentId;
    @Size(max = 32)
    String createUser;
    @Null(message = "createDate should be null.")
    String createDate;

}
