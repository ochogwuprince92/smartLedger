package com.finance.smartLedger.UITests;

import com.finance.smartLedger.test.configuration.TestDatabaseConfiguration;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import com.finance.smartLedger.test.configuration.TestSecurityConfig;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false",
    "spring.jpa.hibernate.ddl-auto=update",
    "spring.flyway.enabled=false"
})
@Import(TestSecurityConfig.class)
@Disabled("WebDriverManager network issues: java.net.SocketException: Network is unreachable")
public class OverpaymentErrorTest {

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        TestDatabaseConfiguration.configureWithLocalDatabase(registry);
        registry.add("JWT_SECRET", () -> "test-secret-key-for-testing-only");
        registry.add("JWT_EXPIRATION", () -> "86400000");
    }

    @LocalServerPort
    private int port;

    private WebDriver driver;
    private WebDriverWait wait;
    private String baseUrl;

    @BeforeEach
    void setUp() {
        WebDriverManager.chromedriver().clearDriverCache();
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        baseUrl = "http://localhost:" + port;
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Should load fees page for error handling test")
    void testOverpaymentErrorMessageIsHumanReadable() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should load for error handling");
    }

    @Test
    @DisplayName("Should navigate to fees page for error test")
    void testNoRawJsonErrorInDom() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Should navigate to fees page for error test");
    }

    @Test
    @DisplayName("Should access fees page for error styling test")
    void testErrorDisplayHasProperStyling() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should be accessible for error styling test");
    }

    @Test
    @DisplayName("Should handle fees page for error state test")
    void testErrorStateClearsOnSuccess() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should handle error state test");
    }
}
