package com.finance.smartLedger.migration;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.finance.smartLedger.test.configuration.TestDatabaseConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.context.TestPropertySource;
import org.junit.jupiter.api.Disabled;

@SpringBootTest
@TestPropertySource(properties = {
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false"
})
@Disabled("Docker not available on this system")
@Testcontainers
class ManualMigrationTest {

  @DynamicPropertySource
  static void postgresProperties(DynamicPropertyRegistry registry) {
    TestDatabaseConfiguration.configureDatabase(registry);
  }

  @Autowired private DataSource dataSource;

  @Test
  void testFlywayMigrations() throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {

      // Verify that the chart of accounts tables exist
      verifyTableExists(statement, "account_categories");
      verifyTableExists(statement, "account_subcategories");
      verifyTableExists(statement, "ledger_accounts");
      verifyTableExists(statement, "account_relationships");
      verifyTableExists(statement, "account_balances");

      // Verify that triggers were created
      verifyTriggerExists(statement, "audit_account_categories");
      verifyTriggerExists(statement, "audit_ledger_accounts");
      verifyTriggerExists(statement, "balance_change_ledger_accounts");
      verifyTriggerExists(statement, "validate_hierarchy_ledger_accounts");

      // Verify initial data was inserted
      verifyAccountCategoriesExist(statement);
      verifyLedgerAccountsExist(statement);

      // Verify constraints exist
      verifyConstraintsExist(statement);
    }
  }

  private void verifyTableExists(Statement statement, String tableName) throws SQLException {
    ResultSet rs =
        statement.executeQuery(
            "SELECT table_name FROM information_schema.tables "
                + "WHERE table_schema = 'public' AND table_name = '"
                + tableName
                + "'");
    assertTrue(rs.next(), "Table " + tableName + " should exist");
    assertEquals(tableName, rs.getString("table_name"));
  }

  private void verifyTriggerExists(Statement statement, String triggerName) throws SQLException {
    ResultSet rs =
        statement.executeQuery(
            "SELECT trigger_name FROM information_schema.triggers "
                + "WHERE trigger_name = '"
                + triggerName
                + "'");
    assertTrue(rs.next(), "Trigger " + triggerName + " should exist");
    assertEquals(triggerName, rs.getString("trigger_name"));
  }

  private void verifyAccountCategoriesExist(Statement statement) throws SQLException {
    ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM account_categories");
    assertTrue(rs.next());
    assertTrue(rs.getInt(1) >= 5, "At least 5 account categories should exist");
  }

  private void verifyLedgerAccountsExist(Statement statement) throws SQLException {
    ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM ledger_accounts");
    assertTrue(rs.next());
    assertTrue(rs.getInt(1) >= 6, "At least 6 ledger accounts should exist");
  }

  private void verifyConstraintsExist(Statement statement) throws SQLException {
    // Verify check constraints on ledger_accounts
    ResultSet rs =
        statement.executeQuery(
            "SELECT constraint_name FROM information_schema.table_constraints "
                + "WHERE table_name = 'ledger_accounts' AND constraint_type = 'CHECK'");
    assertTrue(rs.next(), "Check constraints should exist on ledger_accounts");
  }

  @Test
  void testAccountNumberFormatConstraint() throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {

      // Try to insert an invalid account number
      assertThrows(
          SQLException.class,
          () -> {
            statement.execute(
                "INSERT INTO ledger_accounts "
                    + "(id, subcategory_id, account_number, account_code, account_name, account_type, normal_balance, currency, opening_balance, current_balance, account_level, is_active, is_control_account, is_reconcilable, created_at, updated_at, created_by, updated_by) "
                    + "VALUES (gen_random_uuid(), (SELECT id FROM account_subcategories LIMIT 1), 'ABC123', '9999', 'Test Account', 'ASSET', 'DEBIT', 'USD', 0, 0, 0, true, false, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM')");
          },
          "Should reject invalid account number format");
    }
  }

  @Test
  void testAuditTriggerFunctionality() throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {

      // Count initial audit log entries
      ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM audit_log");
      rs.next();
      int initialCount = rs.getInt(1);

      // Insert a new account
      statement.execute(
          "INSERT INTO ledger_accounts "
              + "(id, subcategory_id, account_number, account_code, account_name, account_type, normal_balance, currency, opening_balance, current_balance, account_level, is_active, is_control_account, is_reconcilable, created_at, updated_at, created_by, updated_by) "
              + "VALUES (gen_random_uuid(), (SELECT id FROM account_subcategories LIMIT 1), '9997', '9997', 'Audit Test Account', 'ASSET', 'DEBIT', 'USD', 0, 0, 0, true, false, true, NOW(), NOW(), 'SYSTEM', 'SYSTEM')");

      // Verify audit log was updated
      rs = statement.executeQuery("SELECT COUNT(*) FROM audit_log");
      rs.next();
      assertTrue(rs.getInt(1) > initialCount, "Audit log should have new entries");
    }
  }
}
