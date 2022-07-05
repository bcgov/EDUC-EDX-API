package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "SECURE_EXCHANGE_STUDENT")
public class SecureExchangeStudentEntity {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "SECURE_EXCHANGE_STUDENT_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
    UUID secureExchangeStudentId;

    @NotNull(message = "student id cannot be null")
    @Column(name = "STUDENT_ID", updatable = false)
    UUID studentId;

    @NotNull(message = "createUser cannot be null")
    @Column(name = "CREATE_USER", updatable = false)
    String createUser;

    @NotNull(message = "createDate cannot be null")
    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    @ManyToOne(optional = false, targetEntity = SecureExchangeEntity.class)
    @JoinColumn(name = "SECURE_EXCHANGE_ID", referencedColumnName = "SECURE_EXCHANGE_ID", updatable = false)
    private SecureExchangeEntity secureExchangeEntity;

}
