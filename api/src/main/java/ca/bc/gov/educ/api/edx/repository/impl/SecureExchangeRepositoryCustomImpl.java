package ca.bc.gov.educ.api.edx.repository.impl;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRepositoryCustom;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class SecureExchangeRepositoryCustomImpl implements SecureExchangeRepositoryCustom {

  @Getter(AccessLevel.PRIVATE)
  private final EntityManager entityManager;

  @Autowired
  SecureExchangeRepositoryCustomImpl(final EntityManager em) {
    this.entityManager = em;
  }

  @Override
  public List<SecureExchangeEntity> findSecureExchange(final UUID edxUserSchoolID, final UUID edxUserDistrictID, final UUID ministryOwnershipTeamID, final UUID ministryContactTeamID, final UUID edxUserID, final String status) {
    final List<Predicate> predicates = new ArrayList<>();
    final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
    final CriteriaQuery<SecureExchangeEntity> criteriaQuery = criteriaBuilder.createQuery(SecureExchangeEntity.class);
    Root<SecureExchangeEntity> secureExchangeEntityRoot = criteriaQuery.from(SecureExchangeEntity.class);
    if (StringUtils.isNotBlank(status)) {
      predicates.add(criteriaBuilder.equal(secureExchangeEntityRoot.get("secureExchangeStatusCode"), status));
    }
    if (edxUserSchoolID != null) {
      predicates.add(criteriaBuilder.equal(secureExchangeEntityRoot.get("edxUserSchoolID"), edxUserSchoolID));
    }
    if (edxUserDistrictID != null) {
      predicates.add(criteriaBuilder.equal(secureExchangeEntityRoot.get("edxUserDistrictID"), edxUserDistrictID));
    }
    if (ministryOwnershipTeamID != null) {
      predicates.add(criteriaBuilder.equal(secureExchangeEntityRoot.get("ministryOwnershipTeamID"), ministryOwnershipTeamID));
    }
    if (ministryContactTeamID != null) {
      predicates.add(criteriaBuilder.equal(secureExchangeEntityRoot.get("ministryContactTeamID"), ministryContactTeamID));
    }
    if (edxUserID != null) {
      predicates.add(criteriaBuilder.equal(secureExchangeEntityRoot.get("edxUserID"), edxUserID));
    }
    criteriaQuery.where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(criteriaQuery).getResultList();
  }
}
