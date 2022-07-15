package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SecureExchangeStudentRepository extends JpaRepository<SecureExchangeStudentEntity, UUID> {

}
