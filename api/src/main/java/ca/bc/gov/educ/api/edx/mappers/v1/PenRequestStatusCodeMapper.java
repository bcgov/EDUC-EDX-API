package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestStatusCodeEntity;
import ca.bc.gov.educ.api.edx.struct.v1.PenRequestStatusCode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
@SuppressWarnings("squid:S1214")
public interface PenRequestStatusCodeMapper {


    PenRequestStatusCodeMapper mapper = Mappers.getMapper(PenRequestStatusCodeMapper.class);

    PenRequestStatusCode toStructure(PenRequestStatusCodeEntity entity);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    PenRequestStatusCodeEntity toModel(PenRequestStatusCode struct);
}
