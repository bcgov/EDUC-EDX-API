package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeContactTypeCodeEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeContactTypeCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
@SuppressWarnings("squid:S1214")
public interface SecureExchangeContactTypeCodeMapper {

    SecureExchangeContactTypeCodeMapper mapper = Mappers.getMapper(SecureExchangeContactTypeCodeMapper.class);

    SecureExchangeContactTypeCode toStructure(SecureExchangeContactTypeCodeEntity entity);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    SecureExchangeContactTypeCodeEntity toModel(SecureExchangeContactTypeCode struct);
}
