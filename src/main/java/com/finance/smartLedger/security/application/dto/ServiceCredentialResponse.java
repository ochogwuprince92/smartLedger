package com.finance.smartLedger.security.application.dto;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public record ServiceCredentialResponse(
    UUID id,
    String name,
    Set<String> grantedPermissions,
    boolean enabled,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    String apiKey // Only shown on creation, never retrievable again
) {
  public static ServiceCredentialResponse from(
      UUID id,
      String name,
      Set<String> grantedPermissions,
      boolean enabled,
      LocalDateTime createdAt,
      LocalDateTime updatedAt,
      String apiKey) {
    return new ServiceCredentialResponse(id, name, grantedPermissions, enabled, createdAt, updatedAt, apiKey);
  }
  
  public static ServiceCredentialResponse fromWithoutApiKey(
      UUID id,
      String name,
      Set<String> grantedPermissions,
      boolean enabled,
      LocalDateTime createdAt,
      LocalDateTime updatedAt) {
    return new ServiceCredentialResponse(id, name, grantedPermissions, enabled, createdAt, updatedAt, null);
  }
}
