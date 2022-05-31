package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationRoleEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationRole;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface EdxActivatonRoleMapper {

  EdxActivatonRoleMapper mapper = Mappers.getMapper(EdxActivatonRoleMapper.class);

  @Mapping(target = "edxActivationCodeId", source = "edxActivationCodeEntity.edxActivationCodeId")
  EdxActivationRole toStructure(EdxActivationRoleEntity entity);

  @InheritInverseConfiguration
  EdxActivationRoleEntity toModel(EdxActivationRole model);
}
