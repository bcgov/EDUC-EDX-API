package ca.bc.gov.educ.api.edx.filter;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.function.Function;
@Service
public class FilterSpecifications<E, T extends Comparable<T>> {

	private EnumMap<FilterOperation, Function<FilterCriteria<T>, Specification<E>>> map;

	public FilterSpecifications() {
		initSpecifications();
	}

	public Function<FilterCriteria<T>, Specification<E>> getSpecification(FilterOperation operation) {
		return map.get(operation);
	}

	@PostConstruct
	public void initSpecifications() {

		map = new EnumMap<>(FilterOperation.class);

		// Equal
		map.put(FilterOperation.EQUAL, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> {
		  if(filterCriteria.getFieldName().contains(".")){
			String[] splits = filterCriteria.getFieldName().split("\\.");
			return criteriaBuilder.equal(root.join(splits[0]).get(splits[1]), filterCriteria.getConvertedSingleValue());
		  } else if(filterCriteria.getConvertedSingleValue() == null) {
			  return criteriaBuilder.isNull(root.get(filterCriteria.getFieldName()));
		  }
		  return criteriaBuilder.equal(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue());
		});

		map.put(FilterOperation.NOT_EQUAL, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
				.notEqual(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue()));

		map.put(FilterOperation.GREATER_THAN,
				filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThan(
						root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue()));

		map.put(FilterOperation.GREATER_THAN_OR_EQUAL_TO,
				filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(
						root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue()));

		map.put(FilterOperation.LESS_THAN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
				.lessThan(root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue()));

		map.put(FilterOperation.LESS_THAN_OR_EQUAL_TO,
				filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(
						root.get(filterCriteria.getFieldName()), filterCriteria.getConvertedSingleValue()));

		map.put(FilterOperation.IN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> root
				.get(filterCriteria.getFieldName()).in(filterCriteria.getConvertedValues()));

		map.put(FilterOperation.NOT_IN, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
				.not(root.get(filterCriteria.getFieldName()).in(filterCriteria.getConvertedSingleValue())));

		map.put(FilterOperation.BETWEEN,
				filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.between(
						root.get(filterCriteria.getFieldName()), filterCriteria.getMinValue(),
						filterCriteria.getMaxValue()));

		map.put(FilterOperation.CONTAINS, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
				.like(root.get(filterCriteria.getFieldName()), "%" + filterCriteria.getConvertedSingleValue() + "%"));

		map.put(FilterOperation.CONTAINS_IGNORE_CASE, filterCriteria -> (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
				.like(criteriaBuilder.lower(root.get(filterCriteria.getFieldName())), "%" + filterCriteria.getConvertedSingleValue().toString().toLowerCase() + "%"));

	}
}
