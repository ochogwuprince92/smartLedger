package com.finance.smartLedger.reporting.infrastructure.scheduler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.finance.smartLedger.notification.application.NotificationService;
import com.finance.smartLedger.notification.domain.NotificationChannel;
import com.finance.smartLedger.notification.domain.NotificationType;
import com.finance.smartLedger.reporting.application.IncomeStatementGenerator;
import com.finance.smartLedger.security.domain.User;
import com.finance.smartLedger.security.infrastructure.persistence.UserRepository;
import com.finance.smartLedger.shared.util.ClockProvider;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReportSchedulerTest {

  @Mock private ClockProvider clockProvider;

  @Mock private IncomeStatementGenerator incomeStatementGenerator;

  @Mock private NotificationService notificationService;

  @Mock private UserRepository userRepository;

  @InjectMocks private ReportScheduler scheduler;

  private LocalDateTime testTime;

  @BeforeEach
  void setUp() {
    testTime = LocalDateTime.now();
    when(clockProvider.now()).thenReturn(testTime);
  }

  @Test
  void generateWeeklyReports_ShouldGenerateIncomeStatementAndEmail() throws Exception {
    // Given
    String reportJson = "{\"reportType\":\"INCOME_STATEMENT\",\"netIncome\":5000}";
    when(incomeStatementGenerator.generateIncomeStatement(any(LocalDateTime.class), any(LocalDateTime.class), anyString()))
        .thenReturn(reportJson);

    User adminUser = createTestUser("admin@test.com", "ADMIN");
    User accountantUser = createTestUser("accountant@test.com", "ACCOUNTANT");
    when(userRepository.findByRoleCodeAndEnabled("ADMIN")).thenReturn(List.of(adminUser));
    when(userRepository.findByRoleCodeAndEnabled("ACCOUNTANT")).thenReturn(List.of(accountantUser));

    // When
    scheduler.generateWeeklyReports();

    // Then
    verify(incomeStatementGenerator).generateIncomeStatement(any(LocalDateTime.class), any(LocalDateTime.class), eq("USD"));
    verify(notificationService, atLeastOnce()).createNotification(
        anyString(),
        isNull(),
        eq(NotificationType.SYSTEM_ALERT),
        eq(NotificationChannel.EMAIL),
        eq("Weekly Income Statement Report"),
        anyString(),
        eq("WeeklyReport"),
        isNull(),
        eq("scheduler")
    );
  }

  @Test
  void generateWeeklyReports_ShouldHandleGenerationErrorGracefully() throws Exception {
    // Given
    when(incomeStatementGenerator.generateIncomeStatement(any(LocalDateTime.class), any(LocalDateTime.class), anyString()))
        .thenThrow(new RuntimeException("Report generation failed"));

    // When
    scheduler.generateWeeklyReports();

    // Then
    verify(incomeStatementGenerator).generateIncomeStatement(any(LocalDateTime.class), any(LocalDateTime.class), anyString());
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
