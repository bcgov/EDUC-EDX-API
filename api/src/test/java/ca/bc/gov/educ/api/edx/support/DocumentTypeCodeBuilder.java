package ca.bc.gov.educ.api.edx.support;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentTypeCodeEntity;
import ca.bc.gov.educ.api.edx.repository.DocumentTypeCodeTableRepository;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class DocumentTypeCodeBuilder {

  String documentTypeCode;

  String label = "label";

  String description = "description";

  Integer displayOrder = 1;

  LocalDateTime effectiveDate = LocalDateTime.now();

  LocalDateTime expiryDate = LocalDateTime.from(new GregorianCalendar(2099, Calendar.FEBRUARY, 1).toZonedDateTime());

  String createUser = "API";

  LocalDateTime createDate = LocalDateTime.now();

  String updateUser = "API";

  LocalDateTime updateDate = LocalDateTime.now();

  public DocumentTypeCodeBuilder withDocumentTypeCode(String documentTypeCode) {
    this.documentTypeCode = documentTypeCode;
    return this;
  }

  public DocumentTypeCodeBuilder withLabel(String label) {
    this.label = label;
    return this;
  }

  public DocumentTypeCodeBuilder withoutCreateAndUpdateUser() {
    this.createUser = null;
    this.createDate = null;
    this.updateUser = null;
    this.updateDate = null;
    return this;
  }

  public SecureExchangeDocumentTypeCodeEntity build() {
    SecureExchangeDocumentTypeCodeEntity typeCode = new SecureExchangeDocumentTypeCodeEntity();
    typeCode.setCreateUser(this.createUser);
    typeCode.setCreateDate(this.createDate);
    typeCode.setUpdateUser(this.updateUser);
    typeCode.setUpdateDate(this.updateDate);

    typeCode.setSecureExchangeDocumentTypeCode(this.documentTypeCode);
    typeCode.setLabel(this.label);
    typeCode.setDescription(this.description);
    typeCode.setDisplayOrder(this.displayOrder);
    typeCode.setEffectiveDate(this.effectiveDate);
    typeCode.setExpiryDate(this.expiryDate);

    return typeCode;
  }

  public static void setUpDocumentTypeCodes(DocumentTypeCodeTableRepository documentTypeCodeRepository) {
    SecureExchangeDocumentTypeCodeEntity passport = new DocumentTypeCodeBuilder()
            .withDocumentTypeCode("CAPASSPORT").build();
    SecureExchangeDocumentTypeCodeEntity bcsc = new DocumentTypeCodeBuilder()
            .withDocumentTypeCode("BCSCPHOTO").build();
    SecureExchangeDocumentTypeCodeEntity bCeIdPHOTONotEffective = new DocumentTypeCodeBuilder()
            .withDocumentTypeCode("BCeIdPHOTO").build();
    bCeIdPHOTONotEffective.setEffectiveDate(LocalDateTime.from(new GregorianCalendar(2199, Calendar.FEBRUARY, 1).toZonedDateTime()));
    SecureExchangeDocumentTypeCodeEntity dlExpired = new DocumentTypeCodeBuilder()
            .withDocumentTypeCode("dl").build();
    dlExpired.setExpiryDate(LocalDateTime.from(new GregorianCalendar(2020, Calendar.FEBRUARY, 1).toZonedDateTime()));
    documentTypeCodeRepository.save(passport);
    documentTypeCodeRepository.save(bcsc);
    documentTypeCodeRepository.save(bCeIdPHOTONotEffective);
    documentTypeCodeRepository.save(dlExpired);
  }

}
