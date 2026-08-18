package com.finance.smartLedger.journal.application;

import com.finance.smartLedger.journal.application.dto.TrialBalanceAccountDto;
import com.finance.smartLedger.journal.application.dto.TrialBalanceResponse;
import com.finance.smartLedger.journal.domain.DebitCredit;
import com.finance.smartLedger.journal.domain.JournalEntry;
import com.finance.smartLedger.journal.domain.JournalLineItem;
import com.finance.smartLedger.journal.infrastructure.persistence.JournalEntryRepository;
import com.finance.smartLedger.journal.infrastructure.persistence.JournalLineItemRepository;
import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.Account;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TrialBalanceService {

  private final JournalEntryRepository journalEntryRepository;
  private final JournalLineItemRepository journalLineItemRepository;
  private final AccountService accountService;

  public TrialBalanceResponse generateTrialBalance(LocalDateTime asOfDate) {
    List<JournalEntry> postedEntries = journalEntryRepository.findPostedEntriesOrderByDateDesc();

    Map<UUID, TrialBalanceAccountDto> accountBalances = new HashMap<>();

    for (JournalEntry entry : postedEntries) {
      if (asOfDate != null && entry.getEntryDate().isAfter(asOfDate)) {
        continue;
      }

      for (JournalLineItem lineItem : entry.getLineItems()) {
        UUID accountId = lineItem.getAccountId();
        BigDecimal amount = lineItem.getAmount().getAmount();

        TrialBalanceAccountDto accountDto =
            accountBalances.computeIfAbsent(
                accountId,
                id -> {
                  Account account = accountService.findById(id).orElse(null);
                  if (account == null) {
                    return new TrialBalanceAccountDto(
                        id,
                        lineItem.getAccountNumber(),
                        lineItem.getAccountName(),
                        "UNKNOWN",
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        lineItem.getAmount().getCurrencyCode());
                  }
                  return new TrialBalanceAccountDto(
                      id,
                      account.getAccountNumber().getValue(),
                      account.getAccountName(),
                      account.getAccountType().name(),
                      BigDecimal.ZERO,
                      BigDecimal.ZERO,
                      account.getBalance().getCurrentBalance().getCurrencyCode());
                });

        if (lineItem.getDebitCredit() == DebitCredit.DEBIT) {
          accountDto =
              new TrialBalanceAccountDto(
                  accountDto.accountId(),
                  accountDto.accountNumber(),
                  accountDto.accountName(),
                  accountDto.accountType(),
                  accountDto.debitBalance().add(amount),
                  accountDto.creditBalance(),
                  accountDto.currency());
        } else {
          accountDto =
              new TrialBalanceAccountDto(
                  accountDto.accountId(),
                  accountDto.accountNumber(),
                  accountDto.accountName(),
                  accountDto.accountType(),
                  accountDto.debitBalance(),
                  accountDto.creditBalance().add(amount),
                  accountDto.currency());
        }

        accountBalances.put(accountId, accountDto);
      }
    }

    List<TrialBalanceAccountDto> accounts = new ArrayList<>(accountBalances.values());

    BigDecimal totalDebits =
        accounts.stream()
            .map(TrialBalanceAccountDto::debitBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCredits =
        accounts.stream()
            .map(TrialBalanceAccountDto::creditBalance)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal difference = totalDebits.subtract(totalCredits);
    boolean isBalanced = difference.compareTo(BigDecimal.ZERO) == 0;

    return new TrialBalanceResponse(
        UUID.randomUUID(),
        asOfDate != null ? asOfDate : LocalDateTime.now(),
        totalDebits,
        totalCredits,
        difference,
        isBalanced,
        accounts,
        LocalDateTime.now());
  }

  public TrialBalanceResponse generateTrialBalance() {
    return generateTrialBalance(null);
  }
}
