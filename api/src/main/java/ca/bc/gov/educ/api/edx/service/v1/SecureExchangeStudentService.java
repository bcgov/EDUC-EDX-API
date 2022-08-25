package ca.bc.gov.educ.api.edx.service.v1;

import ca.bc.gov.educ.api.edx.exception.EntityNotFoundException;
import ca.bc.gov.educ.api.edx.exception.NotFoundException;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeEntityMapper;
import ca.bc.gov.educ.api.edx.mappers.v1.SecureExchangeStudentMapper;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeEntity;
import ca.bc.gov.educ.api.edx.model.v1.SecureExchangeStudentEntity;
import ca.bc.gov.educ.api.edx.props.ApplicationProperties;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeStudentRepository;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchange;
import ca.bc.gov.educ.api.edx.struct.v1.SecureExchangeStudent;
import ca.bc.gov.educ.api.edx.utils.TransformUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class SecureExchangeStudentService {

    @Getter(AccessLevel.PRIVATE)
    private final SecureExchangeService exchangeService;

    private final RESTService restService;
    private final ApplicationProperties applicationProperties;
    private static final SecureExchangeEntityMapper secureExchangeMapper = SecureExchangeEntityMapper.mapper;
    private static final SecureExchangeStudentMapper studentMapper = SecureExchangeStudentMapper.mapper;
    private final SecureExchangeStudentRepository repository;

    @Autowired
    public SecureExchangeStudentService(SecureExchangeService exchangeService, RESTService restService, ApplicationProperties applicationProperties, SecureExchangeStudentRepository repository) {
        this.exchangeService = exchangeService;
        this.restService = restService;
        this.applicationProperties = applicationProperties;
        this.repository = repository;
    }

    public SecureExchange addStudentToExchange(UUID secureExchangeID, SecureExchangeStudent secureExchangeStudent) throws NotFoundException, EntityNotFoundException {
        // not found exception handler will fire if student not found
        restService.get(applicationProperties.getStudentApiEndpoint() + secureExchangeStudent.getStudentId(), String.class);
        // entity not found will fire if not found
        SecureExchangeEntity secureExchangeEntity = this.exchangeService.retrieveSecureExchange(secureExchangeID);
        if (secureExchangeEntity.getSecureExchangeStudents() == null) {
            secureExchangeEntity.setSecureExchangeStudents(new HashSet<>());
        }
        SecureExchangeStudentEntity student = secureExchangeEntity.getSecureExchangeStudents()
                .stream()
                .filter(s -> UUID.fromString(secureExchangeStudent.getStudentId()).equals(s.getStudentId()))
                .findAny()
                .orElse(null);
        if (student != null) {
            //do nothing as student already exists
            return secureExchangeMapper.toStructure(secureExchangeEntity);
        }
        secureExchangeEntity.setIsReadByMinistry(secureExchangeStudent.getStaffUserIdentifier() != null);
        secureExchangeEntity.setIsReadByExchangeContact(secureExchangeStudent.getEdxUserID() != null);
        if(secureExchangeEntity.getReviewer() != secureExchangeStudent.getStaffUserIdentifier()){
            secureExchangeEntity.setReviewer(secureExchangeStudent.getStaffUserIdentifier());
            TransformUtil.uppercaseFields(secureExchangeEntity);
            TransformUtil.uppercaseFields(secureExchangeStudent);
        }
            SecureExchangeStudentEntity secureExchangeStudentEntity = studentMapper.toModel(secureExchangeStudent);
        secureExchangeStudentEntity.setSecureExchangeEntity(secureExchangeEntity);
        if (secureExchangeStudentEntity.getCreateUser() == null) {
            secureExchangeStudentEntity.setCreateUser(ApplicationProperties.CLIENT_ID);
        }
        secureExchangeStudentEntity.setCreateDate(LocalDateTime.now());
        secureExchangeEntity.getSecureExchangeStudents().add(secureExchangeStudentEntity);
        secureExchangeEntity = this.exchangeService.updateSecureExchange(secureExchangeEntity);
        return secureExchangeMapper.toStructure(secureExchangeEntity);
    }

    public void deleteStudentFromExchange(UUID secureExchangeStudentId) {
        if (repository.existsById(secureExchangeStudentId)) {
            repository.deleteById(secureExchangeStudentId);
        }
    }

    public List<SecureExchangeStudent> getStudentIDsFromExchange(UUID secureExchangeID) throws EntityNotFoundException {
        SecureExchangeEntity secureExchangeEntity = this.exchangeService.retrieveSecureExchange(secureExchangeID);
        SecureExchange secureExchange = secureExchangeMapper.toStructure(secureExchangeEntity);
        return secureExchange.getStudentsList();
    }
}
