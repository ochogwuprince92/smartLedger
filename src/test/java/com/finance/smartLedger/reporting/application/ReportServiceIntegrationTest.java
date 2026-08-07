package com.finance.smartLedger.reporting.application;

import static org.junit.jupiter.api.Assertions.*;

import com.finance.smartLedger.ledger.application.AccountService;
import com.finance.smartLedger.ledger.domain.AccountType;
import com.finance.smartLedger.ledger.infrastructure.persistence.AccountRepository;
import com.finance.smartLedger.reporting.domain.Report;
import com.finance.smartLedger.reporting.domain.ReportStatus;
import com.finance.smartLedger.reporting.domain.ReportType;
import com.finance.smartLedger.reporting.infrastructure.persistence.ReportRepository;
import com.finance.smartLedger.shared.valueobject.Money;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class ReportServiceIntegrationTest {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16-alpine")
          .withDatabaseName("smartledger_test")
          .withUsername("test")
          .withPassword("test");

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
  }

  @Autowired private ReportService reportService;

  @Autowired private ReportRepository reportRepository;

  @Autowired private AccountService accountService;

  @Autowired private AccountRepository accountRepository;

  @BeforeEach
  void setUp() {
    reportRepository.deleteAll();
    accountRepository.deleteAll();

    accountService.createAccount(
        "1001",
        "1001",
        "Cash",
        AccountType.ASSET,
        Money.of(BigDecimal.valueOf(1000.00), "USD"),
        "test-user");

    accountService.createAccount(
        "2001",
        "2001",
        "Accounts Payable",
        AccountType.LIABILITY,
        Money.of(BigDecimal.valueOf(500.00), "USD"),
        "test-user");

    accountService.createAccount(
        "3001",
        "3001",
        "Equity",
        AccountType.EQUITY,
        Money.of(BigDecimal.valueOf(500.00), "USD"),
        "test-user");

    accountService.createAccount(
        "4001",
        "4001",
        "Revenue",
        AccountType.REVENUE,
        Money.of(BigDecimal.valueOf(2000.00), "USD"),
        "test-user");

    accountService.createAccount(
        "5001",
        "5001",
        "Expenses",
        AccountType.EXPENSE,
        Money.of(BigDecimal.valueOf(1500.00), "USD"),
        "test-user");
  }

  @AfterEach
  void tearDown() {
    reportRepository.deleteAll();
    accountRepository.deleteAll();
  }

  @Test
  void createReport_Success() {
    Report report =
        reportService.createReport(
            "RPT-001",
            LocalDateTime.now(),
            ReportType.BALANCE_SHEET,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Monthly balance sheet",
            "test-user");

    assertNotNull(report);
    assertNotNull(report.getId());
    assertEquals("RPT-001", report.getReportNumber());
    assertEquals(ReportType.BALANCE_SHEET, report.getReportType());
    assertEquals(ReportStatus.PENDING, report.getStatus());
    assertEquals("USD", report.getCurrencyCode());
  }

  @Test
  void createReport_DuplicateNumber_ThrowsException() {
    reportService.createReport(
        "RPT-001",
        LocalDateTime.now(),
        ReportType.BALANCE_SHEET,
        LocalDateTime.now().minusDays(30),
        LocalDateTime.now(),
        "USD",
        "Monthly balance sheet",
        "test-user");

    assertThrows(
        IllegalArgumentException.class,
        () ->
            reportService.createReport(
                "RPT-001",
                LocalDateTime.now(),
                ReportType.INCOME_STATEMENT,
                LocalDateTime.now().minusDays(30),
                LocalDateTime.now(),
                "USD",
                "Another report",
                "test-user"));
  }

  @Test
  void generateReport_BalanceSheet_Success() {
    Report report =
        reportService.createReport(
            "RPT-001",
            LocalDateTime.now(),
            ReportType.BALANCE_SHEET,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Monthly balance sheet",
            "test-user");

    Report generated = reportService.generateReport(report.getId(), "test-user");

    assertEquals(ReportStatus.COMPLETED, generated.getStatus());
    assertNotNull(generated.getGeneratedAt());
    assertNotNull(generated.getReportData());
    assertTrue(generated.getReportData().contains("BALANCE_SHEET"));
    assertTrue(generated.getReportData().contains("assets"));
    assertTrue(generated.getReportData().contains("liabilities"));
    assertTrue(generated.getReportData().contains("equity"));
  }

  @Test
  void generateReport_IncomeStatement_Success() {
    Report report =
        reportService.createReport(
            "RPT-002",
            LocalDateTime.now(),
            ReportType.INCOME_STATEMENT,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Monthly income statement",
            "test-user");

    Report generated = reportService.generateReport(report.getId(), "test-user");

    assertEquals(ReportStatus.COMPLETED, generated.getStatus());
    assertNotNull(generated.getGeneratedAt());
    assertNotNull(generated.getReportData());
    assertTrue(generated.getReportData().contains("INCOME_STATEMENT"));
    assertTrue(generated.getReportData().contains("revenues"));
    assertTrue(generated.getReportData().contains("expenses"));
    assertTrue(generated.getReportData().contains("netIncome"));
  }

  @Test
  void generateReport_CashFlowStatement_Success() {
    Report report =
        reportService.createReport(
            "RPT-003",
            LocalDateTime.now(),
            ReportType.CASH_FLOW_STATEMENT,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Monthly cash flow statement",
            "test-user");

    Report generated = reportService.generateReport(report.getId(), "test-user");

    assertEquals(ReportStatus.COMPLETED, generated.getStatus());
    assertNotNull(generated.getGeneratedAt());
    assertNotNull(generated.getReportData());
    assertTrue(generated.getReportData().contains("CASH_FLOW_STATEMENT"));
    assertTrue(generated.getReportData().contains("operatingActivities"));
    assertTrue(generated.getReportData().contains("investingActivities"));
    assertTrue(generated.getReportData().contains("financingActivities"));
  }

  @Test
  void generateReport_AlreadyGenerating_ThrowsException() {
    Report report =
        reportService.createReport(
            "RPT-001",
            LocalDateTime.now(),
            ReportType.BALANCE_SHEET,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Monthly balance sheet",
            "test-user");

    report.startGenerating("test-user");
    reportRepository.save(report);

    assertThrows(
        IllegalStateException.class,
        () -> reportService.generateReport(report.getId(), "test-user"));
  }

  @Test
  void findByReportType_Success() {
    reportService.createReport(
        "RPT-001",
        LocalDateTime.now(),
        ReportType.BALANCE_SHEET,
        LocalDateTime.now().minusDays(30),
        LocalDateTime.now(),
        "USD",
        "Balance sheet 1",
        "test-user");

    reportService.createReport(
        "RPT-002",
        LocalDateTime.now(),
        ReportType.BALANCE_SHEET,
        LocalDateTime.now().minusDays(30),
        LocalDateTime.now(),
        "USD",
        "Balance sheet 2",
        "test-user");

    reportService.createReport(
        "RPT-003",
        LocalDateTime.now(),
        ReportType.INCOME_STATEMENT,
        LocalDateTime.now().minusDays(30),
        LocalDateTime.now(),
        "USD",
        "Income statement",
        "test-user");

    List<Report> balanceSheetReports = reportService.findByReportType(ReportType.BALANCE_SHEET);

    assertEquals(2, balanceSheetReports.size());
    assertTrue(
        balanceSheetReports.stream().allMatch(r -> r.getReportType() == ReportType.BALANCE_SHEET));
  }

  @Test
  void findByStatus_Success() {
    Report report1 =
        reportService.createReport(
            "RPT-001",
            LocalDateTime.now(),
            ReportType.BALANCE_SHEET,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Balance sheet 1",
            "test-user");

    Report report2 =
        reportService.createReport(
            "RPT-002",
            LocalDateTime.now(),
            ReportType.INCOME_STATEMENT,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Income statement",
            "test-user");

    reportService.generateReport(report1.getId(), "test-user");

    List<Report> pendingReports = reportService.findByStatus(ReportStatus.PENDING);

    assertEquals(1, pendingReports.size());
    assertEquals("RPT-002", pendingReports.get(0).getReportNumber());
  }

  @Test
  void findByCurrencyCode_Success() {
    reportService.createReport(
        "RPT-001",
        LocalDateTime.now(),
        ReportType.BALANCE_SHEET,
        LocalDateTime.now().minusDays(30),
        LocalDateTime.now(),
        "USD",
        "USD report",
        "test-user");

    reportService.createReport(
        "RPT-002",
        LocalDateTime.now(),
        ReportType.BALANCE_SHEET,
        LocalDateTime.now().minusDays(30),
        LocalDateTime.now(),
        "EUR",
        "EUR report",
        "test-user");

    List<Report> usdReports = reportService.findByCurrencyCode("USD");

    assertEquals(1, usdReports.size());
    assertEquals("USD", usdReports.get(0).getCurrencyCode());
  }

  @Test
  void deleteReport_Success() {
    Report report =
        reportService.createReport(
            "RPT-001",
            LocalDateTime.now(),
            ReportType.BALANCE_SHEET,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Balance sheet",
            "test-user");

    reportService.deleteReport(report.getId());

    assertFalse(reportService.findById(report.getId()).isPresent());
  }

  @Test
  void deleteReport_Generating_ThrowsException() {
    Report report =
        reportService.createReport(
            "RPT-001",
            LocalDateTime.now(),
            ReportType.BALANCE_SHEET,
            LocalDateTime.now().minusDays(30),
            LocalDateTime.now(),
            "USD",
            "Balance sheet",
            "test-user");

    report.startGenerating("test-user");
    reportRepository.save(report);

    assertThrows(IllegalStateException.class, () -> reportService.deleteReport(report.getId()));
  }
}
