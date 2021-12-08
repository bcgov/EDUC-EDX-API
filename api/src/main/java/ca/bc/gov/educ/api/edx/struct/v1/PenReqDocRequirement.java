package ca.bc.gov.educ.api.edx.struct.v1;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PenReqDocRequirement implements Serializable {

  private static final long serialVersionUID = 1290327040864105148L;

  private int maxSize;

  private List<String> extensions;

}
