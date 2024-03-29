package ca.bc.gov.educ.api.edx.mappers;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
class Base64MapperTest {
  private final Base64Mapper mapper = new Base64Mapper();

  @Test
  void testMap_GivenNullString_ReturnsBlankByteArray() {
    byte[] bytes = mapper.map((String) null);
    assertEquals(0, bytes.length);
  }

  @Test
  void testMap_GivenNullByteArray_ReturnsNullString() {
    String result = mapper.map((byte[]) null);
    assertNull(result);
  }
}
