package com.finance.smartLedger.reconciliation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Request to create a reconciliation")
public record CreateReconciliationRequest(
    @Schema(description = "Reconciliation number", example = "REC-2024-001", required = true)
        @NotBlank
        String reconciliationNumber,
    @Schema(description = "Reconciliation date", required = true) @NotNull
        LocalDateTime reconciliationDate,
    @Schema(description = "Source system", example = "BANK", required = true) @NotBlank
        String sourceSystem,
    @Schema(description = "Source reference", example = "BANK-001") String sourceReference,
    @Schema(description = "Total expected amount", required = true) @NotNull
        BigDecimal totalExpectedAmount,
    @Schema(description = "Description", example = "Monthly bank reconciliation")
        String description) {}
