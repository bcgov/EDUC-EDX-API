package ca.bc.gov.educ.api.edx.model.v1;

import java.time.LocalDate;

public interface GmpStats {
  LocalDate getStatusUpdateDate();
  String getStatus();
  double getAverageCompletionTime();
}
