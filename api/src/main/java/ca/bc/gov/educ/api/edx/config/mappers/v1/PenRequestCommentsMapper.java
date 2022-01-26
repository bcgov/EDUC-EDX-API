package ca.bc.gov.educ.api.edx.config.mappers.v1;

import ca.bc.gov.educ.api.edx.config.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.config.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface PenRequestCommentsMapper {
  PenRequestCommentsMapper mapper = Mappers.getMapper(PenRequestCommentsMapper.class);

  SecureExchangeComments toStructure(SecureExchangeCommentEntity entity);

  @Mapping(target = "secureExchange", ignore = true)
  SecureExchangeCommentEntity toModel(SecureExchangeComments structure);
}
