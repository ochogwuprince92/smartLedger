package com.finance.smartLedger.security.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Login request credentials")
public record LoginRequest(
    @Schema(description = "Username", example = "john.doe", required = true) @NotBlank
        String username,
    @Schema(description = "Password", example = "password123", required = true) @NotBlank
        String password) {}
