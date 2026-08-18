package com.finance.smartLedger.ledger.infrastructure.scheduler;

import com.finance.smartLedger.ledger.application.BalanceService;
import com.finance.smartLedger.notification.application.NotificationService;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationType;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.shared.util.ClockProvider;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.scheduled.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class EndOfDayBalanceScheduler {

  private final BalanceService balanceService;
  private final ClockProvider clockProvider;
  private final NotificationService notificationService;
  private final UserRepository userRepository;

  @Value("${app.scheduled.end-of-day:0 0 23 * * ?}")
  private String endOfDayCron;

  @Scheduled(cron = "${app.scheduled.end-of-day:0 0 23 * * ?}")
  public void processEndOfDayBalances() {
    log.info("Starting end-of-day balance processing at: {}", clockProvider.now());
    try {
      boolean isBalanced = balanceService.isTrialBalanceBalanced();
      com.finance.smartLedger.shared.valueobject.Money trialBalanceDifference = balanceService.calculateTrialBalance();
      List<com.finance.smartLedger.ledger.domain.Account> accountsWithNegativeBalance = balanceService.getAccountsWithNegativeBalance();
      List<com.finance.smartLedger.ledger.domain.Account> accountsWithZeroBalance = balanceService.getAccountsWithZeroBalance();

      log.info("Trial balance check - Balanced: {}, Difference: {}", isBalanced, trialBalanceDifference);
      log.info("Accounts with negative balance: {}", accountsWithNegativeBalance.size());
      log.info("Accounts with zero balance: {}", accountsWithZeroBalance.size());

      if (!isBalanced || !accountsWithNegativeBalance.isEmpty()) {
        sendBalanceAlert(isBalanced, trialBalanceDifference, accountsWithNegativeBalance);
      }

      log.info("End-of-day balance processing completed at: {}", clockProvider.now());
    } catch (Exception e) {
      log.error("Error during end-of-day balance processing", e);
    }
  }

  private void sendBalanceAlert(boolean isBalanced, com.finance.smartLedger.shared.valueobject.Money trialBalanceDifference, List<com.finance.smartLedger.ledger.domain.Account> accountsWithNegativeBalance) {
    try {
      List<com.finance.smartLedger.security.domain.User> adminUsers = userRepository.findByRoleCodeAndEnabled("ADMIN");
      List<com.finance.smartLedger.security.domain.User> accountantUsers = userRepository.findByRoleCodeAndEnabled("ACCOUNTANT");

      String subject = "End-of-Day Balance Alert";
      StringBuilder message = new StringBuilder();
      message.append("End-of-day balance check completed with issues:\n\n");

      if (!isBalanced) {
        message.append(String.format("- Trial balance is NOT balanced. Difference: %s\n", trialBalanceDifference));
      }

      if (!accountsWithNegativeBalance.isEmpty()) {
        message.append(String.format("- %d accounts have negative balances\n", accountsWithNegativeBalance.size()));
      }

      message.append("\nPlease review the ledger for details.");

      for (com.finance.smartLedger.security.domain.User user : adminUsers) {
        notificationService.createNotification(
            user.getEmail(),
            user.getPhone(),
            NotificationType.SYSTEM_ALERT,
            NotificationChannel.EMAIL,
            subject,
            message.toString(),
            "BalanceCheck",
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
            message.toString(),
            "BalanceCheck",
            null,
            "scheduler"
        );
      }

      log.info("Balance alerts sent to {} ADMIN and {} ACCOUNTANT users", adminUsers.size(), accountantUsers.size());
    } catch (Exception e) {
      log.error("Failed to send balance alerts", e);
    }
  }
}
