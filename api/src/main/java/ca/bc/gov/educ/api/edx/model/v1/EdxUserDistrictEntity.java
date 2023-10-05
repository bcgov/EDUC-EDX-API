package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.*;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


/**
 * The type Edx user district entity.
 */
@Data
@Entity
@Table(name = "EDX_USER_DISTRICT" , uniqueConstraints = {@UniqueConstraint(name = "EDX_USER_ID_DISTRICT_ID_UK", columnNames = {"EDX_USER_ID", "DISTRICT_ID"})})
@DynamicUpdate
public class EdxUserDistrictEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_USER_DISTRICT_ID", updatable = false, columnDefinition = "BINARY(16)")
  UUID edxUserDistrictID;

  @ManyToOne(optional = false,targetEntity = EdxUserEntity.class)
  @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
  @JoinColumn(name = "EDX_USER_ID", referencedColumnName = "EDX_USER_ID")
  EdxUserEntity edxUserEntity;

  @Column(name = "DISTRICT_ID", columnDefinition = "BINARY(16)")
  UUID districtID;

  @Column(name = "EXPIRY_DATE")
  LocalDateTime expiryDate;

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

  public Set<EdxUserDistrictRoleEntity> getEdxUserDistrictRoleEntities() {
    if(this.edxUserDistrictRoleEntities == null){
      this.edxUserDistrictRoleEntities = new HashSet<>();
    }
    return this.edxUserDistrictRoleEntities;
  }

  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  @OneToMany(mappedBy = "edxUserDistrictEntity", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true, targetEntity = EdxUserDistrictRoleEntity.class)
  private Set<EdxUserDistrictRoleEntity> edxUserDistrictRoleEntities;

  @PreRemove
  public void preRemove() {
    if(this.edxUserEntity != null) {
      this.edxUserEntity.getEdxUserDistrictEntities().remove(this);
      this.edxUserEntity = null;
    }
  }

}
