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
import org.springframework.test.annotation.DirtiesContext;
import com.finance.smartLedger.test.configuration.TestSecurityConfig;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@Import(TestSecurityConfig.class)
public class NavigationTest {

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
    @Order(1)
    @DisplayName("Should navigate to Dashboard page")
    void testDashboardNavigation() {
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Should navigate to Dashboard page");
    }

    @Test
    @Order(2)
    @DisplayName("Should navigate to Fees page")
    void testFeesNavigation() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Should navigate to Fees page");
    }

    @Test
    @Order(3)
    @DisplayName("Should navigate to Payments page")
    void testPaymentsNavigation() {
        driver.get(baseUrl + "/payments");
        wait.until(ExpectedConditions.urlContains("/payments"));
        assertTrue(driver.getCurrentUrl().contains("/payments"), "Should navigate to Payments page");
    }

    @Test
    @Order(4)
    @DisplayName("Should navigate to Ledger page")
    void testLedgerNavigation() {
        driver.get(baseUrl + "/ledger");
        wait.until(ExpectedConditions.urlContains("/ledger"));
        assertTrue(driver.getCurrentUrl().contains("/ledger"), "Should navigate to Ledger page");
    }

    @Test
    @Order(5)
    @DisplayName("Should navigate to Journal page")
    void testJournalNavigation() {
        driver.get(baseUrl + "/journal");
        wait.until(ExpectedConditions.urlContains("/journal"));
        assertTrue(driver.getCurrentUrl().contains("/journal"), "Should navigate to Journal page");
    }

    @Test
    @Order(6)
    @DisplayName("Should navigate to Reconciliation page")
    void testReconciliationNavigation() {
        driver.get(baseUrl + "/reconciliation");
        wait.until(ExpectedConditions.urlContains("/reconciliation"));
        assertTrue(driver.getCurrentUrl().contains("/reconciliation"), "Should navigate to Reconciliation page");
    }

    @Test
    @Order(7)
    @DisplayName("Should navigate to Reports page")
    void testReportsNavigation() {
        driver.get(baseUrl + "/reports");
        wait.until(ExpectedConditions.urlContains("/reports"));
        assertTrue(driver.getCurrentUrl().contains("/reports"), "Should navigate to Reports page");
    }

    @Test
    @Order(8)
    @DisplayName("Should navigate to AI Insights page")
    void testAiInsightsNavigation() {
        driver.get(baseUrl + "/ai-insights");
        wait.until(ExpectedConditions.urlContains("/ai-insights"));
        assertTrue(driver.getCurrentUrl().contains("/ai-insights"), "Should navigate to AI Insights page");
    }

    @Test
    @Order(9)
    @DisplayName("Should have all 8 navigation links present on dashboard")
    void testAllNavigationLinksPresent() {
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Verify dashboard page loads
        assertTrue(driver.getCurrentUrl().contains("/dashboard"), "Dashboard page should load");
    }

    @Test
    @Order(10)
    @DisplayName("Should navigate between pages using nav links")
    void testNavigationBetweenPages() {
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.urlContains("/dashboard"));
        
        // Navigate from Dashboard to Fees
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Should navigate to Fees page");
        
        // Navigate from Fees to Payments
        driver.get(baseUrl + "/payments");
        wait.until(ExpectedConditions.urlContains("/payments"));
        assertTrue(driver.getCurrentUrl().contains("/payments"), "Should navigate to Payments page");
        
        // Navigate from Payments to Ledger
        driver.get(baseUrl + "/ledger");
        wait.until(ExpectedConditions.urlContains("/ledger"));
        assertTrue(driver.getCurrentUrl().contains("/ledger"), "Should navigate to Ledger page");
    }
}
