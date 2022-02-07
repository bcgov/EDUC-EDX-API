package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.MinistryOwnershipTeamEntity;
import ca.bc.gov.educ.api.edx.struct.v1.MinistryTeam;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface MinistryTeamMapper {

  MinistryTeamMapper mapper = Mappers.getMapper(MinistryTeamMapper.class);

  MinistryTeam toStructure(MinistryOwnershipTeamEntity entity);
}
