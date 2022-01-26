package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.Base64Mapper;
import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocument;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocumentMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, Base64Mapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SecureExchangeDocumentMapper {

  SecureExchangeDocumentMapper mapper = Mappers.getMapper(SecureExchangeDocumentMapper.class);

  SecureExchangeDocument toStructure(SecureExchangeDocumentEntity entity);

  @Mapping(target = "secureExchange", ignore = true)
  SecureExchangeDocumentEntity toModel(SecureExchangeDocument struct);

  SecureExchangeDocumentMetadata toMetadataStructure(SecureExchangeDocumentEntity entity);

  @Mapping(target = "digitalID", source = "secureExchange.digitalID")
  @Mapping(target = "secureExchangeID", source = "secureExchange.secureExchangeID")
  SecureExchangeDocumentMetadata toMetaData(SecureExchangeDocumentEntity entity);
}
