package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "SECURE_EXCHANGE_DOCUMENT_TYPE_CODE")
public class SecureExchangeDocumentTypeCodeEntity {
    /**
     * The Status code.
     */
    @Id
    @Column(name = "SECURE_EXCHANGE_DOCUMENT_TYPE_CODE", unique = true, updatable = false)
    String secureExchangeDocumentTypeCode;

    /**
     * The Label.
     */
    @NotNull(message = "label cannot be null")
    @Column(name = "LABEL")
    String label;

    /**
     * The Description.
     */
    @NotNull(message = "description cannot be null")
    @Column(name = "DESCRIPTION")
    String description;

    /**
     * The Display order.
     */
    @NotNull(message = "displayOrder cannot be null")
    @Column(name = "DISPLAY_ORDER")
    Integer displayOrder;

    /**
     * The Effective date.
     */
    @NotNull(message = "effectiveDate cannot be null")
    @Column(name = "EFFECTIVE_DATE")
    LocalDateTime effectiveDate;

    /**
     * The Expiry date.
     */
    @NotNull(message = "expiryDate cannot be null")
    @Column(name = "EXPIRY_DATE")
    LocalDateTime expiryDate;

    /**
     * The Create user.
     */
    @NotNull(message = "createUser cannot be null")
    @Column(name = "CREATE_USER", updatable = false)
    String createUser;

    /**
     * The Create date.
     */
    @NotNull(message = "createDate cannot be null")
    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    /**
     * The Update user.
     */
    @NotNull(message = "updateUser cannot be null")
    @Column(name = "UPDATE_USER", updatable = false)
    String updateUser;

    /**
     * The Update date.
     */
    @NotNull(message = "updateDate cannot be null")
    @PastOrPresent
    @Column(name = "UPDATE_DATE", updatable = false)
    LocalDateTime updateDate;
}
