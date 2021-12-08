package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestCommentsEntity;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestComments;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface PenRequestCommentsMapper {
  PenRequestCommentsMapper mapper = Mappers.getMapper(PenRequestCommentsMapper.class);

  PenRequestComments toStructure(PenRequestCommentsEntity entity);

  @Mapping(target = "penRequestEntity", ignore = true)
  PenRequestCommentsEntity toModel(PenRequestComments structure);
}
