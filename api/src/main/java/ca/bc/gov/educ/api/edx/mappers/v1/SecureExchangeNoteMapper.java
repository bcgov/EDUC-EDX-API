package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeNoteEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeNote;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@DecoratedWith(SecureExchangeNoteDecorator.class)
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface SecureExchangeNoteMapper {

    SecureExchangeNoteMapper mapper = Mappers.getMapper(SecureExchangeNoteMapper.class);

    SecureExchangeNote toStructure(SecureExchangeNoteEntity entity);

    @Mapping(target = "secureExchangeEntity", ignore = true)
    SecureExchangeNoteEntity toModel(SecureExchangeNote structure);

}
