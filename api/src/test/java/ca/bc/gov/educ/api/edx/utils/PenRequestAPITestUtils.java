package ca.bc.gov.educ.api.edx.utils;

import ca.bc.gov.educ.api.edx.repository.DocumentRepository;
import ca.bc.gov.educ.api.edx.repository.PenRequestRepository;
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
public class PenRequestAPITestUtils {

  @Autowired
  private DocumentRepository documentRepository;


  @Autowired
  private PenRequestRepository penRequestRepository;

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void cleanDB() {
    this.documentRepository.deleteAll();
    this.penRequestRepository.deleteAll();
  }

  @Transactional
  public byte[] getDocumentBlobByDocumentID(final UUID documentID) {
    return this.documentRepository.findById(documentID).orElseThrow().getDocumentData();
  }
}
