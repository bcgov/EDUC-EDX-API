package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.config.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.SecureExchangeEndpoint;
import ca.bc.gov.educ.api.edx.exception.InvalidParameterException;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.PenRequestRuntimeException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.filter.FilterOperation;
import ca.bc.gov.educ.api.edx.filter.SecureExchangeFilterSpecs;
import ca.bc.gov.educ.api.edx.config.mappers.v1.PenRequestStatusCodeMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.service.v1.SecureExchangeService;
import ca.bc.gov.educ.api.edx.struct.v1.*;
import ca.bc.gov.educ.api.edx.utils.UUIDUtil;
import ca.bc.gov.educ.api.edx.validator.PenRequestPayloadValidator;
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
  private final PenRequestPayloadValidator payloadValidator;
  @Getter(AccessLevel.PRIVATE)
  private final SecureExchangeService service;
  private static final SecureExchangeEntityMapper mapper = SecureExchangeEntityMapper.mapper;
  private static final PenRequestStatusCodeMapper statusCodeMapper = PenRequestStatusCodeMapper.mapper;
  private final SecureExchangeFilterSpecs secureExchangeFilterSpecs;
  @Autowired
  SecureExchangeController(final SecureExchangeService secureExchange, final PenRequestPayloadValidator payloadValidator, SecureExchangeFilterSpecs secureExchangeFilterSpecs) {
    this.service = secureExchange;
    this.payloadValidator = payloadValidator;
    this.secureExchangeFilterSpecs = secureExchangeFilterSpecs;
  }

  public SecureExchange retrieveSecureExchange(String secure_exchange_id) {
    return mapper.toStructure(getService().retrieveSecureExchange(UUIDUtil.fromString(secure_exchange_id)));
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
        return mapper.toStructure(getService().updatePenRequest(mapper.toModel(secureExchange)));
    }

  public List<SecureExchangeStatusCode> getSecureExchangeStatusCodes() {
    val secureExchangeStatusCode = new ArrayList<SecureExchangeStatusCode>();
    getService().getPenRequestStatusCodesList().forEach(element -> secureExchangeStatusCode.add(statusCodeMapper.toStructure(element)));
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
    Specification<SecureExchangeEntity> penRequestSpecs = null;
    try {
      getSortCriteria(sortCriteriaJson, objectMapper, sorts);
      if (StringUtils.isNotBlank(searchCriteriaListJson)) {
        List<SearchCriteria> criteriaList = objectMapper.readValue(searchCriteriaListJson, new TypeReference<>() {
        });
        penRequestSpecs = getPenRequestEntitySpecification(criteriaList);
      }
    } catch (JsonProcessingException e) {
      throw new PenRequestRuntimeException(e.getMessage());
    }
    return getService().findAll(penRequestSpecs, pageNumber, pageSize, sorts).thenApplyAsync(secureExchangeEntities -> secureExchangeEntities.map(mapper::toStructure));
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

  private Specification<SecureExchangeEntity> getPenRequestEntitySpecification(List<SearchCriteria> criteriaList) {
    Specification<SecureExchangeEntity> penRequestSpecs = null;
    if (!criteriaList.isEmpty()) {
      var i = 0;
      for (SearchCriteria criteria : criteriaList) {
        if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
          Specification<SecureExchangeEntity> typeSpecification = getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteria.getValue(), criteria.getValueType());
          if (i == 0) {
            penRequestSpecs = Specification.where(typeSpecification);
          } else {
            assert penRequestSpecs != null;
            penRequestSpecs = penRequestSpecs.and(typeSpecification);
          }
          i++;
        } else {
          throw new InvalidParameterException("Search Criteria can not contain null values for key, value and operation type");
        }
      }
    }
    return penRequestSpecs;
  }

  private Specification<SecureExchangeEntity> getTypeSpecification(String key, FilterOperation filterOperation, String value, ValueType valueType) {
    Specification<SecureExchangeEntity> penRequestSpecs = null;
    switch (valueType) {
      case STRING:
        penRequestSpecs = secureExchangeFilterSpecs.getStringTypeSpecification(key, value, filterOperation);
        break;
      case DATE_TIME:
        penRequestSpecs = secureExchangeFilterSpecs.getDateTimeTypeSpecification(key, value, filterOperation);
        break;
      case LONG:
        penRequestSpecs = secureExchangeFilterSpecs.getLongTypeSpecification(key, value, filterOperation);
        break;
      case INTEGER:
        penRequestSpecs = secureExchangeFilterSpecs.getIntegerTypeSpecification(key, value, filterOperation);
        break;
      case DATE:
        penRequestSpecs = secureExchangeFilterSpecs.getDateTypeSpecification(key, value, filterOperation);
        break;
      case UUID:
        penRequestSpecs = secureExchangeFilterSpecs.getUUIDTypeSpecification(key, value, filterOperation);
        break;
      default:
        break;
    }
    return penRequestSpecs;
  }

}

