package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreate;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@DecoratedWith(SecureExchangeEntityDecorator.class)
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SecureExchangeEntityMapper {

  SecureExchangeEntityMapper mapper = Mappers.getMapper(SecureExchangeEntityMapper.class);

  SecureExchange toStructure(SecureExchangeEntity entity);

  @Mapping(target = "secureExchangeComment", ignore = true)
  SecureExchangeEntity toModel(SecureExchange struct);

  @Mapping(target = "secureExchangeComment", ignore = true)
  @Mapping(target = "secureExchangeDocument", ignore = true)
  SecureExchangeEntity toModel(SecureExchangeCreate struct);
}
