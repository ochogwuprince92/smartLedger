package com.finance.smartLedger.security.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleRequest {

  @NotBlank(message = "Role code is required")
  @Size(max = 50, message = "Role code must not exceed 50 characters")
  private String code;

  @NotBlank(message = "Role name is required")
  @Size(max = 100, message = "Role name must not exceed 100 characters")
  private String name;

  private String description;
}
