package com.finance.smartLedger.reconciliation.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "reconciliations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Reconciliation extends AuditableEntity {

  @Column(name = "reconciliation_number", nullable = false, unique = true, length = 50)
  private String reconciliationNumber;

  @Column(name = "reconciliation_date", nullable = false)
  private LocalDateTime reconciliationDate;

  @Column(name = "source_system", nullable = false, length = 50)
  private String sourceSystem;

  @Column(name = "source_reference", length = 100)
  private String sourceReference;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private ReconciliationStatus status;

  @Column(name = "total_expected_amount")
  private BigDecimal totalExpectedAmount;

  @Column(name = "total_actual_amount")
  private BigDecimal totalActualAmount;

  @Column(name = "variance_amount")
  private BigDecimal varianceAmount;

  @Column(name = "suspense_account_id")
  private UUID suspenseAccountId;

  @Column(name = "description", length = 500)
  private String description;

  @Column(name = "completed_at")
  private LocalDateTime completedAt;

  @OneToMany(mappedBy = "reconciliation", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<ReconciliationItem> items = new ArrayList<>();

  public Reconciliation(
      String reconciliationNumber,
      LocalDateTime reconciliationDate,
      String sourceSystem,
      String sourceReference,
      BigDecimal totalExpectedAmount,
      String description,
      String createdBy) {
    this.reconciliationNumber = reconciliationNumber;
    this.reconciliationDate = reconciliationDate;
    this.sourceSystem = sourceSystem;
    this.sourceReference = sourceReference;
    this.status = ReconciliationStatus.PENDING;
    this.totalExpectedAmount = totalExpectedAmount;
    this.totalActualAmount = BigDecimal.ZERO;
    this.varianceAmount = totalExpectedAmount;
    this.description = description;
    this.items = new ArrayList<>();
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }

  public void startReconciliation(String updatedBy) {
    if (status != ReconciliationStatus.PENDING) {
      throw new IllegalStateException("Can only start reconciliation in PENDING status");
    }
    this.status = ReconciliationStatus.IN_PROGRESS;
    this.setUpdatedBy(updatedBy);
  }

  public void completeReconciliation(String updatedBy) {
    if (status != ReconciliationStatus.IN_PROGRESS) {
      throw new IllegalStateException("Can only complete reconciliation in IN_PROGRESS status");
    }
    this.status = ReconciliationStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
    this.setUpdatedBy(updatedBy);
  }

  public void failReconciliation(String updatedBy) {
    this.status = ReconciliationStatus.FAILED;
    this.setUpdatedBy(updatedBy);
  }

  public void markPartiallyMatched(String updatedBy) {
    this.status = ReconciliationStatus.PARTIALLY_MATCHED;
    this.setUpdatedBy(updatedBy);
  }

  public void addItem(ReconciliationItem item) {
    item.setReconciliation(this);
    items.add(item);
  }

  public void calculateVariance() {
    BigDecimal actualTotal =
        items.stream()
            .map(ReconciliationItem::getActualAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    this.totalActualAmount = actualTotal;
    this.varianceAmount = totalExpectedAmount.subtract(actualTotal);
  }

  public boolean isBalanced() {
    return varianceAmount != null && varianceAmount.compareTo(BigDecimal.ZERO) == 0;
  }
}
