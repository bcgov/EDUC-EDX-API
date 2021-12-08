package ca.bc.gov.educ.api.edx.filter;

import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.UUID;
import java.util.function.Function;

@Service
@Slf4j
public class PenRequestFilterSpecs {

  private final FilterSpecifications<PenRequestEntity, ChronoLocalDate> dateFilterSpecifications;
  private final FilterSpecifications<PenRequestEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications;
  private final FilterSpecifications<PenRequestEntity, Integer> integerFilterSpecifications;
  private final FilterSpecifications<PenRequestEntity, String> stringFilterSpecifications;
  private final FilterSpecifications<PenRequestEntity, Long> longFilterSpecifications;
  private final FilterSpecifications<PenRequestEntity, UUID> uuidFilterSpecifications;
  private final Converters converters;

  public PenRequestFilterSpecs(FilterSpecifications<PenRequestEntity, ChronoLocalDate> dateFilterSpecifications, FilterSpecifications<PenRequestEntity, ChronoLocalDateTime<?>> dateTimeFilterSpecifications, FilterSpecifications<PenRequestEntity, Integer> integerFilterSpecifications, FilterSpecifications<PenRequestEntity, String> stringFilterSpecifications, FilterSpecifications<PenRequestEntity, Long> longFilterSpecifications, FilterSpecifications<PenRequestEntity, UUID> uuidFilterSpecifications, Converters converters) {
    this.dateFilterSpecifications = dateFilterSpecifications;
    this.dateTimeFilterSpecifications = dateTimeFilterSpecifications;
    this.integerFilterSpecifications = integerFilterSpecifications;
    this.stringFilterSpecifications = stringFilterSpecifications;
    this.longFilterSpecifications = longFilterSpecifications;
    this.uuidFilterSpecifications = uuidFilterSpecifications;
    this.converters = converters;
  }

  public Specification<PenRequestEntity> getDateTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDate.class), dateFilterSpecifications);
  }

  public Specification<PenRequestEntity> getDateTimeTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(ChronoLocalDateTime.class), dateTimeFilterSpecifications);
  }

  public Specification<PenRequestEntity> getIntegerTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Integer.class), integerFilterSpecifications);
  }

  public Specification<PenRequestEntity> getLongTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(Long.class), longFilterSpecifications);
  }

  public Specification<PenRequestEntity> getStringTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(String.class), stringFilterSpecifications);
  }

  private <T extends Comparable<T>> Specification<PenRequestEntity> getSpecification(String fieldName,
                                                                                     String filterValue,
                                                                                     FilterOperation filterOperation,
                                                                                     Function<String, T> converter,
                                                                                     FilterSpecifications<PenRequestEntity, T> specifications) {
    FilterCriteria<T> criteria = new FilterCriteria<>(fieldName, filterValue, filterOperation, converter);
    return specifications.getSpecification(criteria.getOperation()).apply(criteria);
  }

  public Specification<PenRequestEntity> getUUIDTypeSpecification(String fieldName, String filterValue, FilterOperation filterOperation) {
    return getSpecification(fieldName, filterValue, filterOperation, converters.getFunction(UUID.class), uuidFilterSpecifications);
  }
}
