package ca.bc.gov.educ.api.edx.filter;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class SecureExchangeFilterSpecs {

  private final FilterSpecifications<SecureExchangeEntity, ChronoLocalDate> dateFilterSpecifications;
  private final FilterSpecifications<SecureExchangeEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications;
  private final FilterSpecifications<SecureExchangeEntity, Integer> integerFilterSpecifications;
  private final FilterSpecifications<SecureExchangeEntity, String> stringFilterSpecifications;
  private final FilterSpecifications<SecureExchangeEntity, Long> longFilterSpecifications;
  private final FilterSpecifications<SecureExchangeEntity, UUID> uuidFilterSpecifications;
  private final Converters converters;

  public SecureExchangeFilterSpecs(FilterSpecifications<SecureExchangeEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<SecureExchangeEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<SecureExchangeEntity, Integer> integerFilterSpecifications, FilterSpecifications<SecureExchangeEntity, String> stringFilterSpecifications, FilterSpecifications<SecureExchangeEntity, Long> longFilterSpecifications, FilterSpecifications<SecureExchangeEntity, UUID> uuidFilterSpecifications, Converters converters) {
    this.dateFilterSpecifications = dateFilterSpecifications;
    this.dateTimeFilterSpecifications = dateTimeFilterSpecifications;
    this.integerFilterSpecifications = integerFilterSpecifications;
    this.stringFilterSpecifications = stringFilterSpecifications;
    this.longFilterSpecifications = longFilterSpecifications;
    this.uuidFilterSpecifications = uuidFilterSpecifications;
    this.converters = converters;
  }

  public Specification<SecureExchangeEntity> getDateTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDate.class), dateFilterSpecifications);
  }

  public Specification<SecureExchangeEntity> getDateTimeTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDateTime.class), dateTimeFilterSpecifications);
  }

  public Specification<SecureExchangeEntity> getIntegerTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Integer.class), integerFilterSpecifications);
  }

  public Specification<SecureExchangeEntity> getLongTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Long.class), longFilterSpecifications);
  }

  public Specification<SecureExchangeEntity> getStringTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(String.class), stringFilterSpecifications);
  }

  private <T extends Comparable<T>> Specification<SecureExchangeEntity> getSpecification(String fieldName,
                                                                                   String filterValue,
                                                                                   FilterOperation filterOperation,
                                                                                   Function<String, T> converter,
                                                                                   FilterSpecifications<SecureExchangeEntity, T> specifications) {
    FilterCriteria<T> criteria = new FilterCriteria<>(fieldName, filterValue, filterOperation, converter);
    return specifications.getSpecification(criteria.getOperation()).apply(criteria);
  }

  public Specification<SecureExchangeEntity> getUUIDTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(UUID.class), uuidFilterSpecifications);
  }
}
