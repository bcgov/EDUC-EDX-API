package ca.bc.gov.educ.api.edx.support;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;

public class SecureExchangeBuilder {
  SecureExchangeDocumentEntity document;

  UUID secureExchangeID = UUID.randomUUID();

  String contactIdentifier = UUID.randomUUID().toString();

  UUID ministryOwnershipTeamID = UUID.randomUUID();

  String secureExchangeStatusCode = "INITREV";

  String secureExchangeContactTypeCode = "SCHOOL";

  String createUser = "API";

  String isReadByMinistry = "N";

  String isReadByContact = "N";

  Date createDate = new Date();

  String updateUser = "API";

  String subject = "Hello student";

  Date updateDate = new Date();

  public SecureExchangeBuilder withDocument(SecureExchangeDocumentEntity document) {
    this.document = document;
    return this;
  }

  public SecureExchangeBuilder withSecureExchangeID(UUID secureExchangeID) {
    this.secureExchangeID = secureExchangeID;
    return this;
  }

  public SecureExchangeBuilder withoutSecureExchangeID() {
    this.secureExchangeID = null;
    return this;
  }

  public SecureExchangeBuilder withSecureExchangeStatusCode(String secureExchangeStatusCode) {
    this.secureExchangeStatusCode = secureExchangeStatusCode;
    return this;
  }

  public SecureExchangeBuilder withoutCreateAndUpdateUser() {
    this.createUser = null;
    this.createDate = null;
    this.updateUser = null;
    this.updateDate = null;
    return this;
  }

  public SecureExchangeEntity build() {
    SecureExchangeEntity secureExchange = new SecureExchangeEntity();
    secureExchange.setCreateUser(this.createUser);
    secureExchange.setCreateDate(LocalDateTime.now());
    secureExchange.setUpdateUser(this.updateUser);
    secureExchange.setUpdateDate(LocalDateTime.now());
    secureExchange.setMinistryOwnershipTeamID(this.ministryOwnershipTeamID);
    secureExchange.setIsReadByExchangeContact(this.isReadByContact);
    secureExchange.setIsReadByMinistry(this.isReadByMinistry);
    secureExchange.setStatusUpdateDate(LocalDateTime.now());

    secureExchange.setSecureExchangeID(this.secureExchangeID);
    secureExchange.setContactIdentifier(this.contactIdentifier);
    secureExchange.setSecureExchangeContactTypeCode(this.secureExchangeContactTypeCode);
    secureExchange.setSubject(this.subject);
    secureExchange.setSecureExchangeStatusCode(this.secureExchangeStatusCode);
    return secureExchange;
  }


}
