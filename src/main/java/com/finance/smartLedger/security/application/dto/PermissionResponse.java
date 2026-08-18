package com.finance.smartLedger.security.application.dto;

import com.finance.smartLedger.security.domain.Permission;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Data;

@Data
public class PermissionResponse {

  private UUID id;
  private String code;
  private String name;
  private String description;
  private String resource;
  private String action;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;

  public static PermissionResponse from(Permission permission) {
    PermissionResponse response = new PermissionResponse();
    response.setId(permission.getId());
    response.setCode(permission.getCode());
    response.setName(permission.getName());
    response.setDescription(permission.getDescription());
    response.setResource(permission.getResource());
    response.setAction(permission.getAction());
    response.setCreatedAt(permission.getCreatedAt());
    response.setUpdatedAt(permission.getUpdatedAt());
    return response;
  }
}
