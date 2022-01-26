package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "secure_exchange_document")
@DynamicUpdate
public class SecureExchangeDocumentEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(
          name = "UUID",
          strategy = "org.hibernate.id.UUIDGenerator",
          parameters = {
                  @Parameter(
                          name = "uuid_gen_strategy_class",
                          value = "org.hibernate.id.uuid.CustomVersionOneStrategy"
                  )
          }
  )
  @Column(name = "secure_exchange_document_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID secureExchangeDocumentID;

  @ManyToOne
  @JoinColumn(name = "secure_exchange_id", updatable = false, columnDefinition = "BINARY(16)")
  SecureExchangeEntity secureExchange;

  @Column(name = "secure_exchange_document_type_code")
  String secureExchangeDocumentTypeCode;

  @Column(name = "file_name")
  String fileName;

  @Column(name = "file_extension")
  String fileExtension;

  @Column(name = "file_size")
  Integer fileSize;

  @Column(name = "create_user", updatable = false)
  String createUser;

  @PastOrPresent
  @Column(name = "create_date", updatable = false)
  LocalDateTime createDate;

  @Column(name = "update_user")
  String updateUser;

  @PastOrPresent
  @Column(name = "update_date")

  LocalDateTime updateDate;

  @Basic(fetch = FetchType.LAZY)
  @Lob
  @Column(name = "document_data")
  byte[] documentData;

}
