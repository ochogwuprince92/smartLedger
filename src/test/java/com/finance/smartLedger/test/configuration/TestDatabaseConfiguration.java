package com.finance.smartLedger.test.configuration;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDatabaseConfiguration {

  private static PostgreSQLContainer<?> postgres;
  private static boolean useTestcontainers = Boolean.parseBoolean(
      System.getProperty("use.testcontainers", "true"));

  @SuppressWarnings("Lombok")
  public static void setUseTestcontainers(boolean use) {
    useTestcontainers = use;
  }

  public static void configureDatabase(DynamicPropertyRegistry registry) {
    if (useTestcontainers) {
      configureWithTestcontainers(registry);
    } else {
      configureWithLocalDatabase(registry);
    }
  }

  private static void configureWithTestcontainers(DynamicPropertyRegistry registry) {
    if (postgres == null || !postgres.isRunning()) {
      postgres =
          new PostgreSQLContainer<>("postgres:15-alpine")
              .withDatabaseName("smartledger_test")
              .withUsername("test")
              .withPassword("test");
      postgres.start();
    }
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    registry.add("spring.flyway.enabled", () -> "true");
    
    // Disable Redis for tests
    registry.add("spring.data.redis.enabled", () -> "false");
    registry.add("spring.cache.type", () -> "none");
    
    // Disable scheduled tasks for tests
    registry.add("app.scheduled.enabled", () -> "false");
    
    // Disable data loader for tests
    registry.add("app.data-loader.enabled", () -> "false");
    
    // Add JWT configuration for tests
    registry.add("JWT_SECRET", () -> "test-secret-key-for-testing-only");
    registry.add("JWT_EXPIRATION", () -> "86400000");
  }

  public static void configureWithLocalDatabase(DynamicPropertyRegistry registry) {
    // Use existing local database configuration from application.yml
    registry.add("spring.datasource.url", () -> 
        System.getenv().getOrDefault("DATASOURCE_URL", "jdbc:postgresql://localhost:5432/smartledger_db"));
    registry.add("spring.datasource.username", () -> 
        System.getenv().getOrDefault("DATASOURCE_USERNAME", "postgres"));
    registry.add("spring.datasource.password", () -> 
        System.getenv().getOrDefault("DATASOURCE_PASSWORD", "ogwaa123"));
    registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    registry.add("spring.flyway.enabled", () -> "false");
    
    // Disable Redis for tests
    registry.add("spring.data.redis.enabled", () -> "false");
    registry.add("spring.cache.type", () -> "none");
    
    // Disable scheduled tasks for tests
    registry.add("app.scheduled.enabled", () -> "false");
    
    // Disable data loader for tests
    registry.add("app.data-loader.enabled", () -> "false");
    
    // Add JWT configuration for tests
    registry.add("JWT_SECRET", () -> "test-secret-key-for-testing-only");
    registry.add("JWT_EXPIRATION", () -> "86400000");
  }

  public static void stopContainer() {
    if (postgres != null && postgres.isRunning()) {
      postgres.stop();
    }
  }
}
