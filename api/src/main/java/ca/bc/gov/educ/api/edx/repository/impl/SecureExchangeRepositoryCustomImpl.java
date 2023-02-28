package ca.bc.gov.educ.api.edx.repository.impl;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRepositoryCustom;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
public class SecureExchangeRepositoryCustomImpl implements SecureExchangeRepositoryCustom {

  @Getter(AccessLevel.PRIVATE)
  private final EntityManager entityManager;

  @Autowired
  SecureExchangeRepositoryCustomImpl(final EntityManager em) {
    this.entityManager = em;
  }

  @Override
  public List<SecureExchangeEntity> findSecureExchange(final String contactIdentifier, final String secureExchangeContactTypeCode) {
    final List<Predicate> predicates = new ArrayList<>();
    final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
    final CriteriaQuery<SecureExchangeEntity> criteriaQuery = criteriaBuilder.createQuery(SecureExchangeEntity.class);
    Root<SecureExchangeEntity> secureExchangeEntityRoot = criteriaQuery.from(SecureExchangeEntity.class);

    if (StringUtils.isNotBlank(contactIdentifier)) {
      predicates.add(criteriaBuilder.equal(secureExchangeEntityRoot.get("contactIdentifier"), contactIdentifier));
    }
    if (StringUtils.isNotBlank(secureExchangeContactTypeCode)) {
      predicates.add(criteriaBuilder.equal(secureExchangeEntityRoot.get("secureExchangeContactTypeCode"), secureExchangeContactTypeCode));
    }

    criteriaQuery.where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(criteriaQuery).getResultList();
  }
}
