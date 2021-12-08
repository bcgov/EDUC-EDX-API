package ca.bc.gov.educ.api.edx.struct.v1;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PenReqDocumentMetadata implements Serializable {

  private static final long serialVersionUID = -8436081551683809151L;
  String penRequestID;
  String digitalID;
  String documentID;
  String documentTypeCode;
  String fileName;
  String fileExtension;
  Integer fileSize;
  String createDate;
}
