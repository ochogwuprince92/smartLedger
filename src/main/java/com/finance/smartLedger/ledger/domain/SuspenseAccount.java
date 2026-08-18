package com.finance.smartLedger.ledger.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "suspense_accounts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SuspenseAccount extends AuditableEntity {

  @Column(name = "account_code", nullable = false, unique = true, length = 20)
  private String accountCode;

  @Column(name = "account_name", nullable = false, length = 100)
  private String accountName;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Column(name = "current_balance", nullable = false)
  private BigDecimal currentBalance;

  @Column(name = "currency_code", nullable = false, length = 3)
  private String currencyCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private SuspenseAccountStatus status = SuspenseAccountStatus.ACTIVE;

  @Column(name = "last_reconciled_at")
  private LocalDateTime lastReconciledAt;

  @Column(name = "requires_review", nullable = false)
  @Builder.Default
  private Boolean requiresReview = false;

  public void addToBalance(BigDecimal amount) {
    this.currentBalance = this.currentBalance.add(amount);
    this.setUpdatedAt(LocalDateTime.now());
  }

  public void subtractFromBalance(BigDecimal amount) {
    this.currentBalance = this.currentBalance.subtract(amount);
    this.setUpdatedAt(LocalDateTime.now());
  }

  public void markForReview() {
    this.requiresReview = true;
    this.setUpdatedAt(LocalDateTime.now());
  }

  public void clearReviewFlag() {
    this.requiresReview = false;
    this.setUpdatedAt(LocalDateTime.now());
  }

  public void markAsReconciled(String updatedBy) {
    this.lastReconciledAt = LocalDateTime.now();
    this.setUpdatedBy(updatedBy);
  }

  public boolean hasBalance() {
    return currentBalance.compareTo(BigDecimal.ZERO) > 0;
  }

  public enum SuspenseAccountStatus {
    ACTIVE,
    INACTIVE,
    LOCKED
  }
}
