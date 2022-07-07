package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecureExchangeStudentRepository extends JpaRepository<SecureExchangeStudent, String> {
}
