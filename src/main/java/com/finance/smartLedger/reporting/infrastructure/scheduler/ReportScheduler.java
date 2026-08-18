package com.finance.smartLedger.reporting.infrastructure.scheduler;

import com.finance.smartLedger.notification.application.NotificationService;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationType;
import com.finance.smartLedger.reporting.application.IncomeStatementGenerator;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.shared.util.ClockProvider;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class ReportScheduler {

  private static final Logger log = LoggerFactory.getLogger(ReportScheduler.class);

  private final ClockProvider clockProvider;
  private final IncomeStatementGenerator incomeStatementGenerator;
  private final NotificationService notificationService;
  private final UserRepository userRepository;

  @Value("${app.scheduled.report-generation:0 0 6 * * MON}")
  private String reportGenerationCron;

  @Scheduled(cron = "${app.scheduled.report-generation:0 0 6 * * MON}")
  public void generateWeeklyReports() {
    log.info("Starting weekly report generation at: {}", clockProvider.now());
    try {
      LocalDateTime now = clockProvider.now();
      LocalDateTime weekStart = now.minusDays(7);
      LocalDateTime weekEnd = now;
      
      String reportJson = incomeStatementGenerator.generateIncomeStatement(weekStart, weekEnd, "USD");
      
      List<com.finance.smartLedger.security.domain.User> adminUsers = userRepository.findByRoleCodeAndEnabled("ADMIN");
      List<com.finance.smartLedger.security.domain.User> accountantUsers = userRepository.findByRoleCodeAndEnabled("ACCOUNTANT");

      String subject = "Weekly Income Statement Report";
      String message = String.format(
          "Weekly Income Statement Report\n\nPeriod: %s to %s\n\nReport Data:\n%s\n\nThis is an automated weekly report.",
          weekStart.toLocalDate(),
          weekEnd.toLocalDate(),
          reportJson
      );

      for (com.finance.smartLedger.security.domain.User user : adminUsers) {
        notificationService.createNotification(
            user.getEmail(),
            user.getPhone(),
            NotificationType.SYSTEM_ALERT,
            NotificationChannel.EMAIL,
            subject,
            message,
            "WeeklyReport",
            null,
            "scheduler"
        );
      }

      for (com.finance.smartLedger.security.domain.User user : accountantUsers) {
        notificationService.createNotification(
            user.getEmail(),
            user.getPhone(),
            NotificationType.SYSTEM_ALERT,
            NotificationChannel.EMAIL,
            subject,
            message,
            "WeeklyReport",
            null,
            "scheduler"
        );
      }

      log.info("Weekly report sent to {} ADMIN and {} ACCOUNTANT users", adminUsers.size(), accountantUsers.size());
      log.info("Weekly report generation completed at: {}", clockProvider.now());
    } catch (Exception e) {
      log.error("Error during weekly report generation", e);
    }
  }
}
