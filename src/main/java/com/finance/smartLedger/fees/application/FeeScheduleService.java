package com.finance.smartLedger.fees.application;

import com.finance.smartLedger.audit.application.AuditService;
import com.finance.smartLedger.fees.domain.FeeSchedule;
import com.finance.smartLedger.fees.domain.FeeSchedule.ScheduleStatus;
import com.finance.smartLedger.fees.domain.FeeScheduleItem;
import com.finance.smartLedger.fees.domain.FeeType;
import com.finance.smartLedger.fees.infrastructure.persistence.FeeScheduleRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FeeScheduleService {

  private final FeeScheduleRepository feeScheduleRepository;
  private final AuditService auditService;

  @Transactional
  public FeeSchedule createSchedule(
      String code,
      String name,
      String academicYear,
      String academicTerm,
      String classGrade,
      LocalDate effectiveFrom,
      LocalDate effectiveTo,
      String description,
      String createdBy) {
    if (feeScheduleRepository.existsByCode(code)) {
      throw new IllegalArgumentException("Fee schedule code already exists: " + code);
    }

    FeeSchedule schedule = new FeeSchedule(code, name, academicYear, classGrade);
    schedule.setAcademicTerm(academicTerm);
    schedule.setEffectiveFrom(effectiveFrom);
    schedule.setEffectiveTo(effectiveTo);
    schedule.setDescription(description);
    schedule.setCreatedBy(createdBy);
    schedule.setUpdatedBy(createdBy);

    FeeSchedule savedSchedule = feeScheduleRepository.save(schedule);
    auditService.logCreate(
        "FeeSchedule", savedSchedule.getId(), "Fee schedule created", null, createdBy);

    return savedSchedule;
  }

  @Transactional
  public FeeSchedule addFeeItem(
      UUID scheduleId,
      FeeType feeType,
      Money amount,
      boolean mandatory,
      String description,
      String updatedBy) {
    FeeSchedule schedule =
        feeScheduleRepository
            .findById(scheduleId)
            .orElseThrow(
                () -> new IllegalArgumentException("Fee schedule not found: " + scheduleId));

    if (!schedule.isDraft()) {
      throw new IllegalStateException("Cannot add fee items to non-draft schedule");
    }

    schedule.addFeeItem(feeType, amount, mandatory);
    schedule.setUpdatedBy(updatedBy);

    FeeSchedule savedSchedule = feeScheduleRepository.save(schedule);
    auditService.logUpdate(
        "FeeSchedule",
        savedSchedule.getId(),
        "Fee item added: " + feeType.getDisplayName(),
        null,
        null,
        "feeItems",
        updatedBy);

    return savedSchedule;
  }

  @Transactional
  public FeeSchedule removeFeeItem(UUID scheduleId, UUID feeItemId, String updatedBy) {
    FeeSchedule schedule =
        feeScheduleRepository
            .findById(scheduleId)
            .orElseThrow(
                () -> new IllegalArgumentException("Fee schedule not found: " + scheduleId));

    if (!schedule.isDraft()) {
      throw new IllegalStateException("Cannot remove fee items from non-draft schedule");
    }

    FeeScheduleItem itemToRemove =
        schedule.getFeeItems().stream()
            .filter(fi -> fi.getId().equals(feeItemId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Fee item not found: " + feeItemId));

    schedule.removeFeeItem(itemToRemove);
    schedule.setUpdatedBy(updatedBy);

    FeeSchedule savedSchedule = feeScheduleRepository.save(schedule);
    auditService.logUpdate(
        "FeeSchedule",
        savedSchedule.getId(),
        "Fee item removed: " + feeItemId,
        null,
        null,
        "feeItems",
        updatedBy);

    return savedSchedule;
  }

  @Transactional
  public FeeSchedule activateSchedule(UUID scheduleId, String updatedBy) {
    FeeSchedule schedule =
        feeScheduleRepository
            .findById(scheduleId)
            .orElseThrow(
                () -> new IllegalArgumentException("Fee schedule not found: " + scheduleId));

    if (!schedule.isDraft()) {
      throw new IllegalStateException("Only draft schedules can be activated");
    }

    if (schedule.getFeeItems().isEmpty()) {
      throw new IllegalStateException("Cannot activate schedule with no fee items");
    }

    schedule.activate();
    schedule.setUpdatedBy(updatedBy);

    FeeSchedule savedSchedule = feeScheduleRepository.save(schedule);
    auditService.logStatusChange(
        "FeeSchedule",
        savedSchedule.getId(),
        "Fee schedule activated",
        "DRAFT",
        "ACTIVE",
        updatedBy);

    return savedSchedule;
  }

  @Transactional
  public FeeSchedule deactivateSchedule(UUID scheduleId, String updatedBy) {
    FeeSchedule schedule =
        feeScheduleRepository
            .findById(scheduleId)
            .orElseThrow(
                () -> new IllegalArgumentException("Fee schedule not found: " + scheduleId));

    schedule.deactivate();
    schedule.setUpdatedBy(updatedBy);

    FeeSchedule savedSchedule = feeScheduleRepository.save(schedule);
    auditService.logStatusChange(
        "FeeSchedule",
        savedSchedule.getId(),
        "Fee schedule deactivated",
        "ACTIVE",
        "INACTIVE",
        updatedBy);

    return savedSchedule;
  }

  @Transactional
  public FeeSchedule updateSchedule(
      UUID scheduleId,
      String name,
      String academicTerm,
      String classGrade,
      LocalDate effectiveFrom,
      LocalDate effectiveTo,
      String description,
      String updatedBy) {
    FeeSchedule schedule =
        feeScheduleRepository
            .findById(scheduleId)
            .orElseThrow(
                () -> new IllegalArgumentException("Fee schedule not found: " + scheduleId));

    if (!schedule.isDraft()) {
      throw new IllegalStateException("Cannot update non-draft schedule");
    }

    schedule.setName(name);
    schedule.setAcademicTerm(academicTerm);
    schedule.setClassGrade(classGrade);
    schedule.setEffectiveFrom(effectiveFrom);
    schedule.setEffectiveTo(effectiveTo);
    schedule.setDescription(description);
    schedule.setUpdatedBy(updatedBy);

    FeeSchedule savedSchedule = feeScheduleRepository.save(schedule);
    auditService.logUpdate(
        "FeeSchedule", savedSchedule.getId(), "Fee schedule updated", null, null, null, updatedBy);

    return savedSchedule;
  }

  @Transactional
  public void deleteSchedule(UUID scheduleId, String deletedBy) {
    FeeSchedule schedule =
        feeScheduleRepository
            .findById(scheduleId)
            .orElseThrow(
                () -> new IllegalArgumentException("Fee schedule not found: " + scheduleId));

    if (schedule.isActive()) {
      throw new IllegalStateException("Cannot delete active schedule");
    }

    feeScheduleRepository.delete(schedule);
    auditService.logDelete("FeeSchedule", scheduleId, "Fee schedule deleted", null, deletedBy);
  }

  public FeeSchedule getSchedule(UUID scheduleId) {
    return feeScheduleRepository
        .findById(scheduleId)
        .orElseThrow(() -> new IllegalArgumentException("Fee schedule not found: " + scheduleId));
  }

  public FeeSchedule getScheduleByCode(String code) {
    return feeScheduleRepository
        .findByCode(code)
        .orElseThrow(() -> new IllegalArgumentException("Fee schedule not found: " + code));
  }

  public List<FeeSchedule> getSchedulesByAcademicYear(String academicYear) {
    return feeScheduleRepository.findByAcademicYearOrderByClassGrade(academicYear);
  }

  public List<FeeSchedule> getSchedulesByClassGrade(String classGrade) {
    return feeScheduleRepository.findByClassGrade(classGrade);
  }

  public List<FeeSchedule> getActiveSchedulesByAcademicYear(String academicYear) {
    return feeScheduleRepository.findActiveByAcademicYear(academicYear);
  }

  public List<FeeSchedule> getEffectiveSchedules(LocalDate date) {
    return feeScheduleRepository.findActiveEffectiveSchedules(date);
  }

  public List<FeeSchedule> getAllSchedules() {
    return feeScheduleRepository.findAll();
  }

  public List<FeeSchedule> getSchedulesByStatus(ScheduleStatus status) {
    return feeScheduleRepository.findByStatus(status);
  }
}
