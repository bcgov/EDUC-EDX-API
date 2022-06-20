package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEventStatesEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SagaEventStates;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SagaEventStatesMapper {

  /**
   * The constant mapper.
   */
  SagaEventStatesMapper mapper = Mappers.getMapper(SagaEventStatesMapper.class);

  SagaEventStatesEntity toModel(SagaEventStates saga);

  @InheritInverseConfiguration
  SagaEventStates toStructure (SagaEventStatesEntity entity);


}
