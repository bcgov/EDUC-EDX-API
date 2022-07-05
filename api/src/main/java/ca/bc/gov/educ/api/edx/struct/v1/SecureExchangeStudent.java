package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode
@Data
public class SecureExchangeStudent implements Serializable {

    UUID secureExchangeStudentId;
    @NotNull
    String studentId;
    @Size(max = 32)
    String createUser;
    @Null(message = "createDate should be null.")
    LocalDateTime createDate;

}
