package ca.bc.gov.educ.api.edx;

import ca.bc.gov.educ.api.edx.utils.SecureExchangeAPITestUtils;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {EdxApiResourceApplication.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseSecureExchangeAPITest {

  @Autowired
  protected SecureExchangeAPITestUtils secureExchangeAPITestUtils;

  @Before
  public void before() {
    this.secureExchangeAPITestUtils.cleanDB();
  }
}
