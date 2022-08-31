package ca.bc.gov.educ.api.edx.mappers.v1;

import ca.bc.gov.educ.api.edx.mappers.LocalDateTimeMapper;
import ca.bc.gov.educ.api.edx.mappers.UUIDMapper;
import ca.bc.gov.educ.api.edx.model.v1.SagaEntity;
import ca.bc.gov.educ.api.edx.struct.v1.EdxDistrictUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.EdxUserActivationInviteSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCommentSagaData;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeCreateSagaData;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * The interface Saga mapper.
 */
@Mapper(uses = {UUIDMapper.class, LocalDateTimeMapper.class})
@SuppressWarnings("squid:S1214")
public interface SagaDataMapper {
  /**
   * The constant mapper.
   */
  SagaDataMapper mapper = Mappers.getMapper(SagaDataMapper.class);

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "sagaState", ignore = true)
  @Mapping(target = "sagaId", ignore = true)
  @Mapping(target = "sagaCompensated", ignore = true)
  @Mapping(target = "retryCount", ignore = true)
  @Mapping(target = "payload", expression = "java(ca.bc.gov.educ.api.edx.utils.JsonUtil.getJsonStringFromObject(sagaData))")
  @Mapping(target = "emailId", ignore = true)
  @Mapping(target = "edxUserId", ignore = true)
  @Mapping(target = "districtID", ignore = true)
  SagaEntity toModel(String sagaName, SecureExchangeCreateSagaData sagaData) throws JsonProcessingException;

  @Mapping(target = "status", ignore = true)
  @Mapping(target = "secureExchangeId", ignore = true)
  @Mapping(target = "sagaState", ignore = true)
  @Mapping(target = "sagaId", ignore = true)
  @Mapping(target = "sagaCompensated", ignore = true)
  @Mapping(target = "retryCount", ignore = true)
  @Mapping(target = "payload", expression = "java(ca.bc.gov.educ.api.edx.utils.JsonUtil.getJsonStringFromObject(sagaData))")
  @Mapping(target = "emailId", source = "sagaData.email")
  @Mapping(target = "districtID", ignore = true)
  SagaEntity toModel(String sagaName, EdxUserActivationInviteSagaData sagaData) throws JsonProcessingException;


  @Mapping(target = "status", ignore = true)
  @Mapping(target = "secureExchangeId", ignore = true)
  @Mapping(target = "sagaState", ignore = true)
  @Mapping(target = "sagaId", ignore = true)
  @Mapping(target = "sagaCompensated", ignore = true)
  @Mapping(target = "retryCount", ignore = true)
  @Mapping(target = "payload", expression = "java(ca.bc.gov.educ.api.edx.utils.JsonUtil.getJsonStringFromObject(sagaData))")
  @Mapping(target = "schoolID", ignore = true)
  @Mapping(target = "emailId", source = "sagaData.email")
  SagaEntity toModel(String sagaName, EdxDistrictUserActivationInviteSagaData sagaData) throws JsonProcessingException;


  @Mapping(target = "status", ignore = true)
  @Mapping(target = "sagaState", ignore = true)
  @Mapping(target = "sagaId", ignore = true)
  @Mapping(target = "sagaCompensated", ignore = true)
  @Mapping(target = "retryCount", ignore = true)
  @Mapping(target = "payload", expression = "java(ca.bc.gov.educ.api.edx.utils.JsonUtil.getJsonStringFromObject(sagaData))")
  @Mapping(target = "emailId", ignore = true)
  @Mapping(target = "edxUserId", ignore = true)
  @Mapping(target = "districtID", ignore = true)
  SagaEntity toModel(String sagaName, SecureExchangeCommentSagaData sagaData) throws JsonProcessingException;
}
