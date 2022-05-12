package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserDistrictEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserDistrict;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, EdxUserDistrictRoleMapper.class})
@SuppressWarnings("squid:S1214")
public interface EdxUserDistrictMapper {

  EdxUserDistrictMapper mapper = Mappers.getMapper(EdxUserDistrictMapper.class);

  @Mapping(target = "edxUserDistrictRoles", source = "edxUserDistrictRoleEntities")
  EdxUserDistrict toStructure(EdxUserDistrictEntity entity);


  @Mapping(source = "edxUserDistrictRoles", target = "edxUserDistrictRoleEntities")
  EdxUserDistrictEntity toModel(EdxUserDistrict edxUserDistrict);

}
