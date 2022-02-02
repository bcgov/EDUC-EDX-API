package ca.bc.gov.educ.api.edx.utils;

import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.SecureExchangeRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Spring boot utility class to manage transaction boundary.
 */
@Component
@Profile("test")
public class SecureExchangeAPITestUtils {

  @Autowired
  private DocumentRepository documentRepository;


  @Autowired
  private SecureExchangeRequestRepository secureExchangeRequestRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void cleanDB() {
    this.documentRepository.deleteAll();
    this.secureExchangeRequestRepository.deleteAll();
  }

  @Transactional
  public byte[] getDocumentBlobByDocumentID(final UUID documentID) {
    return this.documentRepository.findById(documentID).orElseThrow().getDocumentData();
  }
}
