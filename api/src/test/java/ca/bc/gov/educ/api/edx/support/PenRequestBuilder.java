package ca.bc.gov.educ.api.edx.support;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import ca.bc.gov.educ.api.edx.model.v1.DocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;

public class PenRequestBuilder {
  DocumentEntity document;

  UUID penRequestID = UUID.randomUUID();

  UUID digitalID = UUID.randomUUID();

  String penRequestStatusCode = "INITREV";

  String legalFirstName = "Tom";

  String legalMiddleNames;

  String legalLastName = "Wayen";

  String createUser = "API";

  Date createDate = new Date();

  String updateUser = "API";

  String emailVerified = "N";

  Date updateDate = new Date();

  public PenRequestBuilder withDocument(DocumentEntity document) {
    this.document = document;
    return this;
  }

  public PenRequestBuilder withPenRequestID(UUID penRequestID) {
    this.penRequestID = penRequestID;
    return this;
  }

  public PenRequestBuilder withoutPenRequestID() {
    this.penRequestID = null;
    return this;
  }

  public PenRequestBuilder withDigitalID(UUID digitalID) {
    this.digitalID = digitalID;
    return this;
  }

  public PenRequestBuilder withPenRequestStatusCode(String penRequestStatusCode) {
    this.penRequestStatusCode = penRequestStatusCode;
    return this;
  }

  public PenRequestBuilder withLegalFirstName(String legalFirstName) {
    this.legalFirstName = legalFirstName;
    return this;
  }

  public PenRequestBuilder withLegalLastNamee(String legalLastName) {
    this.legalLastName = legalLastName;
    return this;
  }

  public PenRequestBuilder withoutCreateAndUpdateUser() {
    this.createUser = null;
    this.createDate = null;
    this.updateUser = null;
    this.updateDate = null;
    return this;
  }

  public PenRequestEntity build() {
    PenRequestEntity penRequest = new PenRequestEntity();
    penRequest.setCreateUser(this.createUser);
    penRequest.setCreateDate(LocalDateTime.now());
    penRequest.setUpdateUser(this.updateUser);
    penRequest.setUpdateDate(LocalDateTime.now());

    penRequest.setPenRequestID(this.penRequestID);
    penRequest.setDigitalID(this.digitalID);
    penRequest.setPenRequestStatusCode(this.penRequestStatusCode);
    penRequest.setLegalFirstName(this.legalFirstName);
    penRequest.setLegalLastName(this.legalLastName);
    penRequest.setEmailVerified(this.emailVerified);
    return penRequest;
  }


}
