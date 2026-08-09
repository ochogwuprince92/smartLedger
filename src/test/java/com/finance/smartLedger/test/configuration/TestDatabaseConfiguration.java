package com.finance.smartLedger.test.configuration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDatabaseConfiguration {

  private static PostgreSQLContainer<?> postgres;

  public static void configureDatabase(DynamicPropertyRegistry registry) {
    if (postgres == null || !postgres.isRunning()) {
      postgres =
          new PostgreSQLContainer<>("postgres:16-alpine")
              .withDatabaseName("smartledger_test")
              .withUsername("test")
              .withPassword("test");
      postgres.start();
    }
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    registry.add("spring.flyway.enabled", () -> "true");
    registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    
    // Disable Redis for tests
    registry.add("spring.data.redis.enabled", () -> "false");
    registry.add("spring.cache.type", () -> "none");
    
    // Disable scheduled tasks for tests
    registry.add("app.scheduled.enabled", () -> "false");
    
    // Disable data loader for tests
    registry.add("app.data-loader.enabled", () -> "false");
  }

  public static void stopContainer() {
    if (postgres != null && postgres.isRunning()) {
      postgres.stop();
    }
  }
}
