package ca.bc.gov.educ.api.edx.repository.impl;

import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.repository.EdxUserRepositoryCustom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class EdxUserRepositoryCustomImpl implements EdxUserRepositoryCustom {

  @Getter(AccessLevel.PRIVATE)
  private final EntityManager entityManager;

  @Autowired
  EdxUserRepositoryCustomImpl(final EntityManager em) {
    this.entityManager = em;
  }

  @Override
  public List<EdxUserEntity> findEdxUsers(final Optional<UUID> digitalId, final Optional<UUID> schoolID, final String firstName, final String lastName, final Optional<UUID> districtID) {
    final List<Predicate> predicates = new ArrayList<>();
    final CriteriaBuilder criteriaBuilder = getEntityManager().getCriteriaBuilder();
    final CriteriaQuery<EdxUserEntity> criteriaQuery = criteriaBuilder.createQuery(EdxUserEntity.class);
    Root<EdxUserEntity> edxUserEntityRoot = criteriaQuery.from(EdxUserEntity.class);

    digitalId.ifPresent(uuid -> predicates.add(criteriaBuilder.equal(edxUserEntityRoot.get("digitalIdentityID"), uuid)));
    districtID.ifPresent(uuid -> {
      Join<EdxUserEntity, EdxUserDistrictEntity> edxUserEntityDistrictJoin = edxUserEntityRoot.join("edxUserDistrictEntities");
      predicates.add(criteriaBuilder.equal(edxUserEntityDistrictJoin.get("districtID"), uuid));
    });
    if (schoolID.isPresent()) {
      Join<EdxUserEntity, EdxUserSchoolEntity> edxUserSchoolEntitySchoolJoin = edxUserEntityRoot.join("edxUserSchoolEntities");
      predicates.add(criteriaBuilder.equal(edxUserSchoolEntitySchoolJoin.get("schoolID"), schoolID.get()));
    }
    if (StringUtils.isNotBlank(firstName)) {
      predicates.add(criteriaBuilder.equal(edxUserEntityRoot.get("firstName"), firstName));
    }
    if (StringUtils.isNotBlank(lastName)) {
      predicates.add(criteriaBuilder.equal(edxUserEntityRoot.get("lastName"), lastName));
    }

    criteriaQuery.where(predicates.toArray(new Predicate[0]));

    return entityManager.createQuery(criteriaQuery).getResultList();
  }
}
