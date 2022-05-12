package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxRoleEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, EdxRolePermissionMapper.class})
@SuppressWarnings("squid:S1214")
public interface EdxRoleMapper {

  EdxRoleMapper mapper = Mappers.getMapper(EdxRoleMapper.class);

  @Mapping(target = "edxRolePermissions", source = "edxRolePermissionEntities")
  EdxRole toStructure(EdxRoleEntity entity);


  @Mapping(source = "edxRolePermissions", target = "edxRolePermissionEntities")
  EdxRoleEntity toModel(EdxRole edxRole);
}
