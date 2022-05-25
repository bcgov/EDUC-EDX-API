package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "EDX_MINISTRY_OWNERSHIP_TEAM")
@Getter
@Setter
public class MinistryOwnershipTeamEntity {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "EDX_MINISTRY_OWNERSHIP_TEAM_ID", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID ministryOwnershipTeamId;

  @NotNull(message = "teamName cannot be null")
  @Column(name = "TEAM_NAME")
  String teamName;

  @NotNull(message = "groupRoleIdentifier cannot be null")
  @Column(name = "GROUP_ROLE_IDENTIFIER")
  String groupRoleIdentifier;

  @Column(name = "DESCRIPTION")
  String description;

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

}
