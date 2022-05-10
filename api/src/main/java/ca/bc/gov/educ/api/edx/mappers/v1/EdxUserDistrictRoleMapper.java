package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictRoleEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserDistrictRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, EdxRoleMapper.class})
@SuppressWarnings("squid:S1214")
public interface EdxUserDistrictRoleMapper {

  EdxUserDistrictRoleMapper mapper = Mappers.getMapper(EdxUserDistrictRoleMapper.class);

  @Mapping(target = "edxRole", source = "edxRoleEntity")
  EdxUserDistrictRole toStructure(EdxUserDistrictRoleEntity entity);

  @Mapping(source = "edxRole", target = "edxRoleEntity")
  EdxUserDistrictRoleEntity toModel(EdxUserDistrictRole edxUserDistrictRole);
}
