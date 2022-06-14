package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.struct.v1.Saga;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * The interface Saga mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SagaMapper {
  /**
   * The constant mapper.
   */
  SagaMapper mapper = Mappers.getMapper(SagaMapper.class);

  SagaEntity toModel(Saga saga);

  @InheritInverseConfiguration
  Saga toStructure(SagaEntity entity);
}
