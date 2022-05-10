package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;


@Data
@Entity
@Table(name = "EDX_USER_SCHOOL", uniqueConstraints = {@UniqueConstraint(name = "EDX_USER_ID_MINCODE_UK", columnNames = {"EDX_USER_ID", "MINCODE"})})
@DynamicUpdate
public class EdxUserSchoolEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
            @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
    @Column(name = "EDX_USER_SCHOOL_ID", updatable = false, columnDefinition = "BINARY(16)")
    UUID edxUserSchoolID;

    @Column(name = "EDX_USER_ID", updatable = false, columnDefinition = "BINARY(16)")
    UUID edxUserID;

    @Column(name = "MINCODE")
    String mincode;

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

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(mappedBy = "edxUserSchoolEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxUserSchoolRoleEntity.class)
    private Set<EdxUserSchoolRoleEntity> edxUserSchoolRoleEntities;
}
