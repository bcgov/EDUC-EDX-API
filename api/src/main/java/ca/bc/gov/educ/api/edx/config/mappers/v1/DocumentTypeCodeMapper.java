package ca.bc.gov.educ.api.edx.config.mappers.v1;

import ca.bc.gov.educ.api.edx.config.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentTypeCodeEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocumentTypeCode;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = UUIDMapper.class)
@SuppressWarnings("squid:S1214")
public interface DocumentTypeCodeMapper {

    DocumentTypeCodeMapper mapper = Mappers.getMapper(DocumentTypeCodeMapper.class);

    SecureExchangeDocumentTypeCode toStructure(SecureExchangeDocumentTypeCodeEntity entity);

    @Mapping(target = "updateUser", ignore = true)
    @Mapping(target = "updateDate", ignore = true)
    @Mapping(target = "createUser", ignore = true)
    @Mapping(target = "createDate", ignore = true)
    SecureExchangeDocumentTypeCodeEntity toModel(SecureExchangeDocumentTypeCode struct);

}
