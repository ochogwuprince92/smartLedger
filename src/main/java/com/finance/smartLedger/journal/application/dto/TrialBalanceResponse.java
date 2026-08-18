package com.finance.smartLedger.journal.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "Trial balance response")
public record TrialBalanceResponse(
    @Schema(description = "Trial balance ID") UUID id,
    @Schema(description = "As of date") LocalDateTime asOfDate,
    @Schema(description = "Total debits") BigDecimal totalDebits,
    @Schema(description = "Total credits") BigDecimal totalCredits,
    @Schema(description = "Difference") BigDecimal difference,
    @Schema(description = "Is balanced") Boolean isBalanced,
    @Schema(description = "Account balances") List<TrialBalanceAccountDto> accounts,
    @Schema(description = "Generated at") LocalDateTime generatedAt) {}
