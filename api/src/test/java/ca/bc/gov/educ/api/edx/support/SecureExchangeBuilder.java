package ca.bc.gov.educ.api.edx.support;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;

public class SecureExchangeBuilder {
  SecureExchangeDocumentEntity document;

  UUID secureExchangeID = UUID.randomUUID();

  UUID digitalID = UUID.randomUUID();

  UUID ministryOwnershipTeamID = UUID.randomUUID();

  String secureExchangeStatusCode = "INITREV";

  String legalFirstName = "Tom";

  String legalMiddleNames;

  String legalLastName = "Wayen";

  String createUser = "API";

  Date createDate = new Date();

  String updateUser = "API";

  String emailVerified = "N";

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

  public SecureExchangeBuilder withDigitalID(UUID digitalID) {
    this.digitalID = digitalID;
    return this;
  }

  public SecureExchangeBuilder withSecureExchangeStatusCode(String secureExchangeStatusCode) {
    this.secureExchangeStatusCode = secureExchangeStatusCode;
    return this;
  }

  public SecureExchangeBuilder withLegalFirstName(String legalFirstName) {
    this.legalFirstName = legalFirstName;
    return this;
  }

  public SecureExchangeBuilder withLegalLastNamee(String legalLastName) {
    this.legalLastName = legalLastName;
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

    secureExchange.setSecureExchangeID(this.secureExchangeID);
    secureExchange.setDigitalID(this.digitalID);
    secureExchange.setSecureExchangeStatusCode(this.secureExchangeStatusCode);
    //secureExchange.setLegalFirstName(this.legalFirstName);
   //secureExchange.setLegalLastName(this.legalLastName);
    //secureExchange.setEmailVerified(this.emailVerified);
    return secureExchange;
  }


}
