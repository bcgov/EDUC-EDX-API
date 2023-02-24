package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SecureExchangeDocMetadata implements Serializable {

  private static final long serialVersionUID = -7471585921119777006L;

  private String documentID;

  @NotNull(message = "documentTypeCode cannot be null")
  private String documentTypeCode;

  @NotNull(message = "fileName cannot be null")
  private String fileName;

  @NotNull(message = "fileExtension cannot be null")
  private String fileExtension;

  @NotNull(message = "fileSize cannot be null")
  private Integer fileSize;

  private String edxUserID;

  private String staffUserIdentifier;

  @Null(message = "Create Date Should be null")
  private String createDate;
}
