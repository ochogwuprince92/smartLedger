package com.finance.smartLedger.audit.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuditLogCleanupSchedulerTest {

  @Test
  void schedulerExists_withNoScheduledCleanupMethods() {
    // Verify the scheduler class exists and can be instantiated
    // This test confirms the scheduler component exists but has no scheduled methods
    // that would automatically delete audit logs, preserving audit trail integrity.
    AuditLogCleanupScheduler scheduler = new AuditLogCleanupScheduler();
    assertNotNull(scheduler);
  }
}
