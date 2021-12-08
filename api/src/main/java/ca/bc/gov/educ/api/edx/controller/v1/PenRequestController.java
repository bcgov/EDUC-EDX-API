package ca.bc.gov.educ.api.edx.controller.v1;

import ca.bc.gov.educ.api.edx.constants.StatsType;
import ca.bc.gov.educ.api.edx.controller.BaseController;
import ca.bc.gov.educ.api.edx.endpoint.v1.PenRequestEndpoint;
import ca.bc.gov.educ.api.edx.exception.InvalidParameterException;
import ca.bc.gov.educ.api.edx.exception.InvalidPayloadException;
import ca.bc.gov.educ.api.edx.exception.PenRequestRuntimeException;
import ca.bc.gov.educ.api.edx.exception.errors.ApiError;
import ca.bc.gov.educ.api.edx.filter.FilterOperation;
import ca.bc.gov.educ.api.edx.filter.PenRequestFilterSpecs;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestEntityMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestGenderCodeMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.PenRequestStatusCodeMapper;
import ca.bc.gov.educ.api.edx.model.v1.PenRequestEntity;
import ca.bc.gov.educ.api.edx.service.v1.PenRequestService;
import ca.bc.gov.educ.api.edx.service.v1.PenRequestStatsService;
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
import org.springframework.lang.NonNull;
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
public class PenRequestController extends BaseController implements PenRequestEndpoint {

  @Getter(AccessLevel.PRIVATE)
  private final PenRequestPayloadValidator payloadValidator;
  @Getter(AccessLevel.PRIVATE)
  private final PenRequestService service;
  private static final PenRequestEntityMapper mapper = PenRequestEntityMapper.mapper;
  private static final PenRequestStatusCodeMapper statusCodeMapper = PenRequestStatusCodeMapper.mapper;
  private static final PenRequestGenderCodeMapper genderCodeMapper = PenRequestGenderCodeMapper.mapper;
  private final PenRequestFilterSpecs penRequestFilterSpecs;
 private final PenRequestStatsService penRequestStatsService;
  @Autowired
  PenRequestController(final PenRequestService penRequest, final PenRequestPayloadValidator payloadValidator, PenRequestFilterSpecs penRequestFilterSpecs, PenRequestStatsService penRequestStatsService) {
    this.service = penRequest;
    this.payloadValidator = payloadValidator;
    this.penRequestFilterSpecs = penRequestFilterSpecs;
    this.penRequestStatsService = penRequestStatsService;
  }

  public PenRequest retrievePenRequest(String id) {
    return mapper.toStructure(getService().retrievePenRequest(UUIDUtil.fromString(id)));
  }

  @Override
  public Iterable<PenRequest> findPenRequests(final String digitalID, final String status, final String pen) {
    return getService().findPenRequests(UUIDUtil.fromString(digitalID), status, pen).stream().map(mapper::toStructure).collect(Collectors.toList());
  }

  public PenRequest createPenRequest(PenRequest penRequest) {
    validatePayload(penRequest, true);
    setAuditColumns(penRequest);
    return mapper.toStructure(getService().createPenRequest(mapper.toModel(penRequest)));
  }

  public PenRequest updatePenRequest(PenRequest penRequest) {
    validatePayload(penRequest, false);
    setAuditColumns(penRequest);
    return mapper.toStructure(getService().updatePenRequest(mapper.toModel(penRequest)));
  }

  public List<PenRequestStatusCode> getPenRequestStatusCodes() {
    val penRequestStatusCodes = new ArrayList<PenRequestStatusCode>();
    getService().getPenRequestStatusCodesList().forEach(element -> penRequestStatusCodes.add(statusCodeMapper.toStructure(element)));
    return penRequestStatusCodes;
  }

  public List<GenderCode> getGenderCodes() {
    return getService().getGenderCodesList().stream().map(genderCodeMapper::toStructure).collect(Collectors.toList());
  }


  private void validatePayload(PenRequest penRequest, boolean isCreateOperation) {
    val validationResult = getPayloadValidator().validatePayload(penRequest, isCreateOperation);
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
  public CompletableFuture<Page<PenRequest>> findAll(Integer pageNumber, Integer pageSize, String sortCriteriaJson, String searchCriteriaListJson) {
    val objectMapper = new ObjectMapper();
    final List<Sort.Order> sorts = new ArrayList<>();
    Specification<PenRequestEntity> penRequestSpecs = null;
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
    return getService().findAll(penRequestSpecs, pageNumber, pageSize, sorts).thenApplyAsync(penRequestEntities -> penRequestEntities.map(mapper::toStructure));
  }

  @Override
  public ResponseEntity<PenRequestStats> getStats(@NonNull final StatsType statsType) {
    return ResponseEntity.ok(this.penRequestStatsService.getStats(statsType));
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

  private Specification<PenRequestEntity> getPenRequestEntitySpecification(List<SearchCriteria> criteriaList) {
    Specification<PenRequestEntity> penRequestSpecs = null;
    if (!criteriaList.isEmpty()) {
      var i = 0;
      for (SearchCriteria criteria : criteriaList) {
        if (criteria.getKey() != null && criteria.getOperation() != null && criteria.getValueType() != null) {
          Specification<PenRequestEntity> typeSpecification = getTypeSpecification(criteria.getKey(), criteria.getOperation(), criteria.getValue(), criteria.getValueType());
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

  private Specification<PenRequestEntity> getTypeSpecification(String key, FilterOperation filterOperation, String value, ValueType valueType) {
    Specification<PenRequestEntity> penRequestSpecs = null;
    switch (valueType) {
      case STRING:
        penRequestSpecs = penRequestFilterSpecs.getStringTypeSpecification(key, value, filterOperation);
        break;
      case DATE_TIME:
        penRequestSpecs = penRequestFilterSpecs.getDateTimeTypeSpecification(key, value, filterOperation);
        break;
      case LONG:
        penRequestSpecs = penRequestFilterSpecs.getLongTypeSpecification(key, value, filterOperation);
        break;
      case INTEGER:
        penRequestSpecs = penRequestFilterSpecs.getIntegerTypeSpecification(key, value, filterOperation);
        break;
      case DATE:
        penRequestSpecs = penRequestFilterSpecs.getDateTypeSpecification(key, value, filterOperation);
        break;
      case UUID:
        penRequestSpecs = penRequestFilterSpecs.getUUIDTypeSpecification(key, value, filterOperation);
        break;
      default:
        break;
    }
    return penRequestSpecs;
  }

}

