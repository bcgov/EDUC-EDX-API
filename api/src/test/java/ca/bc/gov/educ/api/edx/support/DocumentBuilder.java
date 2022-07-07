package ca.bc.gov.educ.api.edx.support;

import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeDocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

public class DocumentBuilder {
    UUID documentID = UUID.randomUUID();

    String documentTypeCode = "BCSCPHOTO";

    String fileName = "card";

    String fileExtension = "jpg";

    int fileSize = 8;

    SecureExchangeEntity secureExchange = new SecureExchangeBuilder().build();

    String createUser = "API";

    Date createDate = new Date();

    String updateUser = "API";

    Date updateDate = new Date();

    byte[] documentData = "My card!".getBytes();


    public DocumentBuilder withDocumentID(UUID documentID) {
        this.documentID = documentID;
        return this;
    }

    public DocumentBuilder withoutDocumentID() {
        this.documentID = null;
        return this;
    }

    public DocumentBuilder withTypeCode(String typeCode) {
        this.documentTypeCode = typeCode;
        return this;
    }

    public DocumentBuilder withFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public DocumentBuilder withFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
        return this;
    }

    public DocumentBuilder withFileSize(int fileSize) {
        this.fileSize = fileSize;
        return this;
    }

    public DocumentBuilder withSecureExchange(SecureExchangeEntity secureExchange) {
        this.secureExchange = secureExchange;
        return this;
    }

    public DocumentBuilder withData(byte[] data) {
        this.documentData = data;
        return this;
    }

    public DocumentBuilder withoutCreateAndUpdateUser() {
        this.createUser = null;
        this.createDate = null;
        this.updateUser = null;
        this.updateDate = null;

        if(this.secureExchange != null) {
            this.secureExchange.setCreateUser(null);
            this.secureExchange.setCreateDate(null);
            this.secureExchange.setUpdateUser(null);
            this.secureExchange.setUpdateDate(null);
        }
        return this;
    }

    public SecureExchangeDocumentEntity build() {
        SecureExchangeDocumentEntity doc = new SecureExchangeDocumentEntity();
        doc.setDocumentID(this.documentID);
        doc.setDocumentTypeCode(this.documentTypeCode);
        doc.setFileName(this.fileName);
        doc.setFileExtension(this.fileExtension);
        doc.setFileSize(this.fileSize);
        doc.setDocumentData(this.documentData);
        doc.setCreateUser(this.createUser);
        doc.setCreateDate(LocalDateTime.now());
        doc.setUpdateUser(this.updateUser);
        doc.setUpdateDate(LocalDateTime.now());
        doc.setSecureExchangeEntity(this.secureExchange);

        return doc;
    }


}
