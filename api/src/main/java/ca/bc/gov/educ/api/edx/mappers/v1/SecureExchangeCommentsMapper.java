package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComments;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SecureExchangeCommentsMapper {
  SecureExchangeCommentsMapper mapper = Mappers.getMapper(SecureExchangeCommentsMapper.class);

  SecureExchangeComments toStructure(SecureExchangeCommentEntity entity);

  @Mapping(target = "secureExchangeEntity", ignore = true)
  SecureExchangeCommentEntity toModel(SecureExchangeComments structure);

  @Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
  @SuppressWarnings("squid:S1214")
  interface SecureExchangeEntityMapper {

    SecureExchangeEntityMapper mapper = Mappers.getMapper(SecureExchangeEntityMapper.class);

    SecureExchange toStructure(SecureExchangeEntity entity);

    @Mapping(target = "secureExchangeComment", ignore = true)
    SecureExchangeEntity toModel(SecureExchange struct);
  }
}
