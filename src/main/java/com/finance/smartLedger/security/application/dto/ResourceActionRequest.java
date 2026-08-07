package com.finance.smartLedger.security.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Resource-action request for permission creation")
public record ResourceActionRequest(
    @Schema(description = "Resource name", example = "account", required = true) @NotBlank
        String resource,
    @Schema(description = "Action name", example = "read", required = true) @NotBlank
        String action) {}
