package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeNoteEntity;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeNote;

public abstract class SecureExchangeNoteDecorator implements SecureExchangeNoteMapper {
  private final SecureExchangeNoteMapper delegate;

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
