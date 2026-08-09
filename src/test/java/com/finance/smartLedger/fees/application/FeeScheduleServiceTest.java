package com.finance.smartLedger.fees.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.audit.application.AuditService;
import com.finance.smartLedger.fees.domain.FeeSchedule;
import com.finance.smartLedger.fees.domain.FeeSchedule.ScheduleStatus;
import com.finance.smartLedger.fees.domain.FeeScheduleItem;
import com.finance.smartLedger.fees.domain.FeeType;
import com.finance.smartLedger.fees.infrastructure.persistence.FeeScheduleRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FeeScheduleServiceTest {

  @Mock private FeeScheduleRepository feeScheduleRepository;
  @Mock private AuditService auditService;

  @InjectMocks private FeeScheduleService feeScheduleService;

  private UUID scheduleId;
  private String code;
  private String academicYear;

  @BeforeEach
  void setUp() {
    scheduleId = UUID.randomUUID();
    code = "FEE-2024-10";
    academicYear = "2024-2025";
  }

  @Test
  void createSchedule_ShouldCreateScheduleSuccessfully() {
    // Given
    when(feeScheduleRepository.existsByCode(code)).thenReturn(false);
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    when(feeScheduleRepository.save(any(FeeSchedule.class))).thenReturn(schedule);

    // When
    FeeSchedule result =
        feeScheduleService.createSchedule(
            code,
            "Grade 10 Fees",
            academicYear,
            "Term 1",
            "Grade 10",
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            "Standard fee schedule",
            "admin");

    // Then
    assertNotNull(result);
    verify(feeScheduleRepository).existsByCode(code);
    verify(feeScheduleRepository).save(any(FeeSchedule.class));
    verify(auditService)
        .logCreate(eq("FeeSchedule"), isNull(), anyString(), isNull(), eq("admin"));
  }

  @Test
  void createSchedule_ShouldThrowException_WhenCodeExists() {
    // Given
    when(feeScheduleRepository.existsByCode(code)).thenReturn(true);

    // When/Then
    assertThrows(
        IllegalArgumentException.class,
        () ->
            feeScheduleService.createSchedule(
                code,
                "Grade 10 Fees",
                academicYear,
                "Term 1",
                "Grade 10",
                LocalDate.now(),
                LocalDate.now().plusMonths(12),
                "Standard fee schedule",
                "admin"));
  }

  @Test
  void addFeeItem_ShouldAddFeeItemSuccessfully() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(feeScheduleRepository.save(any(FeeSchedule.class))).thenReturn(schedule);

    // When
    FeeSchedule result =
        feeScheduleService.addFeeItem(
            scheduleId,
            FeeType.TUITION_FEE,
            Money.of(new BigDecimal("5000.00"), "USD"),
            true,
            "Standard tuition",
            "admin");

    // Then
    assertNotNull(result);
    assertEquals(1, schedule.getFeeItems().size());
    verify(feeScheduleRepository).save(any(FeeSchedule.class));
    verify(auditService)
        .logUpdate(
            eq("FeeSchedule"),
            isNull(),
            anyString(),
            isNull(),
            isNull(),
            anyString(),
            eq("admin"));
  }

  @Test
  void addFeeItem_ShouldThrowException_WhenScheduleNotDraft() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    schedule.activate();
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

    // When/Then
    assertThrows(
        IllegalStateException.class,
        () ->
            feeScheduleService.addFeeItem(
                scheduleId,
                FeeType.TUITION_FEE,
                Money.of(new BigDecimal("5000.00"), "USD"),
                true,
                "Standard tuition",
                "admin"));
  }

  @Test
  @Disabled("Requires JPA persistence - FeeScheduleItem ID is null until persisted")
  void removeFeeItem_ShouldRemoveFeeItemSuccessfully() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    schedule.setId(scheduleId);
    schedule.addFeeItem(FeeType.TUITION_FEE, Money.of(new BigDecimal("5000.00"), "USD"), true);
    FeeScheduleItem item = schedule.getFeeItems().iterator().next();
    UUID itemId = item.getId();

    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(feeScheduleRepository.save(any(FeeSchedule.class))).thenReturn(schedule);

    // When
    FeeSchedule result = feeScheduleService.removeFeeItem(scheduleId, itemId, "admin");

    // Then
    assertNotNull(result);
    assertEquals(0, schedule.getFeeItems().size());
    verify(feeScheduleRepository).save(any(FeeSchedule.class));
    verify(auditService)
        .logUpdate(
            eq("FeeSchedule"),
            eq(scheduleId),
            anyString(),
            isNull(),
            isNull(),
            anyString(),
            eq("admin"));
  }

  @Test
  void activateSchedule_ShouldActivateScheduleSuccessfully() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    schedule.addFeeItem(FeeType.TUITION_FEE, Money.of(new BigDecimal("5000.00"), "USD"), true);
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(feeScheduleRepository.save(any(FeeSchedule.class))).thenReturn(schedule);

    // When
    FeeSchedule result = feeScheduleService.activateSchedule(scheduleId, "admin");

    // Then
    assertNotNull(result);
    assertTrue(schedule.isActive());
    verify(feeScheduleRepository).save(any(FeeSchedule.class));
    verify(auditService)
        .logStatusChange(
            eq("FeeSchedule"),
            isNull(),
            anyString(),
            eq("DRAFT"),
            eq("ACTIVE"),
            eq("admin"));
  }

  @Test
  void activateSchedule_ShouldThrowException_WhenScheduleNotDraft() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    schedule.activate();
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

    // When/Then
    assertThrows(
        IllegalStateException.class,
        () -> feeScheduleService.activateSchedule(scheduleId, "admin"));
  }

  @Test
  void activateSchedule_ShouldThrowException_WhenNoFeeItems() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

    // When/Then
    assertThrows(
        IllegalStateException.class,
        () -> feeScheduleService.activateSchedule(scheduleId, "admin"));
  }

  @Test
  void deactivateSchedule_ShouldDeactivateScheduleSuccessfully() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    schedule.activate();
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(feeScheduleRepository.save(any(FeeSchedule.class))).thenReturn(schedule);

    // When
    FeeSchedule result = feeScheduleService.deactivateSchedule(scheduleId, "admin");

    // Then
    assertNotNull(result);
    assertEquals(ScheduleStatus.INACTIVE, schedule.getStatus());
    verify(feeScheduleRepository).save(any(FeeSchedule.class));
    verify(auditService)
        .logStatusChange(
            eq("FeeSchedule"),
            isNull(),
            anyString(),
            eq("ACTIVE"),
            eq("INACTIVE"),
            eq("admin"));
  }

  @Test
  void updateSchedule_ShouldUpdateScheduleSuccessfully() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    when(feeScheduleRepository.save(any(FeeSchedule.class))).thenReturn(schedule);

    // When
    FeeSchedule result =
        feeScheduleService.updateSchedule(
            scheduleId,
            "Updated Name",
            "Term 2",
            "Grade 11",
            LocalDate.now(),
            LocalDate.now().plusMonths(12),
            "Updated description",
            "admin");

    // Then
    assertNotNull(result);
    assertEquals("Updated Name", schedule.getName());
    assertEquals("Term 2", schedule.getAcademicTerm());
    verify(feeScheduleRepository).save(any(FeeSchedule.class));
    verify(auditService)
        .logUpdate(
            eq("FeeSchedule"),
            isNull(),
            anyString(),
            isNull(),
            isNull(),
            isNull(),
            eq("admin"));
  }

  @Test
  void updateSchedule_ShouldThrowException_WhenScheduleNotDraft() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    schedule.activate();
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

    // When/Then
    assertThrows(
        IllegalStateException.class,
        () ->
            feeScheduleService.updateSchedule(
                scheduleId,
                "Updated Name",
                "Term 2",
                "Grade 11",
                LocalDate.now(),
                LocalDate.now().plusMonths(12),
                "Updated description",
                "admin"));
  }

  @Test
  void deleteSchedule_ShouldDeleteScheduleSuccessfully() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));
    doNothing().when(feeScheduleRepository).delete(any(FeeSchedule.class));

    // When
    feeScheduleService.deleteSchedule(scheduleId, "admin");

    // Then
    verify(feeScheduleRepository).delete(any(FeeSchedule.class));
    verify(auditService)
        .logDelete(eq("FeeSchedule"), eq(scheduleId), anyString(), isNull(), eq("admin"));
  }

  @Test
  void deleteSchedule_ShouldThrowException_WhenScheduleActive() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    schedule.activate();
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

    // When/Then
    assertThrows(
        IllegalStateException.class, () -> feeScheduleService.deleteSchedule(scheduleId, "admin"));
  }

  @Test
  void getSchedule_ShouldReturnSchedule() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    schedule.setId(scheduleId);
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.of(schedule));

    // When
    FeeSchedule result = feeScheduleService.getSchedule(scheduleId);

    // Then
    assertNotNull(result);
    assertEquals(scheduleId, result.getId());
  }

  @Test
  void getSchedule_ShouldThrowException_WhenNotFound() {
    // Given
    when(feeScheduleRepository.findById(scheduleId)).thenReturn(Optional.empty());

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> feeScheduleService.getSchedule(scheduleId));
  }

  @Test
  void getScheduleByCode_ShouldReturnSchedule() {
    // Given
    FeeSchedule schedule = new FeeSchedule(code, "Grade 10 Fees", academicYear, "Grade 10");
    when(feeScheduleRepository.findByCode(code)).thenReturn(Optional.of(schedule));

    // When
    FeeSchedule result = feeScheduleService.getScheduleByCode(code);

    // Then
    assertNotNull(result);
    assertEquals(code, result.getCode());
  }

  @Test
  void getScheduleByCode_ShouldThrowException_WhenNotFound() {
    // Given
    when(feeScheduleRepository.findByCode(code)).thenReturn(Optional.empty());

    // When/Then
    assertThrows(IllegalArgumentException.class, () -> feeScheduleService.getScheduleByCode(code));
  }

  @Test
  void getSchedulesByAcademicYear_ShouldReturnSchedules() {
    // Given
    when(feeScheduleRepository.findByAcademicYearOrderByClassGrade(academicYear))
        .thenReturn(java.util.List.of());

    // When
    var result = feeScheduleService.getSchedulesByAcademicYear(academicYear);

    // Then
    assertNotNull(result);
    verify(feeScheduleRepository).findByAcademicYearOrderByClassGrade(academicYear);
  }

  @Test
  void getActiveSchedulesByAcademicYear_ShouldReturnActiveSchedules() {
    // Given
    when(feeScheduleRepository.findActiveByAcademicYear(academicYear))
        .thenReturn(java.util.List.of());

    // When
    var result = feeScheduleService.getActiveSchedulesByAcademicYear(academicYear);

    // Then
    assertNotNull(result);
    verify(feeScheduleRepository).findActiveByAcademicYear(academicYear);
  }
}
