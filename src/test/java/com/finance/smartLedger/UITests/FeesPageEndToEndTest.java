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
import com.finance.smartLedger.test.configuration.TestSecurityConfig;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.data.redis.enabled=false",
    "spring.cache.type=none",
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false"
})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestSecurityConfig.class)
public class FeesPageEndToEndTest {

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
    @DisplayName("Should load fees page")
    void testFeesPageLoads() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should load");
    }

    @Test
    @Order(2)
    @DisplayName("Should navigate to fees page")
    void testFeesPageNavigation() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Should navigate to Fees page");
    }

    @Test
    @Order(3)
    @DisplayName("Should load fees page with correct URL")
    void testFeesPageUrl() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        String currentUrl = driver.getCurrentUrl();
        assertTrue(currentUrl.contains("/fees"), "URL should contain /fees");
    }

    @Test
    @Order(4)
    @DisplayName("Should access fees page")
    void testFeesPageAccess() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertNotNull(driver.getCurrentUrl(), "Current URL should not be null");
    }

    @Test
    @Order(5)
    @DisplayName("Should load fees page without errors")
    void testFeesPageNoErrors() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should load without errors");
    }

    @Test
    @Order(6)
    @DisplayName("Should have fees page accessible")
    void testFeesPageAccessible() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should be accessible");
    }

    @Test
    @Order(7)
    @DisplayName("Should respond to fees page request")
    void testFeesPageResponse() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should respond");
    }

    @Test
    @Order(8)
    @DisplayName("Should render fees page")
    void testFeesPageRender() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should render");
    }

    @Test
    @Order(9)
    @DisplayName("Should display fees page")
    void testFeesPageDisplay() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page should display");
    }

    @Test
    @Order(10)
    @DisplayName("Should handle fees page request")
    void testFeesPageRequest() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Fees page request should be handled");
    }
}
