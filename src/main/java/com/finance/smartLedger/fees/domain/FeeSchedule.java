package com.finance.smartLedger.fees.domain;

import com.finance.smartLedger.shared.entity.AuditableEntity;
import com.finance.smartLedger.shared.valueobject.Money;
import jakarta.persistence.*;
import jakarta.persistence.AttributeOverride;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "fee_schedules",
    indexes = {
      @Index(name = "idx_schedule_academic_year", columnList = "academic_year"),
      @Index(name = "idx_schedule_class_grade", columnList = "class_grade"),
      @Index(name = "idx_schedule_status", columnList = "status")
    })
@Data
@NoArgsConstructor
@EqualsAndHashCode(
    callSuper = true,
    exclude = {"feeItems"})
public class FeeSchedule extends AuditableEntity {

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "code", nullable = false, unique = true)
  private String code;

  @Column(name = "academic_year", nullable = false)
  private String academicYear;

  @Column(name = "academic_term")
  private String academicTerm;

  @Column(name = "class_grade")
  private String classGrade;

  @Column(name = "effective_from", nullable = false)
  private LocalDate effectiveFrom;

  @Column(name = "effective_to")
  private LocalDate effectiveTo;

  @Embedded
  @AttributeOverride(name = "amount", column = @Column(name = "total_amount"))
  @AttributeOverride(name = "currencyCode", column = @Column(name = "total_amount_currency"))
  private Money totalAmount;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private ScheduleStatus status;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @OneToMany(mappedBy = "feeSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<FeeScheduleItem> feeItems = new HashSet<>();

  public FeeSchedule(String code, String name, String academicYear, String classGrade) {
    this.code = code;
    this.name = name;
    this.academicYear = academicYear;
    this.classGrade = classGrade;
    this.effectiveFrom = LocalDate.now();
    this.status = ScheduleStatus.DRAFT;
    this.totalAmount = Money.zero("USD");
  }

  public void addFeeItem(FeeType feeType, Money amount, boolean mandatory) {
    FeeScheduleItem item = new FeeScheduleItem(this, feeType, amount, mandatory);
    feeItems.add(item);
    recalculateTotal();
  }

  public void removeFeeItem(FeeScheduleItem item) {
    feeItems.remove(item);
    recalculateTotal();
  }

  public void recalculateTotal() {
    Money newTotal = Money.zero("USD");
    for (FeeScheduleItem item : feeItems) {
      newTotal = newTotal.add(item.getAmount());
    }
    this.totalAmount = newTotal;
  }

  public void activate() {
    if (status == ScheduleStatus.DRAFT) {
      this.status = ScheduleStatus.ACTIVE;
    }
  }

  public void deactivate() {
    if (status == ScheduleStatus.ACTIVE) {
      this.status = ScheduleStatus.INACTIVE;
    }
  }

  public boolean isActive() {
    return status == ScheduleStatus.ACTIVE;
  }

  public boolean isDraft() {
    return status == ScheduleStatus.DRAFT;
  }

  public boolean isEffective() {
    LocalDate now = LocalDate.now();
    return (effectiveFrom == null || !now.isBefore(effectiveFrom))
        && (effectiveTo == null || !now.isAfter(effectiveTo));
  }

  public enum ScheduleStatus {
    DRAFT,
    ACTIVE,
    INACTIVE,
    ARCHIVED
  }
}
