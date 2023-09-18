package ca.bc.gov.educ.api.edx.struct.v1;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreateSchoolSagaData extends School {
  private Optional<EdxUser> initialEdxUser;
}
