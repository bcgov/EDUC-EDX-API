package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeCommentEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeComment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SecureExchangeCommentMapper {
  SecureExchangeCommentMapper mapper = Mappers.getMapper(SecureExchangeCommentMapper.class);

  SecureExchangeComment toStructure(SecureExchangeCommentEntity entity);

  @Mapping(target = "secureExchangeEntity", ignore = true)
  SecureExchangeCommentEntity toModel(SecureExchangeComment structure);
}
