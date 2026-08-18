package com.finance.smartLedger.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PermissionRequest {

  @NotBlank(message = "Permission code is required")
  @Size(max = 100, message = "Permission code must not exceed 100 characters")
  private String code;

  @NotBlank(message = "Permission name is required")
  @Size(max = 100, message = "Permission name must not exceed 100 characters")
  private String name;

  private String description;

  @Size(max = 50, message = "Resource must not exceed 50 characters")
  private String resource;

  @Size(max = 50, message = "Action must not exceed 50 characters")
  private String action;
}
