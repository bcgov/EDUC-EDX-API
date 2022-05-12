package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolRoleEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchoolRole;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, EdxRoleMapper.class})
@SuppressWarnings("squid:S1214")
public interface EdxUserSchoolRoleMapper {

  EdxUserSchoolRoleMapper mapper = Mappers.getMapper(EdxUserSchoolRoleMapper.class);

  @Mapping(target = "edxRole", source = "edxRoleEntity")
  @Mapping(target = "edxUserSchoolID", source = "edxUserSchoolEntity.edxUserSchoolID")
  EdxUserSchoolRole toStructure(EdxUserSchoolRoleEntity entity);



  @InheritInverseConfiguration
  EdxUserSchoolRoleEntity toModel(EdxUserSchoolRole edxUserSchoolRole);
}
