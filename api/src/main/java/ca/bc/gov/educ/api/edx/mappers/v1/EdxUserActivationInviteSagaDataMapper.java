package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationRole;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface EdxUserActivationInviteSagaDataMapper {

  EdxUserActivationInviteSagaDataMapper mapper = Mappers.getMapper(EdxUserActivationInviteSagaDataMapper.class);


  @Mapping(target = "edxActivationRoles", source = "edxActivationRoleCodes")
  EdxActivationCode toEdxActivationCode(EdxUserActivationInviteSagaData edxUserActivationInviteSagaData);

  default List<EdxActivationRole> map(List<String> values) {
    if (values == null) {
      return Collections.emptyList();
    }
    List<EdxActivationRole> activationRoles = new ArrayList<>();
    for (String value : values) {
      EdxActivationRole activationRole = new EdxActivationRole();
      activationRole.setEdxRoleCode(value);
      activationRoles.add(activationRole);
    }
    return activationRoles;
  }
}
