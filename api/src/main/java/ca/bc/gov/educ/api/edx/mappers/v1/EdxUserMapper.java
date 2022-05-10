package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUser;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class, EdxUserSchoolMapper.class, EdxUserDistrictMapper.class})
@SuppressWarnings("squid:S1214")
public interface EdxUserMapper {

  EdxUserMapper mapper = Mappers.getMapper(EdxUserMapper.class);

  @Mapping(target = "edxUserSchools", source = "edxUserSchoolEntities")
  @Mapping(target = "edxUserDistricts", source = "edxUserDistrictEntities")
  EdxUser toStructure(EdxUserEntity entity);

  @Mapping(source = "edxUserSchools", target = "edxUserSchoolEntities")
  @Mapping(source = "edxUserDistricts", target = "edxUserDistrictEntities")
  EdxUserEntity toModel(EdxUser edxUser);
}
