package ca.bc.gov.educ.api.edx.struct.gradschool.v1;

import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class GradSchool extends BaseRequest implements Serializable {

  private static final long serialVersionUID = 1L;

  private String gradSchoolID;

  @NotNull
  private String schoolID;

  @NotNull
  private String submissionModeCode;

  @NotNull
  private String canIssueTranscripts;

  @NotNull
  private String canIssueCertificates;

}
