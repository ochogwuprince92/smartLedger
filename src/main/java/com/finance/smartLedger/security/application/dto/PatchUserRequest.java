package com.finance.smartLedger.security.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Request to partially update a user")
public record PatchUserRequest(
    @Schema(description = "First name") String firstName,
    @Schema(description = "Last name") String lastName,
    @Schema(description = "Phone number") String phone,
    @Schema(description = "Password update") PasswordUpdateRequest passwordUpdate,
    @Schema(description = "User enabled status") Boolean isEnabled,
    @Schema(description = "User locked status") Boolean isLocked) {

  @Schema(description = "Password update request")
  public record PasswordUpdateRequest(
      @Schema(description = "Current password", required = true) @NotNull String oldPassword,
      @Schema(description = "New password", required = true) @NotNull String newPassword) {}
}
