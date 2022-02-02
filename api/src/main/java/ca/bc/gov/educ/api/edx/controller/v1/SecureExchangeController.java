package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.config.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.config.mappers.v1.SecureExchangeStatusCodeMapper;
import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.SecureExchangeEndpoint;
import ca.bc.gov.educ.api.edx.exception.InvalidParameterException;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.SecureExchangeRuntimeException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.filter.FilterOperation;
import ca.bc.gov.educ.api.edx.filter.SecureExchangeFilterSpecs;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.UUIDUtil;
import ca.bc.gov.educ.api.edx.validator.SecureExchangePayloadValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestController
@Slf4j
public class SecureExchangeController extends BaseController implements SecureExchangeEndpoint {

  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangePayloadValidator payloadValidator;
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeService service;
  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
  private static final SecureExchangeStatusCodeMapper statusCodeMapper = SecureExchangeStatusCodeMapper.mapper;
  private final SecureExchangeFilterSpecs secureExchangeFilterSpecs;
  @Autowired
  SecureExchangeController(final SecureExchangeService secureExchange, final SecureExchangePayloadValidator payloadValidator, SecureExchangeFilterSpecs secureExchangeFilterSpecs) {
    this.service = secureExchange;
    this.payloadValidator = payloadValidator;
    this.secureExchangeFilterSpecs = secureExchangeFilterSpecs;
  }

  public SecureExchange retrieveSecureExchange(String secureExchangeId) {
    return mapper.toStructure(getService().retrieveSecureExchange(UUIDUtil.fromString(secureExchangeId)));
  }

    @Override
    public List<SecureExchange> findSecureExchanges(String digitalID, String status) {
        return getService().findSecureExchange(UUIDUtil.fromString(digitalID), status).stream().map(mapper::toStructure).collect(Collectors.toList());
    }

    @Override
    public SecureExchange createSecureExchange(SecureExchange secureExchange) {
        validatePayload(secureExchange, true);
        setAuditColumns(secureExchange);
        return mapper.toStructure(getService().createSecureExchange(mapper.toModel(secureExchange)));
    }

    @Override
    public SecureExchange updateSecureExchange(SecureExchange secureExchange) {
        validatePayload(secureExchange, false);
        setAuditColumns(secureExchange);
        return mapper.toStructure(getService().updateSecureExchange(mapper.toModel(secureExchange)));
    }

  public List<SecureExchangeStatusCode> getSecureExchangeStatusCodes() {
    val secureExchangeStatusCode = new ArrayList<SecureExchangeStatusCode>();
    getService().getSecureExchangeStatusCodesList().forEach(element -> secureExchangeStatusCode.add(statusCodeMapper.toStructure(element)));
    return secureExchangeStatusCode;
  }



  private void validatePayload(SecureExchange secureExchange, boolean isCreateOperation) {
    val validationResult = getPayloadValidator().validatePayload(secureExchange, isCreateOperation);
    if (!validationResult.isEmpty()) {
      ApiError error = ApiError.builder().timestamp(LocalDateTime.now()).message("Payload contains invalid data.").status(BAD_REQUEST).build();
      error.addValidationErrors(validationResult);
      throw new InvalidPayloadException(error);
    }
  }

  @Override
  @Transactional
  public ResponseEntity<Void> deleteById(final UUID id) {
    getService().deleteById(id);
    return ResponseEntity.noContent().build();
  }

  @Override
  @Transactional(propagation = Propagation.SUPPORTS)
  public CompletableFuture<Page<SecureExchange>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
    val objectMapper = new ObjectMapper();
    final List<Sort.Order> sorts = new ArrayList<>();
    Specification<SecureExchangeEntity> secureExchangeSpecs = null;
    try {
      getSortCriteria(sortCriteriaJson, objectMapper, sorts);
      if (StringUtils.isNotBlank(searchCriteriaListJson)) {
        List<SearchCriteria> criteriaList = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        secureExchangeSpecs = getSecureExchangeEntitySpecification(criteriaList);
      }
    } catch (JsonProcessingException e) {
      throw new SecureExchangeRuntimeException(e.getMessage());
    }
    return getService().findAll(secureExchangeSpecs, pageNumber, pageSize, sorts).thenApplyAsync(secureExchangeEntities -> secureExchangeEntities.map(mapper::toStructure));
  }


  private void getSortCriteria(String sortCriteriaJson, ObjectMapper objectMapper, List<Sort.Order> sorts) throws JsonProcessingException {
    if (StringUtils.isNotBlank(sortCriteriaJson)) {
      Map<String, String> sortMap = objectMapper.readValue(sortCriteriaJson, new TypeReference<>() {
      });
      sortMap.forEach((k, v) -> {
        if ("ASC".equalsIgnoreCase(v)) {
          sorts.add(new Sort.Order(Sort.Direction.ASC, k));
        } else {
          sorts.add(new Sort.Order(Sort.Direction.DESC, k));
        }
      });
    }
  }

  private Specification<SecureExchangeEntity> getSecureExchangeEntitySpecification(List<SearchCriteria> criteriaList) {
    Specification<SecureExchangeEntity> secureExchangeSpecs = null;
    if (!criteriaList.isEmpty()) {
      var i = 0;
      for (SearchCriteria criteria : criteriaList) {
        if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
          Specification<SecureExchangeEntity> typeSpecification = getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteria.getValue(), criteria.getValueType());
          if (i == 0) {
            secureExchangeSpecs = Specification.where(typeSpecification);
          } else {
            assert secureExchangeSpecs != null;
            secureExchangeSpecs = secureExchangeSpecs.and(typeSpecification);
          }
          i++;
        } else {
          throw new InvalidParameterException("Search Criteria can not contain null values for key, value and operation type");
        }
      }
    }
    return secureExchangeSpecs;
  }

  private Specification<SecureExchangeEntity> getTypeSpecification(String key, FilterOperation filterOperation, String value, ValueType valueType) {
    Specification<SecureExchangeEntity> secureExchangeSpecs = null;
    switch (valueType) {
      case STRING:
        secureExchangeSpecs = secureExchangeFilterSpecs.getStringTypeSpecification(key, value, filterOperation);
        break;
      case DATE_TIME:
        secureExchangeSpecs = secureExchangeFilterSpecs.getDateTimeTypeSpecification(key, value, filterOperation);
        break;
      case LONG:
        secureExchangeSpecs = secureExchangeFilterSpecs.getLongTypeSpecification(key, value, filterOperation);
        break;
      case INTEGER:
        secureExchangeSpecs = secureExchangeFilterSpecs.getIntegerTypeSpecification(key, value, filterOperation);
        break;
      case DATE:
        secureExchangeSpecs = secureExchangeFilterSpecs.getDateTypeSpecification(key, value, filterOperation);
        break;
      case UUID:
        secureExchangeSpecs = secureExchangeFilterSpecs.getUUIDTypeSpecification(key, value, filterOperation);
        break;
      default:
        break;
    }
    return secureExchangeSpecs;
  }

}

