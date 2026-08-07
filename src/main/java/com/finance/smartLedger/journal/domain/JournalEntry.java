package com.finance.smartLedger.journal.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "journal_entries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalEntry extends AuditableEntity {

  @Column(name = "entry_number", nullable = false, unique = true, length = 50)
  private String entryNumber;

  @Column(name = "entry_date", nullable = false)
  private LocalDateTime entryDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "entry_type", nullable = false, length = 20)
  private JournalEntryType entryType;

  @Column(name = "reference_number", length = 100)
  private String referenceNumber;

  @Column(name = "description", nullable = false, length = 500)
  private String description;

  @Column(name = "posted", nullable = false)
  @Builder.Default
  private Boolean posted = false;

  @Column(name = "posted_date")
  private LocalDateTime postedDate;

  @Column(name = "posted_by")
  private String postedBy;

  @OneToMany(mappedBy = "journalEntryId", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("sequenceNumber ASC")
  @Builder.Default
  private List<JournalLineItem> lineItems = new ArrayList<>();

  public JournalEntry(
      String entryNumber,
      LocalDateTime entryDate,
      JournalEntryType entryType,
      String referenceNumber,
      String description,
      String createdBy) {
    this.entryNumber = entryNumber;
    this.entryDate = entryDate;
    this.entryType = entryType;
    this.referenceNumber = referenceNumber;
    this.description = description;
    this.posted = false;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }

  public void addLineItem(JournalLineItem lineItem) {
    if (posted) {
      throw new IllegalStateException("Cannot add line items to a posted journal entry");
    }
    lineItem.setJournalEntryId(getId());
    lineItems.add(lineItem);
  }

  public void validateDoubleEntry() {
    if (lineItems == null || lineItems.isEmpty()) {
      throw new IllegalStateException("Journal entry must have at least one line item");
    }

    BigDecimal totalDebits =
        lineItems.stream()
            .filter(item -> item.getDebitCredit() == DebitCredit.DEBIT)
            .map(item -> item.getAmount().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCredits =
        lineItems.stream()
            .filter(item -> item.getDebitCredit() == DebitCredit.CREDIT)
            .map(item -> item.getAmount().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    if (totalDebits.compareTo(totalCredits) != 0) {
      throw new IllegalStateException(
          String.format(
              "Double-entry validation failed: Total debits (%s) must equal total credits (%s)",
              totalDebits, totalCredits));
    }

    if (totalDebits.compareTo(BigDecimal.ZERO) == 0) {
      throw new IllegalStateException("Journal entry cannot have zero total amount");
    }
  }

  public void post(String postedBy) {
    if (posted) {
      throw new IllegalStateException("Journal entry is already posted");
    }

    validateDoubleEntry();

    this.posted = true;
    this.postedDate = LocalDateTime.now();
    this.postedBy = postedBy;
    this.setUpdatedBy(postedBy);
  }

  public boolean isBalanced() {
    if (lineItems == null || lineItems.isEmpty()) {
      return false;
    }

    BigDecimal totalDebits =
        lineItems.stream()
            .filter(item -> item.getDebitCredit() == DebitCredit.DEBIT)
            .map(item -> item.getAmount().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal totalCredits =
        lineItems.stream()
            .filter(item -> item.getDebitCredit() == DebitCredit.CREDIT)
            .map(item -> item.getAmount().getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return totalDebits.compareTo(totalCredits) == 0 && totalDebits.compareTo(BigDecimal.ZERO) > 0;
  }

  public BigDecimal getTotalDebits() {
    return lineItems.stream()
        .filter(item -> item.getDebitCredit() == DebitCredit.DEBIT)
        .map(item -> item.getAmount().getAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  public BigDecimal getTotalCredits() {
    return lineItems.stream()
        .filter(item -> item.getDebitCredit() == DebitCredit.CREDIT)
        .map(item -> item.getAmount().getAmount())
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
