package com.finance.smartLedger.journal.presentation;

import com.finance.smartLedger.journal.application.JournalEntryService;
import com.finance.smartLedger.journal.application.TrialBalanceService;
import com.finance.smartLedger.journal.application.dto.*;
import com.finance.smartLedger.journal.domain.JournalEntry;
import com.finance.smartLedger.journal.domain.JournalEntryType;
import com.finance.smartLedger.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/journal")
@RequiredArgsConstructor
@Tag(name = "Journal Entry", description = "Journal entry management endpoints")
public class JournalController {

  private final JournalEntryService journalEntryService;
  private final TrialBalanceService trialBalanceService;

  @PostMapping("/entries")
  @Operation(
      summary = "Create journal entry",
      description = "Creates a new journal entry with line items")
  @PreAuthorize("hasAuthority('JOURNAL:CREATE')")
  public ResponseEntity<ApiResponse<JournalEntryResponse>> createJournalEntry(
      @RequestBody @Valid CreateJournalEntryRequest request) {
    JournalEntry journalEntry =
        journalEntryService.createJournalEntry(
            request.entryNumber(),
            request.entryDate(),
            request.entryType().toDomain(),
            request.referenceNumber(),
            request.description(),
            "system");

    for (JournalLineItemRequest lineItemRequest : request.lineItems()) {
      journalEntryService.addLineItem(
          journalEntry.getId(),
          lineItemRequest.accountId(),
          lineItemRequest.debitCredit().toDomain(),
          lineItemRequest.amount(),
          lineItemRequest.description(),
          "system");
    }

    // Reload the journal entry with lineItems to ensure they are properly loaded
    journalEntry =
        journalEntryService
            .findByIdWithLineItems(journalEntry.getId())
            .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));

    return ResponseEntity.status(HttpStatus.CREATED)
        .body(
            ApiResponse.success(
                "Journal entry created successfully", JournalEntryResponse.from(journalEntry)));
  }

  @PostMapping("/entries/{id}/post")
  @Operation(
      summary = "Post journal entry",
      description = "Posts a journal entry and updates account balances")
  @PreAuthorize("hasAuthority('JOURNAL:POST')")
  public ResponseEntity<ApiResponse<JournalEntryResponse>> postJournalEntry(
      @Parameter(description = "Journal entry ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "User posting the entry",
          content = @Content(schema = @Schema(implementation = PostRequest.class)))
      PostRequest request) {
    JournalEntry postedEntry = journalEntryService.postJournalEntry(id, request.postedBy());
    return ResponseEntity.ok(
        ApiResponse.success(
            "Journal entry posted successfully", JournalEntryResponse.from(postedEntry)));
  }

  @GetMapping("/entries/{id}")
  @Operation(
      summary = "Get journal entry by ID",
      description = "Retrieves a journal entry by its ID")
  @PreAuthorize("hasAuthority('JOURNAL:READ')")
  public ResponseEntity<ApiResponse<JournalEntryResponse>> getJournalEntry(
      @Parameter(description = "Journal entry ID") @PathVariable UUID id) {
    JournalEntry journalEntry =
        journalEntryService
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));
    return ResponseEntity.ok(
        ApiResponse.success(
            "Journal entry retrieved successfully", JournalEntryResponse.from(journalEntry)));
  }

  @GetMapping("/entries/number/{entryNumber}")
  @Operation(
      summary = "Get journal entry by number",
      description = "Retrieves a journal entry by its entry number")
  @PreAuthorize("hasAuthority('JOURNAL:READ')")
  public ResponseEntity<ApiResponse<JournalEntryResponse>> getJournalEntryByNumber(
      @Parameter(description = "Entry number") @PathVariable String entryNumber) {
    JournalEntry journalEntry =
        journalEntryService
            .findByEntryNumber(entryNumber)
            .orElseThrow(() -> new IllegalArgumentException("Journal entry not found"));
    return ResponseEntity.ok(
        ApiResponse.success(
            "Journal entry retrieved successfully", JournalEntryResponse.from(journalEntry)));
  }

  @GetMapping("/entries")
  @Operation(
      summary = "List journal entries",
      description = "Lists all journal entries with optional filters")
  @PreAuthorize("hasAuthority('JOURNAL:READ')")
  public ResponseEntity<ApiResponse<List<JournalEntryResponse>>> listJournalEntries(
      @Parameter(description = "Filter by entry type") @RequestParam(required = false)
          JournalEntryType entryType,
      @Parameter(description = "Filter by posted status") @RequestParam(required = false)
          Boolean posted,
      @Parameter(description = "Filter by start date") @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Filter by end date") @RequestParam(required = false)
          LocalDateTime endDate) {
    List<JournalEntry> entries;

    if (entryType != null) {
      entries = journalEntryService.findByEntryType(entryType);
    } else if (posted != null) {
      entries = journalEntryService.findByPosted(posted);
    } else if (startDate != null && endDate != null) {
      entries = journalEntryService.findByEntryDateBetween(startDate, endDate);
    } else {
      entries = journalEntryService.findPostedEntriesOrderByDateDesc();
    }

    List<JournalEntryResponse> responses =
        entries.stream().map(JournalEntryResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/transactions")
  @Operation(
      summary = "List transactions by account",
      description = "Lists all journal entries/transactions for a specific account")
  @PreAuthorize("hasAuthority('JOURNAL:READ')")
  public ResponseEntity<ApiResponse<List<JournalEntryResponse>>> listTransactionsByAccount(
      @Parameter(description = "Account ID") @RequestParam UUID accountId,
      @Parameter(description = "Filter by start date") @RequestParam(required = false)
          LocalDateTime startDate,
      @Parameter(description = "Filter by end date") @RequestParam(required = false)
          LocalDateTime endDate) {
    List<JournalEntry> entries;
    
    if (startDate != null && endDate != null) {
      entries = journalEntryService.findByAccountIdAndDateBetween(accountId, startDate, endDate);
    } else {
      entries = journalEntryService.findByAccountId(accountId);
    }

    List<JournalEntryResponse> responses =
        entries.stream().map(JournalEntryResponse::from).collect(Collectors.toList());
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @DeleteMapping("/entries/{id}")
  @Operation(summary = "Delete journal entry", description = "Deletes an unposted journal entry")
  @PreAuthorize("hasAuthority('JOURNAL:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteJournalEntry(
      @Parameter(description = "Journal entry ID") @PathVariable UUID id) {
    journalEntryService.deleteJournalEntry(id);
    return ResponseEntity.ok(ApiResponse.success("Journal entry deleted successfully", null));
  }

  @GetMapping("/trial-balance")
  @Operation(
      summary = "Generate trial balance",
      description = "Generates a trial balance as of a specific date")
  @PreAuthorize("hasAuthority('JOURNAL:READ')")
  public ResponseEntity<ApiResponse<TrialBalanceResponse>> generateTrialBalance(
      @Parameter(description = "As of date") @RequestParam(required = false)
          LocalDateTime asOfDate) {
    TrialBalanceResponse trialBalance =
        asOfDate != null
            ? trialBalanceService.generateTrialBalance(asOfDate)
            : trialBalanceService.generateTrialBalance();
    return ResponseEntity.ok(
        ApiResponse.success("Trial balance generated successfully", trialBalance));
  }

  public record PostRequest(@Schema(description = "User posting the entry") String postedBy) {}
}
