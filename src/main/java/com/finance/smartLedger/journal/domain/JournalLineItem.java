package com.finance.smartLedger.journal.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "journal_line_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class JournalLineItem extends AuditableEntity {

  @Column(name = "journal_entry_id", nullable = false)
  private UUID journalEntryId;

  @Column(name = "account_id", nullable = false)
  private UUID accountId;

  @Column(name = "account_number", nullable = false, length = 50)
  private String accountNumber;

  @Column(name = "account_name", nullable = false, length = 100)
  private String accountName;

  @Enumerated(EnumType.STRING)
  @Column(name = "debit_credit", nullable = false, length = 10)
  private DebitCredit debitCredit;

  @Embedded private Money amount;

  @Column(name = "description", length = 255)
  private String description;

  @Column(name = "sequence_number", nullable = false)
  private Integer sequenceNumber;

  public JournalLineItem(
      UUID journalEntryId,
      UUID accountId,
      String accountNumber,
      String accountName,
      DebitCredit debitCredit,
      Money amount,
      String description,
      Integer sequenceNumber,
      String createdBy) {
    this.journalEntryId = journalEntryId;
    this.accountId = accountId;
    this.accountNumber = accountNumber;
    this.accountName = accountName;
    this.debitCredit = debitCredit;
    this.amount = amount;
    this.description = description;
    this.sequenceNumber = sequenceNumber;
    this.setCreatedBy(createdBy);
    this.setUpdatedBy(createdBy);
  }
}
