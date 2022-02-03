package ca.bc.gov.educ.api.edx.support;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;

public class SecureExchangeBuilder {
  SecureExchangeDocumentEntity document;

  UUID secureExchangeID = UUID.randomUUID();

  UUID edxUserID = UUID.randomUUID();

  UUID ministryOwnershipTeamID = UUID.randomUUID();

  String secureExchangeStatusCode = "INITREV";

  String createUser = "API";

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

  public SecureExchangeBuilder withEdxUserID(UUID edxUserID) {
    this.edxUserID = edxUserID;
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

    secureExchange.setSecureExchangeID(this.secureExchangeID);
    secureExchange.setEdxUserID(this.edxUserID);
    secureExchange.setSubject(this.subject);
    secureExchange.setSecureExchangeStatusCode(this.secureExchangeStatusCode);
    return secureExchange;
  }


}
