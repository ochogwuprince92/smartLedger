package com.finance.smartLedger.ledger.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request to partially update an account")
public record PatchAccountRequest(
    @Schema(description = "Account name") String accountName,
    @Schema(description = "Account description") String description,
    @Schema(description = "Account active status") Boolean isActive,
    @Schema(description = "User who updated the account", required = true) @NotBlank
        String updatedBy) {}
