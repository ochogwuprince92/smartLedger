package com.finance.smartLedger.reconciliation.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reconciliation_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReconciliationItem extends AuditableEntity {

  @Column(name = "reconciliation_id", nullable = false)
  private UUID reconciliationId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reconciliation_id", insertable = false, updatable = false)
  private Reconciliation reconciliation;

  @Column(name = "item_reference", nullable = false, length = 100)
  private String itemReference;

  @Column(name = "item_type", nullable = false, length = 50)
  private String itemType;

  @Column(name = "expected_amount", nullable = false)
  private BigDecimal expectedAmount;

  @Column(name = "actual_amount")
  private BigDecimal actualAmount;

  @Column(name = "variance_amount")
  private BigDecimal varianceAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "match_status", length = 20)
  private MatchStatus matchStatus;

  @Column(name = "matched_transaction_id")
  private UUID matchedTransactionId;

  @Column(name = "matched_at")
  private LocalDateTime matchedAt;

  @Column(name = "description", length = 500)
  private String description;

  public ReconciliationItem(
      UUID reconciliationId,
      String itemReference,
      String itemType,
      BigDecimal expectedAmount,
      String description,
      String createdBy) {
    this.reconciliationId = reconciliationId;
    this.itemReference = itemReference;
    this.itemType = itemType;
    this.expectedAmount = expectedAmount;
    this.actualAmount = BigDecimal.ZERO;
    this.varianceAmount = expectedAmount;
    this.matchStatus = MatchStatus.UNMATCHED;
    this.description = description;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }

  public void match(UUID transactionId, BigDecimal actualAmount, String updatedBy) {
    this.matchedTransactionId = transactionId;
    this.actualAmount = actualAmount;
    this.varianceAmount = expectedAmount.subtract(actualAmount);
    this.matchStatus = MatchStatus.MATCHED;
    this.matchedAt = LocalDateTime.now();
    this.setUpdatedBy(updatedBy);
  }

  public void markUnmatched(String updatedBy) {
    this.matchStatus = MatchStatus.UNMATCHED;
    this.matchedTransactionId = null;
    this.actualAmount = BigDecimal.ZERO;
    this.varianceAmount = expectedAmount;
    this.matchedAt = null;
    this.setUpdatedBy(updatedBy);
  }

  public void moveToSuspense(UUID suspenseAccountId, String updatedBy) {
    this.matchStatus = MatchStatus.SUSPENSE;
    this.setUpdatedBy(updatedBy);
  }

  public boolean isMatched() {
    return matchStatus == MatchStatus.MATCHED;
  }

  public boolean hasVariance() {
    return varianceAmount != null && varianceAmount.compareTo(BigDecimal.ZERO) != 0;
  }
}
