package com.finance.smartLedger.audit.infrastructure.scheduler;

import com.finance.smartLedger.audit.application.AuditService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogCleanupScheduler {

  private final AuditService auditService;

  @Value("${audit.retention.days:90}")
  private int retentionDays;

  @Scheduled(cron = "0 0 2 * * ?") // Run daily at 2 AM
  public void cleanupOldAuditLogs() {
    try {
      log.info("Starting audit log cleanup for logs older than {} days", retentionDays);
      LocalDateTime cutoffDate = LocalDateTime.now().minusDays(retentionDays);
      auditService.deleteAuditLogsBeforeDate(cutoffDate);
      log.info("Audit log cleanup completed successfully");
    } catch (Exception e) {
      log.error("Error during audit log cleanup", e);
    }
  }
}
