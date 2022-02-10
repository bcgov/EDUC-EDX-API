package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.EdxUserSchoolEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserSchool;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface EdxUserSchoolMapper {

  EdxUserSchoolMapper mapper = Mappers.getMapper(EdxUserSchoolMapper.class);

  EdxUserSchool toStructure(EdxUserSchoolEntity entity);
}
