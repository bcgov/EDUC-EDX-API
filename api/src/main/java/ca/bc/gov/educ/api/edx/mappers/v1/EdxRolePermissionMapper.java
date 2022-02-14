package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxRolePermissionEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxRolePermission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, EdxPermissionMapper.class})
@SuppressWarnings("squid:S1214")
public interface EdxRolePermissionMapper {

  EdxRolePermissionMapper mapper = Mappers.getMapper(EdxRolePermissionMapper.class);

  @Mapping(target = "edxPermission", source = "edxPermissionEntity")
  EdxRolePermission toStructure(EdxRolePermissionEntity entity);
}
