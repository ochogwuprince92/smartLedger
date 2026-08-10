package com.finance.smartLedger.audit.infrastructure.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogCleanupScheduler {

  // Scheduled cleanup disabled to preserve audit trail integrity.
  // Audit logs must not be automatically deleted as they are required for compliance.
  // Manual deletion via API endpoints remains available for administrative purposes.
}
