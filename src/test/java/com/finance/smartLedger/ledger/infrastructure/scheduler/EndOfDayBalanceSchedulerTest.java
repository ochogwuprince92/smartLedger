package com.finance.smartLedger.ledger.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.ledger.application.BalanceService;
import com.finance.smartLedger.ledger.domain.Account;
import com.finance.smartLedger.notification.application.NotificationService;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationType;
import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.shared.util.ClockProvider;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EndOfDayBalanceSchedulerTest {

  @Mock private BalanceService balanceService;

  @Mock private ClockProvider clockProvider;

  @Mock private NotificationService notificationService;

  @Mock private UserRepository userRepository;

  @InjectMocks private EndOfDayBalanceScheduler scheduler;

  private LocalDateTime testTime;

  @BeforeEach
  void setUp() {
    testTime = LocalDateTime.now();
    when(clockProvider.now()).thenReturn(testTime);
  }

  @Test
  void processEndOfDayBalances_ShouldCallBalanceServiceAndAlert_WhenTrialBalanceNotBalanced() {
    // Given
    when(balanceService.calculateTrialBalanceByCurrency()).thenReturn(
        Map.of(
            "USD",
            com.finance.smartLedger.shared.valueobject.Money.of(java.math.BigDecimal.valueOf(100), "USD")));
    when(balanceService.getAccountsWithNegativeBalance()).thenReturn(new ArrayList<>());
    when(balanceService.getAccountsWithZeroBalance()).thenReturn(new ArrayList<>());

    User adminUser = createTestUser("admin@test.com", "ADMIN");
    User accountantUser = createTestUser("accountant@test.com", "ACCOUNTANT");
    when(userRepository.findByRoleCodeAndEnabled("ADMIN")).thenReturn(List.of(adminUser));
    when(userRepository.findByRoleCodeAndEnabled("ACCOUNTANT")).thenReturn(List.of(accountantUser));

    // When
    scheduler.processEndOfDayBalances();

    // Then
    verify(balanceService).calculateTrialBalanceByCurrency();
    verify(balanceService).getAccountsWithNegativeBalance();
    verify(balanceService).getAccountsWithZeroBalance();
    verify(notificationService, atLeastOnce()).createNotification(
        anyString(),
        isNull(),
        eq(NotificationType.SYSTEM_ALERT),
        eq(NotificationChannel.EMAIL),
        eq("End-of-Day Balance Alert"),
        anyString(),
        eq("BalanceCheck"),
        isNull(),
        eq("scheduler")
    );
  }

  @Test
  void processEndOfDayBalances_ShouldAlert_WhenAccountsHaveNegativeBalance() {
    // Given
    when(balanceService.calculateTrialBalanceByCurrency()).thenReturn(
        Map.of("USD", com.finance.smartLedger.shared.valueobject.Money.zero("USD")));

    Account negativeAccount = new Account();
    when(balanceService.getAccountsWithNegativeBalance()).thenReturn(List.of(negativeAccount));
    when(balanceService.getAccountsWithZeroBalance()).thenReturn(new ArrayList<>());

    User adminUser = createTestUser("admin@test.com", "ADMIN");
    when(userRepository.findByRoleCodeAndEnabled("ADMIN")).thenReturn(List.of(adminUser));

    // When
    scheduler.processEndOfDayBalances();

    // Then
    verify(balanceService).calculateTrialBalanceByCurrency();
    verify(balanceService).getAccountsWithNegativeBalance();
    verify(notificationService, atLeastOnce()).createNotification(
        anyString(),
        isNull(),
        eq(NotificationType.SYSTEM_ALERT),
        eq(NotificationChannel.EMAIL),
        eq("End-of-Day Balance Alert"),
        anyString(),
        eq("BalanceCheck"),
        isNull(),
        eq("scheduler")
    );
  }

  @Test
  void processEndOfDayBalances_ShouldNotAlert_WhenEverythingBalancesCorrectly() {
    // Given - regression guard: quiet on normal night
    when(balanceService.calculateTrialBalanceByCurrency()).thenReturn(
        Map.of(
            "USD", com.finance.smartLedger.shared.valueobject.Money.zero("USD"),
            "NGN", com.finance.smartLedger.shared.valueobject.Money.zero("NGN")));
    when(balanceService.getAccountsWithNegativeBalance()).thenReturn(new ArrayList<>());
    when(balanceService.getAccountsWithZeroBalance()).thenReturn(new ArrayList<>());

    // When
    scheduler.processEndOfDayBalances();

    // Then
    verify(balanceService).calculateTrialBalanceByCurrency();
    verify(balanceService).getAccountsWithNegativeBalance();
    verify(balanceService).getAccountsWithZeroBalance();
    verify(notificationService, never()).createNotification(
        anyString(),
        anyString(),
        any(NotificationType.class),
        any(NotificationChannel.class),
        anyString(),
        anyString(),
        anyString(),
        any(UUID.class),
        anyString()
    );
  }

  private User createTestUser(String email, String roleCode) {
    User user = new User("testuser", email, "password");
    user.setId(UUID.randomUUID());
    user.setEnabled(true);
    return user;
  }
}
