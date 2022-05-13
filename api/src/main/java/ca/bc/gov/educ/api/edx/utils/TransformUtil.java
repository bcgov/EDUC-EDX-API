package ca.bc.gov.educ.api.edx.utils;

import ca.bc.gov.educ.api.edx.exception.SecureExchangeRuntimeException;

import java.beans.Expression;
import java.beans.Statement;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.util.StringUtils.capitalize;

public class TransformUtil {
  private TransformUtil() {
  }

  public static <T> T uppercaseFields(T rec) {
    var clazz = rec.getClass();
    List<Field> fields = new ArrayList<>();
    var superClazz = clazz;
    while (!superClazz.equals(Object.class)) {
      fields.addAll(Arrays.asList(superClazz.getDeclaredFields()));
      superClazz = superClazz.getSuperclass();
    }
    fields.forEach(field -> TransformUtil.transformFieldToUppercase(field, rec));
    return rec;
  }

  public static boolean isUppercaseField(Class<?> clazz, String fieldName) {
    var superClazz = clazz;
    while (!superClazz.equals(Object.class)) {
      try {
        Field field = superClazz.getDeclaredField(fieldName);
        return field.getAnnotation(UpperCase.class) != null;
      } catch (NoSuchFieldException e) {
        superClazz = superClazz.getSuperclass();
      }
    }
    return false;
  }

  private static <T> void transformFieldToUppercase(Field field, T rec) {
    if (!field.getType().equals(String.class)) {
      return;
    }

    if (field.getAnnotation(UpperCase.class) != null) {
      try {
        var fieldName = capitalize(field.getName());
        var expr = new Expression(rec, "get" + fieldName, new Object[0]);
        var entityFieldValue = (String) expr.getValue();
        if (entityFieldValue != null) {
          var stmt = new Statement(rec, "set" + fieldName, new Object[]{entityFieldValue.toUpperCase()});
          stmt.execute();
        }
      } catch (Exception ex) {
        throw new SecureExchangeRuntimeException(ex.getMessage());
      }
    }

  }
}