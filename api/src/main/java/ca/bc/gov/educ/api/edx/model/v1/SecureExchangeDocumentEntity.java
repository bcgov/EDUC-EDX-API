package ca.bc.gov.educ.api.edx.model.v1;

import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.annotations.Parameter;

import java.sql.Types;
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
  SecureExchangeEntity secureExchangeEntity;

  @NotNull(message = "documentTypeCode cannot be null")
  @Column(name = "SECURE_EXCHANGE_DOCUMENT_TYPE_CODE")
  String documentTypeCode;

  @Column(name = "EDX_USER_ID")
  UUID edxUserID;

  @Column(name = "STAFF_USER_IDENTIFIER")
  String staffUserIdentifier;

  @NotNull(message = "commentUserName cannot be null")
  @Column(name = "FILE_NAME")
  String fileName;

  @Column(name = "FILE_EXTENSION")
  String fileExtension;

  @Column(name = "FILE_SIZE")
  Integer fileSize;

  @NotNull(message = "createUser cannot be null")
  @Column(name = "CREATE_USER", updatable = false)
  String createUser;

  @NotNull(message = "createDate cannot be null")
  @PastOrPresent
  @Column(name = "CREATE_DATE", updatable = false)
  LocalDateTime createDate;

  @NotNull(message = "updateUser cannot be null")
  @Column(name = "UPDATE_USER")
  String updateUser;

  @NotNull(message = "updateDate cannot be null")
  @PastOrPresent
  @Column(name = "UPDATE_DATE")
  LocalDateTime updateDate;

  @Basic(fetch = FetchType.LAZY)
  @Lob
  @JdbcTypeCode(Types.VARBINARY)
  @Column(name = "DOCUMENT_DATA", columnDefinition = "BLOB")
  byte[] documentData;
}
