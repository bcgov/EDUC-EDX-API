package ca.bc.gov.educ.api.edx.repository;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;

import java.util.List;

public interface SecureExchangeRepositoryCustom {

  List<SecureExchangeEntity> findSecureExchange(final String contactIdentifier, final String secureExchangeContactTypeCode);
}
