package com.finance.smartLedger.shared.util;

import java.util.UUID;

public final class UuidGenerator {

  private UuidGenerator() {}

  public static UUID generate() {
    return UUID.randomUUID();
  }

  public static UUID generateFromString(String input) {
    return UUID.nameUUIDFromBytes(input.getBytes());
  }

  public static String generateAsString() {
    return UUID.randomUUID().toString();
  }

  public static boolean isValid(String uuid) {
    try {
      UUID.fromString(uuid);
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static UUID parse(String uuid) {
    return UUID.fromString(uuid);
  }
}
