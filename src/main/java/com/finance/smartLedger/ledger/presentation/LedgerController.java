package com.finance.smartLedger.ledger.presentation;

import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.application.BalanceService;
import com.finance.smartLedger.ledger.application.dto.*;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.domain.valueobject.AccountBalance;
import com.finance.smartLedger.shared.dto.ApiResponse;
import com.finance.smartLedger.shared.valueobject.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ledger")
@RequiredArgsConstructor
@Tag(name = "Ledger Management", description = "APIs for managing ledger accounts and balances")
public class LedgerController {

  private static final String CURRENCY_PARAMETER = "ISO-4217 currency code, e.g. NGN";

  private final AccountService accountService;
  private final BalanceService balanceService;

  @PostMapping("/accounts")
  @Operation(
      summary = "Create a new account",
      description = "Creates a new ledger account with the specified details")
  @PreAuthorize("hasAuthority('LEDGER:CREATE')")
  public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Account creation request",
          content = @Content(schema = @Schema(implementation = CreateAccountRequest.class)))
      CreateAccountRequest request) {
    Account account =
        accountService.createAccount(
            request.accountNumber(),
            request.accountCode(),
            request.accountName(),
            request.accountType(),
            request.initialBalance(),
            request.createdBy());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("Account created successfully", AccountResponse.from(account)));
  }

  @GetMapping("/accounts/{id}")
  @Operation(summary = "Get account by ID", description = "Retrieves account details by its ID")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
      @Parameter(description = "Account ID") @PathVariable UUID id) {
    Account account =
        accountService
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    return ResponseEntity.ok(ApiResponse.success(AccountResponse.from(account)));
  }

  @GetMapping("/accounts/number/{accountNumber}")
  @Operation(
      summary = "Get account by number",
      description = "Retrieves account details by account number")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
      @Parameter(description = "Account number") @PathVariable String accountNumber) {
    Account account =
        accountService
            .findByAccountNumber(accountNumber)
            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
    return ResponseEntity.ok(ApiResponse.success(AccountResponse.from(account)));
  }

  @GetMapping("/accounts")
  @Operation(summary = "Get all accounts", description = "Retrieves all ledger accounts")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts() {
    List<Account> accounts = accountService.findAllAccounts();
    List<AccountResponse> responses = accounts.stream().map(AccountResponse::from).toList();
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/accounts/type/{accountType}")
  @Operation(summary = "Get accounts by type", description = "Retrieves accounts filtered by type")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsByType(
      @Parameter(description = "Account type") @PathVariable AccountType accountType) {
    List<Account> accounts = accountService.findByAccountType(accountType);
    List<AccountResponse> responses = accounts.stream().map(AccountResponse::from).toList();
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/accounts/active")
  @Operation(summary = "Get active accounts", description = "Retrieves all active accounts")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<List<AccountResponse>>> getActiveAccounts() {
    List<Account> accounts = accountService.findActiveAccounts();
    List<AccountResponse> responses = accounts.stream().map(AccountResponse::from).toList();
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @PatchMapping("/accounts/{id}")
  @Operation(
      summary = "Partially update account",
      description = "Partially updates account details including status")
  @PreAuthorize("hasAuthority('LEDGER:UPDATE')")
  public ResponseEntity<ApiResponse<AccountResponse>> patchAccount(
      @Parameter(description = "Account ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Account patch request",
          content = @Content(schema = @Schema(implementation = PatchAccountRequest.class)))
      PatchAccountRequest request) {
    Account account =
        accountService.patchAccount(
            id,
            request.accountName(),
            request.description(),
            request.isActive(),
            request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Account updated successfully", AccountResponse.from(account)));
  }

  @PatchMapping("/accounts/{id}/balance")
  @Operation(
      summary = "Update account balance",
      description = "Updates account balance by debiting or crediting the specified amount")
  @PreAuthorize("hasAuthority('LEDGER:UPDATE')")
  public ResponseEntity<ApiResponse<AccountResponse>> updateAccountBalance(
      @Parameter(description = "Account ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Balance update request",
          content = @Content(schema = @Schema(implementation = BalanceUpdateRequest.class)))
      BalanceUpdateRequest request) {
    Account account =
        accountService.updateBalance(
            id, request.operation().name(), request.amount(), request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Balance updated successfully", AccountResponse.from(account)));
  }

  @DeleteMapping("/accounts/{id}")
  @Operation(
      summary = "Delete account",
      description = "Deletes an account (only if no child accounts exist)")
  @PreAuthorize("hasAuthority('LEDGER:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteAccount(
      @Parameter(description = "Account ID") @PathVariable UUID id) {
    accountService.deleteAccount(id);
    return ResponseEntity.ok(ApiResponse.success("Account deleted successfully", null));
  }

  @GetMapping("/accounts/{id}/balance")
  @Operation(
      summary = "Get account balance",
      description = "Retrieves the current balance of an account")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<BalanceResponse>> getAccountBalance(
      @Parameter(description = "Account ID") @PathVariable UUID id) {
    Money balance = accountService.getAccountBalance(id);
    return ResponseEntity.ok(ApiResponse.success(BalanceResponse.from(balance)));
  }

  @GetMapping("/accounts/{id}/balance/details")
  @Operation(
      summary = "Get account balance details",
      description = "Retrieves detailed balance information")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<AccountBalance>> getAccountBalanceDetails(
      @Parameter(description = "Account ID") @PathVariable UUID id) {
    AccountBalance balance = accountService.getAccountBalanceDetails(id);
    return ResponseEntity.ok(ApiResponse.success(balance));
  }

  @GetMapping("/balances/by-type")
  @Operation(
      summary = "Get balances by account type",
      description = "Retrieves total balances grouped by account type and currency")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Map<AccountType, Map<String, BalanceResponse>>>>
      getBalancesByType(
          @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
          String currency) {
    Map<AccountType, Map<String, BalanceResponse>> balances =
        balanceService.getBalancesByAccountType().entrySet().stream()
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey,
                    entry -> toBalanceResponses(entry.getValue(), currency),
                    (first, second) -> first,
                    () -> new EnumMap<>(AccountType.class)));
    return ResponseEntity.ok(ApiResponse.success(balances));
  }

  @GetMapping("/balances/assets")
  @Operation(
      summary = "Get total asset balance",
      description = "Retrieves the total balance of all asset accounts, per currency")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Map<String, BalanceResponse>>> getTotalAssetBalance(
      @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
      String currency) {
    return totalBalanceResponse(AccountType.ASSET, currency);
  }

  @GetMapping("/balances/liabilities")
  @Operation(
      summary = "Get total liability balance",
      description = "Retrieves the total balance of all liability accounts, per currency")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Map<String, BalanceResponse>>> getTotalLiabilityBalance(
      @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
      String currency) {
    return totalBalanceResponse(AccountType.LIABILITY, currency);
  }

  @GetMapping("/balances/equity")
  @Operation(
      summary = "Get total equity balance",
      description = "Retrieves the total balance of all equity accounts, per currency")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Map<String, BalanceResponse>>> getTotalEquityBalance(
      @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
      String currency) {
    return totalBalanceResponse(AccountType.EQUITY, currency);
  }

  @GetMapping("/balances/revenue")
  @Operation(
      summary = "Get total revenue balance",
      description = "Retrieves the total balance of all revenue accounts, per currency")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Map<String, BalanceResponse>>> getTotalRevenueBalance(
      @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
      String currency) {
    return totalBalanceResponse(AccountType.REVENUE, currency);
  }

  @GetMapping("/balances/expenses")
  @Operation(
      summary = "Get total expense balance",
      description = "Retrieves the total balance of all expense accounts, per currency")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Map<String, BalanceResponse>>> getTotalExpenseBalance(
      @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
      String currency) {
    return totalBalanceResponse(AccountType.EXPENSE, currency);
  }

  @GetMapping("/balances/net-income")
  @Operation(
      summary = "Get net income",
      description = "Calculates and returns the net income (revenue - expenses) per currency")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Map<String, BalanceResponse>>> getNetIncome(
      @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
      String currency) {
    Map<String, Money> netIncome =
        currency == null
            ? balanceService.getNetIncomeByCurrency()
            : Map.of(currency, balanceService.getNetIncome(currency));
    return ResponseEntity.ok(ApiResponse.success(toBalanceResponses(netIncome, null)));
  }

  private ResponseEntity<ApiResponse<Map<String, BalanceResponse>>> totalBalanceResponse(
      AccountType accountType, String currency) {
    Map<String, Money> totals =
        currency == null
            ? balanceService.getTotalBalanceByCurrency(accountType)
            : Map.of(currency, balanceService.getTotalBalance(accountType, currency));
    return ResponseEntity.ok(ApiResponse.success(toBalanceResponses(totals, null)));
  }

  private static Map<String, BalanceResponse> toBalanceResponses(
      Map<String, Money> balances, String currency) {
    return balances.entrySet().stream()
        .filter(entry -> currency == null || entry.getKey().equals(currency))
        .collect(
            Collectors.toMap(
                Map.Entry::getKey,
                entry -> BalanceResponse.from(entry.getValue()),
                (first, second) -> first,
                LinkedHashMap::new));
  }

  @PatchMapping("/accounts/{id}/balance/adjust")
  @Operation(
      summary = "Adjust account balance",
      description = "Adjusts an account balance with a specified amount")
  @PreAuthorize("hasAuthority('LEDGER:UPDATE')")
  public ResponseEntity<ApiResponse<AccountResponse>> adjustBalance(
      @Parameter(description = "Account ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Balance adjustment request",
          content = @Content(schema = @Schema(implementation = AdjustBalanceRequest.class)))
      AdjustBalanceRequest request) {
    Account account =
        balanceService.adjustBalanceWithAccount(
            id, request.amount(), request.reason(), request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Balance adjusted successfully", AccountResponse.from(account)));
  }

  @PostMapping("/balances/transfer")
  @Operation(
      summary = "Transfer balance between accounts",
      description = "Transfers balance from one account to another")
  @PreAuthorize("hasAuthority('LEDGER:UPDATE')")
  public ResponseEntity<ApiResponse<Void>> transferBalance(
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Balance transfer request",
          content = @Content(schema = @Schema(implementation = TransferBalanceRequest.class)))
      TransferBalanceRequest request) {
    balanceService.transferBalance(
        request.fromAccountId(),
        request.toAccountId(),
        request.amount(),
        request.reference(),
        request.updatedBy());
    return ResponseEntity.ok(ApiResponse.success("Balance transferred successfully", null));
  }

  @GetMapping("/balances/trial-balance")
  @Operation(
      summary = "Calculate trial balance",
      description =
          "Calculates the trial balance difference per currency (each should be zero). "
              + "Pass 'currency' to restrict the result to a single ISO-4217 currency.")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Map<String, BalanceResponse>>> calculateTrialBalance(
      @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
      String currency) {
    Map<String, Money> differences =
        currency == null
            ? balanceService.calculateTrialBalanceByCurrency()
            : Map.of(currency, balanceService.calculateTrialBalance(currency));
    return ResponseEntity.ok(ApiResponse.success(toBalanceResponses(differences, null)));
  }

  @GetMapping("/balances/trial-balanced")
  @Operation(
      summary = "Check if trial balance is balanced",
      description =
          "Returns true if the trial balance is balanced for every currency, or for the given "
              + "currency when 'currency' is supplied")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<Boolean>> isTrialBalanceBalanced(
      @Parameter(description = CURRENCY_PARAMETER) @RequestParam(required = false)
      String currency) {
    boolean balanced =
        currency == null
            ? balanceService.isTrialBalanceBalanced()
            : balanceService.isTrialBalanceBalanced(currency);
    return ResponseEntity.ok(ApiResponse.success(balanced));
  }

  @GetMapping("/accounts/negative-balance")
  @Operation(
      summary = "Get accounts with negative balance",
      description = "Retrieves all accounts with negative balances")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsWithNegativeBalance() {
    List<Account> accounts = balanceService.getAccountsWithNegativeBalance();
    List<AccountResponse> responses = accounts.stream().map(AccountResponse::from).toList();
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @GetMapping("/accounts/zero-balance")
  @Operation(
      summary = "Get accounts with zero balance",
      description = "Retrieves all accounts with zero balances")
  @PreAuthorize("hasAuthority('LEDGER:READ')")
  public ResponseEntity<ApiResponse<List<AccountResponse>>> getAccountsWithZeroBalance() {
    List<Account> accounts = balanceService.getAccountsWithZeroBalance();
    List<AccountResponse> responses = accounts.stream().map(AccountResponse::from).toList();
    return ResponseEntity.ok(ApiResponse.success(responses));
  }

  @PatchMapping("/accounts/{id}/balance/reconcile")
  @Operation(
      summary = "Reconcile account balance",
      description = "Reconciles an account balance to match expected balance")
  @PreAuthorize("hasAuthority('LEDGER:UPDATE')")
  public ResponseEntity<ApiResponse<AccountResponse>> reconcileBalance(
      @Parameter(description = "Account ID") @PathVariable UUID id,
      @RequestBody
      @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Balance reconciliation request",
          content = @Content(schema = @Schema(implementation = ReconcileBalanceRequest.class)))
      ReconcileBalanceRequest request) {
    Account account =
        balanceService.reconcileBalanceWithAccount(
            id, request.expectedBalance(), request.reason(), request.updatedBy());
    return ResponseEntity.ok(
        ApiResponse.success("Balance reconciled successfully", AccountResponse.from(account)));
  }
}
