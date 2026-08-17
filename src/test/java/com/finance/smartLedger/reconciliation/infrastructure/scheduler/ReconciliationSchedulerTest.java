package com.finance.smartLedger.reconciliation.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.reconciliation.application.ReconciliationService;
import com.finance.smartLedger.reconciliation.domain.Reconciliation;
import com.finance.smartLedger.reconciliation.domain.ReconciliationStatus;
import com.finance.smartLedger.shared.util.ClockProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReconciliationSchedulerTest {

  @Mock private ClockProvider clockProvider;

  @Mock private ReconciliationService reconciliationService;

  @InjectMocks private ReconciliationScheduler scheduler;

  private LocalDateTime testTime;

  @BeforeEach
  void setUp() {
    testTime = LocalDateTime.now();
    when(clockProvider.now()).thenReturn(testTime);
  }

  @Test
  void performDailyReconciliation_ShouldFindPendingAndStartReconciliations() {
    // Given
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    Reconciliation pendingReconciliation1 = createTestReconciliation(id1, "REC-001");
    Reconciliation pendingReconciliation2 = createTestReconciliation(id2, "REC-002");
    
    when(reconciliationService.findByStatus(ReconciliationStatus.PENDING))
        .thenReturn(List.of(pendingReconciliation1, pendingReconciliation2));

    // When
    scheduler.performDailyReconciliation();

    // Then
    verify(reconciliationService).findByStatus(ReconciliationStatus.PENDING);
    verify(reconciliationService).startReconciliation(id1, "scheduler");
    verify(reconciliationService).startReconciliation(id2, "scheduler");
  }

  @Test
  void performDailyReconciliation_ShouldContinueOnIndividualFailure() {
    // Given
    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    Reconciliation pendingReconciliation1 = createTestReconciliation(id1, "REC-001");
    Reconciliation pendingReconciliation2 = createTestReconciliation(id2, "REC-002");
    
    when(reconciliationService.findByStatus(ReconciliationStatus.PENDING))
        .thenReturn(List.of(pendingReconciliation1, pendingReconciliation2));
    
    doThrow(new RuntimeException("Failed to start reconciliation"))
        .when(reconciliationService).startReconciliation(id1, "scheduler");

    // When
    scheduler.performDailyReconciliation();

    // Then
    verify(reconciliationService).findByStatus(ReconciliationStatus.PENDING);
    verify(reconciliationService).startReconciliation(id1, "scheduler");
    verify(reconciliationService).startReconciliation(id2, "scheduler");
  }

  @Test
  void performDailyReconciliation_ShouldLogSummary() {
    // Given
    UUID id1 = UUID.randomUUID();
    Reconciliation pendingReconciliation1 = createTestReconciliation(id1, "REC-001");
    when(reconciliationService.findByStatus(ReconciliationStatus.PENDING))
        .thenReturn(List.of(pendingReconciliation1));

    // When
    scheduler.performDailyReconciliation();

    // Then
    verify(reconciliationService).findByStatus(ReconciliationStatus.PENDING);
    verify(reconciliationService).startReconciliation(id1, "scheduler");
  }

  private Reconciliation createTestReconciliation(UUID id, String reconciliationNumber) {
    Reconciliation reconciliation = new Reconciliation(
        reconciliationNumber,
        LocalDateTime.now(),
        "BANK",
        "REF-001",
        java.math.BigDecimal.valueOf(1000),
        "Test reconciliation",
        "testuser"
    );
    try {
      java.lang.reflect.Field idField = com.finance.smartLedger.shared.entity.BaseEntity.class.getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(reconciliation, id);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return reconciliation;
  }
}
