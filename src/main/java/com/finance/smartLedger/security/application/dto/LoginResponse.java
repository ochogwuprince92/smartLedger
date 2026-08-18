package com.finance.smartLedger.security.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

@Schema(description = "Login response with JWT token")
public record LoginResponse(
    @Schema(description = "JWT authentication token") String token,
    @Schema(description = "User ID") UUID userId,
    @Schema(description = "Username") String username,
    @Schema(description = "User email") String email) {}
