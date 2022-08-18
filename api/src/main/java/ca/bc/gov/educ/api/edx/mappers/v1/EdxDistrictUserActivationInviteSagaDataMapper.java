package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationCode;
import ca.bc.gov.educ.api.edx.struct.v1.EdxActivationRole;
import ca.bc.gov.educ.api.edx.struct.v1.EdxDistrictUserActivationInviteSagaData;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
public interface EdxDistrictUserActivationInviteSagaDataMapper {


  EdxDistrictUserActivationInviteSagaDataMapper mapper = Mappers.getMapper(EdxDistrictUserActivationInviteSagaDataMapper.class);


  @Mapping(target = "edxActivationRoles", source = "edxActivationRoleCodes")
  EdxActivationCode toEdxActivationCode(EdxDistrictUserActivationInviteSagaData edxDistrictUserActivationInviteSagaData);

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
