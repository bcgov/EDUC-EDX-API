package ca.bc.gov.educ.api.edx.struct.v1;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OnboardingFileUpload {
  @NotNull
  String createUser;
  @NotNull
  @ToString.Exclude
  String fileContents;
}
