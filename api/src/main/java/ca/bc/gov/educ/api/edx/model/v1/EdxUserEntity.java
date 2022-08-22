package ca.bc.gov.educ.api.edx.model.v1;

import ca.bc.gov.educ.api.edx.utils.UpperCase;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Data
@Entity
@Table(name = "EDX_USER")
@DynamicUpdate
public class EdxUserEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
    @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_USER_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserID;

  @Column(name = "DIGITAL_IDENTITY_ID", unique = true, columnDefinition = "BINARY(16)")
  UUID digitalIdentityID;

  @Column(name = "FIRST_NAME")
  @UpperCase
  String firstName;

  @Column(name = "LAST_NAME")
  @UpperCase
  String lastName;

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
  @OneToMany(mappedBy = "edxUserEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxUserSchoolEntity.class)
  private Set<EdxUserSchoolEntity> edxUserSchoolEntities;

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "edxUserEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxUserDistrictEntity.class)
  private Set<EdxUserDistrictEntity> edxUserDistrictEntities;

  @Column(name = "EMAIL")
  @UpperCase
  String email;

  public Set<EdxUserDistrictEntity> getEdxUserDistrictEntities() {
    if(this.edxUserDistrictEntities== null){
      this.edxUserDistrictEntities = new HashSet<>();
    }
    return this.edxUserDistrictEntities;
  }

  public Set<EdxUserSchoolEntity> getEdxUserSchoolEntities() {
    if(this.edxUserSchoolEntities== null){
      this.edxUserSchoolEntities = new HashSet<>();
    }
    return this.edxUserSchoolEntities;

  }
}
