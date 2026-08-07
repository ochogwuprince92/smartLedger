package com.finance.smartLedger.reconciliation.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Schema(description = "Request for a reconciliation item")
public record ReconciliationItemRequest(
    @Schema(description = "Item reference", example = "TXN-001", required = true) @NotBlank
        String itemReference,
    @Schema(description = "Item type", example = "PAYMENT", required = true) @NotBlank
        String itemType,
    @Schema(description = "Expected amount", required = true) @NotNull BigDecimal expectedAmount,
    @Schema(description = "Description", example = "Payment from customer") String description) {}
