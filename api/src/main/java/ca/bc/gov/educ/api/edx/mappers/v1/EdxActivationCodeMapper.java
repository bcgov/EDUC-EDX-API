package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxActivationCodeEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface EdxActivationCodeMapper {

  EdxActivationCodeMapper mapper = Mappers.getMapper(EdxActivationCodeMapper.class);

  @Mapping(target = "edxActivationRoles", source = "edxActivationRoleEntities")
  EdxActivationCode toStructure(EdxActivationCodeEntity entity);

  @InheritInverseConfiguration
  EdxActivationCodeEntity toModel(EdxActivationCode model);
}
