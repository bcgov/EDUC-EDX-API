package ca.bc.gov.educ.api.edx.model.v1;

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
@Table(name = "SECURE_EXCHANGE_DOCUMENT")
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
  @Column(name = "SECURE_EXCHANGE_DOCUMENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID documentID;

  @ManyToOne
  @JoinColumn(name = "SECURE_EXCHANGE_ID", updatable = false, columnDefinition = "BINARY(16)")
  SecureExchangeEntity secureExchange;

  @Column(name = "SECURE_EXCHANGE_DOCUMENT_TYPE_CODE")
  String documentTypeCode;

  @Column(name = "FILE_NAME")
  String fileName;

  @Column(name = "FILE_EXTENSION")
  String fileExtension;

  @Column(name = "FILE_SIZE")
  Integer fileSize;

  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @Column(name = "UPDATE_USER")
  String updateUser;

  @PastOrPresent
  @Column(name = "UPDATE_DATE")

  LocalDateTime updateDate;

  @Basic(fetch = FetchType.LAZY)
  @Lob
  @Column(name = "DOCUMENT_DATA")
  byte[] documentData;

}
