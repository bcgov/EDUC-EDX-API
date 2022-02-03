package ca.bc.gov.educ.api.edx.config.mappers.v1;

import ca.bc.gov.educ.api.edx.config.mappers.Base64Mapper;
import ca.bc.gov.educ.api.edx.config.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.config.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocMetadata;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocument;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeDocumentMetadata;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, Base64Mapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface DocumentMapper {

  DocumentMapper mapper = Mappers.getMapper(DocumentMapper.class);

  SecureExchangeDocument toStructure(SecureExchangeDocumentEntity entity);

  @Mapping(target = "secureExchange", ignore = true)
  SecureExchangeDocumentEntity toModel(SecureExchangeDocument struct);

  SecureExchangeDocMetadata toMetadataStructure(SecureExchangeDocumentEntity entity);

  @Mapping(target = "edxUserID", source = "secureExchange.edxUserID")
  @Mapping(target = "secureExchangeID", source = "secureExchange.secureExchangeID")
  SecureExchangeDocumentMetadata toMetaData(SecureExchangeDocumentEntity entity);
}
