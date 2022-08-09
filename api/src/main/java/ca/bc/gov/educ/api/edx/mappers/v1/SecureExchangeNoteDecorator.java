package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.Base64Mapper;
import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeNoteEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeNote;

public abstract class SecureExchangeNoteDecorator implements SecureExchangeNoteMapper {
  private final SecureExchangeNoteMapper delegate;
  private final UUIDMapper uUIDMapper = new UUIDMapper();
  private final LocalDateTimeMapper localDateTimeMapper = new LocalDateTimeMapper();
  private final Base64Mapper base64Mapper = new Base64Mapper();

  protected SecureExchangeNoteDecorator(final SecureExchangeNoteMapper delegate) {
    this.delegate = delegate;
  }

  @Override
  public SecureExchangeNote toStructure(SecureExchangeNoteEntity entity) {
    SecureExchangeNote secureExchangeNote = this.delegate.toStructure(entity);
    SecureExchangeEntity secureExchange =  entity.getSecureExchangeEntity();
    if(secureExchange != null){
      secureExchangeNote.setSecureExchangeID(secureExchange.getSecureExchangeID().toString());
    }
    return secureExchangeNote;
  }

}
