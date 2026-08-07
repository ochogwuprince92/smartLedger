package com.finance.smartLedger.ledger.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "transactions",
    indexes = {
      @Index(name = "idx_transaction_type", columnList = "transaction_type"),
      @Index(name = "idx_transaction_date", columnList = "transaction_date"),
      @Index(name = "idx_reference_number", columnList = "reference_number")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class Transaction extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  @Column(name = "transaction_type", nullable = false, length = 50)
  private TransactionType type;

  @Column(name = "description", nullable = false, columnDefinition = "TEXT")
  private String description;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "amount", nullable = false))
  private Money amount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "debit_account_id", nullable = false)
  private Account debitAccount;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "credit_account_id", nullable = false)
  private Account creditAccount;

  @Column(name = "reference_number", length = 100)
  private String referenceNumber;

  @Column(name = "transaction_date", nullable = false)
  private LocalDateTime transactionDate;

  @Column(name = "is_posted", nullable = false)
  @Builder.Default
  private Boolean isPosted = false;

  @Column(name = "posted_date")
  private LocalDateTime postedDate;

  @PrePersist
  protected void onCreate() {
    if (transactionDate == null) {
      transactionDate = LocalDateTime.now();
    }
  }

  public void post() {
    if (isPosted) {
      throw new IllegalStateException("Transaction is already posted");
    }
    this.isPosted = true;
    this.postedDate = LocalDateTime.now();
  }

  public void reverse(String reason) {
    if (!isPosted) {
      throw new IllegalStateException("Cannot reverse an unposted transaction");
    }
    this.isPosted = false;
    this.description = "REVERSED: " + this.description + " - Reason: " + reason;
  }
}
