package ca.bc.gov.educ.api.edx.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SecureExchange extends SecureExchangeBase implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  private List<SecureExchangeStudent> studentsList;

  private List<SecureExchangeNote> noteList;

  private List<SecureExchangeDocMetadata> documentList;
}





