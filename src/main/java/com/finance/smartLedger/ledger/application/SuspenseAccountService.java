package com.finance.smartLedger.ledger.application;

import com.finance.smartLedger.ledger.domain.SuspenseAccount;
import com.finance.smartLedger.ledger.domain.SuspenseAccount.SuspenseAccountStatus;
import com.finance.smartLedger.ledger.infrastructure.persistence.SuspenseAccountRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SuspenseAccountService {

  private final SuspenseAccountRepository suspenseAccountRepository;

  public SuspenseAccount createSuspenseAccount(
      String accountCode,
      String accountName,
      String description,
      String currencyCode,
      String createdBy) {

    if (suspenseAccountRepository.existsByAccountCode(accountCode)) {
      throw new IllegalArgumentException("Suspense account code already exists: " + accountCode);
    }

    SuspenseAccount account =
        SuspenseAccount.builder()
            .accountCode(accountCode)
            .accountName(accountName)
            .description(description)
            .currentBalance(BigDecimal.ZERO)
            .currencyCode(currencyCode)
            .status(SuspenseAccountStatus.ACTIVE)
            .requiresReview(false)
            .build();
    account.setCreatedBy(createdBy);

    return suspenseAccountRepository.save(account);
  }

  public Optional<SuspenseAccount> findById(UUID id) {
    return suspenseAccountRepository.findById(id);
  }

  public Optional<SuspenseAccount> findByAccountCode(String accountCode) {
    return suspenseAccountRepository.findByAccountCode(accountCode);
  }

  public List<SuspenseAccount> findByStatus(SuspenseAccountStatus status) {
    return suspenseAccountRepository.findByStatus(status);
  }

  public List<SuspenseAccount> findRequiringReview() {
    return suspenseAccountRepository.findByRequiresReviewTrue();
  }

  public List<SuspenseAccount> findActiveWithBalance() {
    return suspenseAccountRepository.findByStatusAndCurrentBalanceGreaterThan(
        SuspenseAccountStatus.ACTIVE, BigDecimal.ZERO);
  }

  @Transactional
  public SuspenseAccount addToSuspense(
      UUID accountId, BigDecimal amount, String reason, String updatedBy) {
    SuspenseAccount account =
        suspenseAccountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Suspense account not found"));

    if (account.getStatus() != SuspenseAccountStatus.ACTIVE) {
      throw new IllegalStateException("Cannot add to inactive suspense account");
    }

    account.addToBalance(amount);
    account.markForReview();
    account.setUpdatedBy(updatedBy);

    return suspenseAccountRepository.save(account);
  }

  @Transactional
  public SuspenseAccount moveFromSuspense(
      UUID suspenseAccountId,
      UUID targetAccountId,
      BigDecimal amount,
      String reason,
      String updatedBy) {

    SuspenseAccount suspenseAccount =
        suspenseAccountRepository
            .findById(suspenseAccountId)
            .orElseThrow(() -> new IllegalArgumentException("Suspense account not found"));

    if (suspenseAccount.getCurrentBalance().compareTo(amount) < 0) {
      throw new IllegalStateException("Insufficient balance in suspense account");
    }

    suspenseAccount.subtractFromBalance(amount);
    suspenseAccount.setUpdatedBy(updatedBy);

    SuspenseAccount saved = suspenseAccountRepository.save(suspenseAccount);

    // Log the transfer for audit purposes
    // In a real implementation, this would also credit the target account

    return saved;
  }

  @Transactional
  public void markAsReconciled(UUID accountId, String updatedBy) {
    SuspenseAccount account =
        suspenseAccountRepository
            .findById(accountId)
            .orElseThrow(() -> new IllegalArgumentException("Suspense account not found"));

    account.markAsReconciled(updatedBy);
    account.clearReviewFlag();
    suspenseAccountRepository.save(account);
  }

  @Transactional
  public void deleteSuspenseAccount(UUID id) {
    SuspenseAccount account =
        suspenseAccountRepository
            .findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Suspense account not found"));

    if (account.hasBalance()) {
      throw new IllegalStateException("Cannot delete suspense account with non-zero balance");
    }

    suspenseAccountRepository.deleteById(id);
  }
}
