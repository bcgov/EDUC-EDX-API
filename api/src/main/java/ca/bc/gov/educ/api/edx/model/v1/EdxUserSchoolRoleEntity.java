package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Data;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;


@Data
@Entity
@Table(name = "EDX_USER_SCHOOL_ROLE", uniqueConstraints = {@UniqueConstraint(name = "EDX_USER_SCHOOL_ID_EDX_ROLE_UK", columnNames = {"EDX_USER_SCHOOL_ID", "EDX_ROLE_CODE"})})
@DynamicUpdate
public class EdxUserSchoolRoleEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "EDX_USER_SCHOOL_ROLE_ID", updatable = false, columnDefinition = "BINARY(16)")
    UUID edxUserSchoolRoleID;

    @Column(name = "CREATE_USER", updatable = false)
    String createUser;

    @PastOrPresent
    @Column(name = "CREATE_DATE", updatable = false)
    LocalDateTime createDate;

    @Column(name = "update_user")
    String updateUser;

    @PastOrPresent
    @Column(name = "update_date")
    LocalDateTime updateDate;

    @ManyToOne(optional = false, targetEntity = EdxUserSchoolEntity.class)
    @JoinColumn(name = "EDX_USER_SCHOOL_ID", referencedColumnName = "EDX_USER_SCHOOL_ID", updatable = false)
    private EdxUserSchoolEntity edxUserSchoolEntity;

    @Column(name = "EDX_ROLE_CODE")
    private String edxRoleCode;
}
