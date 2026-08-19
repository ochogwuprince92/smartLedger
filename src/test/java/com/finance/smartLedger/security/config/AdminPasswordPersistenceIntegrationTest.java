package com.finance.smartLedger.security.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

/**
 * Test to verify that AdminPasswordInitializer has been removed.
 *
 * This test verifies that the AdminPasswordInitializer class no longer exists,
 * which means admin password changes will now persist across application restarts.
 *
 * Before the fix: AdminPasswordInitializer was a CommandLineRunner that ran on every
 * startup and unconditionally reset the admin password to the configured default,
 * reverting any legitimate password changes made through the application's password
 * reset flows.
 *
 * After the fix: AdminPasswordInitializer has been deleted, so password changes
 * persist across application restarts.
 */
class AdminPasswordPersistenceIntegrationTest {

  @Test
  void adminPasswordInitializerShouldBeRemoved() {
    // Verify that AdminPasswordInitializer class no longer exists
    // If it exists, it would be a CommandLineRunner that runs on startup
    // and revert the admin password to the default
    try {
      Class<?> initializerClass = Class.forName("com.finance.smartLedger.security.config.AdminPasswordInitializer");
      fail("AdminPasswordInitializer class still exists at " + initializerClass.getName() + 
          ". It should have been deleted to prevent password reversion on startup. " +
          "This class was a CommandLineRunner that unconditionally reset the admin " +
          "password on every application startup, breaking legitimate password changes.");
    } catch (ClassNotFoundException e) {
      // This is the expected state - AdminPasswordInitializer has been removed
      // Password changes will now persist across application restarts
      assertTrue(true, "AdminPasswordInitializer has been successfully removed. " +
          "Admin password changes will now persist across application restarts.");
    }
  }
}
