package ca.bc.gov.educ.api.edx.utils;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Data
class TestParentClass {
  @UpperCase
  String filedA;

  String filedB;
}

@Data
@EqualsAndHashCode(callSuper = true)
class TestChildClass extends TestParentClass {
  @UpperCase
  String filedE;
}

@RunWith(SpringRunner.class)
class TransformUtilTest {
  @Test
  void testIsUppercaseField_WhenFieldInParentClass_ShouldReturnTrue()  {
    assertTrue(TransformUtil.isUppercaseField(TestChildClass.class, "filedA"));
  }

  @Test
  void testIsUppercaseField_WhenFieldInClass_ShouldReturnTrue()  {
    assertTrue(TransformUtil.isUppercaseField(TestChildClass.class, "filedE"));
  }

  @Test
  void testIsUppercaseField_WhenFieldNotExists_ShouldReturnFalse()  {
    assertFalse(TransformUtil.isUppercaseField(TestChildClass.class, "filedC"));
  }

  @Test
  void testIsUppercaseField_WhenFieldIsNotUppercased_ShouldReturnFalse()  {
    assertFalse(TransformUtil.isUppercaseField(TestChildClass.class, "filedB"));
  }
}
