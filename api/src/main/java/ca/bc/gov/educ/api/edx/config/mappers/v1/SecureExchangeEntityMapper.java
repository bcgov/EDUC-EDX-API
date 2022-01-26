package ca.bc.gov.educ.api.edx.config.mappers.v1;

import ca.bc.gov.educ.api.edx.config.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.config.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SecureExchangeEntityMapper {

  SecureExchangeEntityMapper mapper = Mappers.getMapper(SecureExchangeEntityMapper.class);

  @Mapping(target = "createDate", ignore = true)
  @Mapping(target = "updateDate", ignore = true)
  SecureExchange toStructure(SecureExchangeEntity entity);

  @Mapping(target = "secureExchangeComment", ignore = true)
  SecureExchangeEntity toModel(SecureExchange struct);
}
