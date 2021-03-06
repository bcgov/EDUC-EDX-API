package ca.bc.gov.educ.api.edx.utils;

import java.util.UUID;

public final class UUIDUtil {
  private UUIDUtil() {
  }


  public static UUID fromString(String uuid) {
    if (uuid == null) {
      return null;
    }
    return UUID.fromString(uuid);
  }
}
