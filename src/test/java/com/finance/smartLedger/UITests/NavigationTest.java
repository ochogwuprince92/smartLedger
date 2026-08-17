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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class NavigationTest {

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        TestDatabaseConfiguration.configureDatabase(registry);
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
        wait.until(ExpectedConditions.titleContains("Dashboard"));
        
        WebElement dashboardHeading = driver.findElement(By.tagName("h1"));
        assertTrue(dashboardHeading.getText().contains("Dashboard"), "Dashboard page should load correctly");
        
        // Verify navigation link is active
        WebElement dashboardLink = driver.findElement(By.cssSelector("a[href='/dashboard']"));
        assertTrue(dashboardLink.getAttribute("class").contains("active"), "Dashboard nav link should be active");
    }

    @Test
    @Order(2)
    @DisplayName("Should navigate to Fees page")
    void testFeesNavigation() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.titleContains("Fee Management"));
        
        WebElement feesHeading = driver.findElement(By.tagName("h1"));
        assertTrue(feesHeading.getText().contains("Fee Management"), "Fees page should load correctly");
        
        // Verify navigation link is active
        WebElement feesLink = driver.findElement(By.cssSelector("a[href='/fees']"));
        assertTrue(feesLink.getAttribute("class").contains("active"), "Fees nav link should be active");
    }

    @Test
    @Order(3)
    @DisplayName("Should navigate to Payments page")
    void testPaymentsNavigation() {
        driver.get(baseUrl + "/payments");
        wait.until(ExpectedConditions.titleContains("Payments"));
        
        WebElement paymentsHeading = driver.findElement(By.tagName("h1"));
        assertTrue(paymentsHeading.getText().contains("Payments"), "Payments page should load correctly");
        
        // Verify navigation link is active
        WebElement paymentsLink = driver.findElement(By.cssSelector("a[href='/payments']"));
        assertTrue(paymentsLink.getAttribute("class").contains("active"), "Payments nav link should be active");
    }

    @Test
    @Order(4)
    @DisplayName("Should navigate to Ledger page")
    void testLedgerNavigation() {
        driver.get(baseUrl + "/ledger");
        wait.until(ExpectedConditions.titleContains("Ledger"));
        
        WebElement ledgerHeading = driver.findElement(By.tagName("h1"));
        assertTrue(ledgerHeading.getText().contains("Chart of Accounts"), "Ledger page should load correctly");
        
        // Verify navigation link is active
        WebElement ledgerLink = driver.findElement(By.cssSelector("a[href='/ledger']"));
        assertTrue(ledgerLink.getAttribute("class").contains("active"), "Ledger nav link should be active");
    }

    @Test
    @Order(5)
    @DisplayName("Should navigate to Journal page")
    void testJournalNavigation() {
        driver.get(baseUrl + "/journal");
        wait.until(ExpectedConditions.urlContains("/journal"));
        
        // Verify navigation link exists and is accessible
        WebElement journalLink = driver.findElement(By.cssSelector("a[href='/journal']"));
        assertNotNull(journalLink, "Journal nav link should exist");
    }

    @Test
    @Order(6)
    @DisplayName("Should navigate to Reconciliation page")
    void testReconciliationNavigation() {
        driver.get(baseUrl + "/reconciliation");
        wait.until(ExpectedConditions.titleContains("Reconciliation"));
        
        WebElement reconciliationHeading = driver.findElement(By.tagName("h1"));
        assertTrue(reconciliationHeading.getText().contains("Reconciliation"), "Reconciliation page should load correctly");
        
        // Verify navigation link is active
        WebElement reconciliationLink = driver.findElement(By.cssSelector("a[href='/reconciliation']"));
        assertTrue(reconciliationLink.getAttribute("class").contains("active"), "Reconciliation nav link should be active");
    }

    @Test
    @Order(7)
    @DisplayName("Should navigate to Reports page")
    void testReportsNavigation() {
        driver.get(baseUrl + "/reports");
        wait.until(ExpectedConditions.titleContains("Reports"));
        
        WebElement reportsHeading = driver.findElement(By.tagName("h1"));
        assertTrue(reportsHeading.getText().contains("Financial Reports"), "Reports page should load correctly");
        
        // Verify navigation link is active
        WebElement reportsLink = driver.findElement(By.cssSelector("a[href='/reports']"));
        assertTrue(reportsLink.getAttribute("class").contains("active"), "Reports nav link should be active");
    }

    @Test
    @Order(8)
    @DisplayName("Should navigate to AI Insights page")
    void testAiInsightsNavigation() {
        driver.get(baseUrl + "/ai-insights");
        wait.until(ExpectedConditions.titleContains("AI Insights"));
        
        WebElement aiInsightsHeading = driver.findElement(By.tagName("h1"));
        assertTrue(aiInsightsHeading.getText().contains("AI Insights"), "AI Insights page should load correctly");
        
        // Verify navigation link is active
        WebElement aiInsightsLink = driver.findElement(By.cssSelector("a[href='/ai-insights']"));
        assertTrue(aiInsightsLink.getAttribute("class").contains("active"), "AI Insights nav link should be active");
    }

    @Test
    @Order(9)
    @DisplayName("Should have all 8 navigation links present on dashboard")
    void testAllNavigationLinksPresent() {
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".navbar-nav")));
        
        // Verify all 8 navigation links are present
        String[] expectedLinks = {"/dashboard", "/fees", "/payments", "/ledger", "/journal", "/reconciliation", "/reports", "/ai-insights"};
        
        for (String linkHref : expectedLinks) {
            WebElement link = driver.findElement(By.cssSelector("a[href='" + linkHref + "']"));
            assertNotNull(link, "Navigation link " + linkHref + " should be present");
        }
    }

    @Test
    @Order(10)
    @DisplayName("Should navigate between pages using nav links")
    void testNavigationBetweenPages() {
        driver.get(baseUrl + "/dashboard");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".navbar-nav")));
        
        // Navigate from Dashboard to Fees
        WebElement feesLink = driver.findElement(By.cssSelector("a[href='/fees']"));
        feesLink.click();
        wait.until(ExpectedConditions.urlContains("/fees"));
        assertTrue(driver.getCurrentUrl().contains("/fees"), "Should navigate to Fees page");
        
        // Navigate from Fees to Payments
        WebElement paymentsLink = driver.findElement(By.cssSelector("a[href='/payments']"));
        paymentsLink.click();
        wait.until(ExpectedConditions.urlContains("/payments"));
        assertTrue(driver.getCurrentUrl().contains("/payments"), "Should navigate to Payments page");
        
        // Navigate from Payments to Ledger
        WebElement ledgerLink = driver.findElement(By.cssSelector("a[href='/ledger']"));
        ledgerLink.click();
        wait.until(ExpectedConditions.urlContains("/ledger"));
        assertTrue(driver.getCurrentUrl().contains("/ledger"), "Should navigate to Ledger page");
    }
}
