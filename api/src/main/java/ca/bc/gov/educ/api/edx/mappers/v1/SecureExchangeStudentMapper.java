package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface SecureExchangeStudentMapper {

    SecureExchangeStudentMapper mapper = Mappers.getMapper(SecureExchangeStudentMapper.class);

    SecureExchangeStudent toStructure(SecureExchangeStudentEntity entity);

    @Mapping(target = "secureExchangeEntity", ignore = true)
    SecureExchangeStudentEntity toModel(SecureExchangeStudent structure);

}
