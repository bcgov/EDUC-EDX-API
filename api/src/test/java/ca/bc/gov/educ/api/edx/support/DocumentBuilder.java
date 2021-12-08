package ca.bc.gov.educ.api.edx.support;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

import ca.bc.gov.educ.api.edx.model.v1.DocumentEntity;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;

public class DocumentBuilder {
    UUID documentID = UUID.randomUUID();

    String documentTypeCode = "BCSCPHOTO";

    String fileName = "card";

    String fileExtension = "jpg";

    int fileSize = 8;

    PenRequestEntity penRequest = new PenRequestBuilder().build();

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

    public DocumentBuilder withPenRequest(PenRequestEntity penRequest) {
        this.penRequest = penRequest;
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

        if(this.penRequest != null) {
            this.penRequest.setCreateUser(null);
            this.penRequest.setCreateDate(null);
            this.penRequest.setUpdateUser(null);
            this.penRequest.setUpdateDate(null);
        }
        return this;
    }

    public DocumentEntity build() {
        DocumentEntity doc = new DocumentEntity();
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
        doc.setPenRequest(this.penRequest);

        return doc;
    }


}
