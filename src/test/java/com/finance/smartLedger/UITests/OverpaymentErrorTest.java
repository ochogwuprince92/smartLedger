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
import org.testcontainers.junit.jupiter.Testcontainers;
import com.finance.smartLedger.test.configuration.TestSecurityConfig;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(TestSecurityConfig.class)
public class OverpaymentErrorTest {

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
    @DisplayName("Should display human-readable overpayment error message")
    void testOverpaymentErrorMessageIsHumanReadable() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table")));
        
        // Click Pay button on first invoice
        WebElement payButton = driver.findElement(By.cssSelector("button[onclick*='showPaymentModal']"));
        payButton.click();
        
        // Wait for payment modal
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("paymentModal")));
        assertTrue(modal.isDisplayed(), "Payment modal should be visible");
        
        // Fill in payment form with valid data
        WebElement feeTypeSelect = driver.findElement(By.id("paymentFeeType"));
        feeTypeSelect.sendKeys("TUITION");
        
        WebElement amountInput = driver.findElement(By.id("paymentAmount"));
        amountInput.sendKeys("10000.00"); // Large amount to potentially trigger overpayment
        
        WebElement paymentMethodSelect = driver.findElement(By.id("paymentMethod"));
        paymentMethodSelect.sendKeys("CASH");
        
        WebElement referenceNumberInput = driver.findElement(By.id("referenceNumber"));
        referenceNumberInput.sendKeys("REF-12345");
        
        // Submit payment
        WebElement recordPaymentButton = driver.findElement(By.cssSelector("#paymentModal .btn-primary"));
        recordPaymentButton.click();
        
        // Wait for error state to appear
        WebElement errorState = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("errorState")));
        
        // Verify error state is displayed
        assertTrue(errorState.isDisplayed() || !errorState.getAttribute("class").contains("d-none"), 
            "Error state should be visible after overpayment attempt");
        
        // Get error message
        WebElement errorMessage = driver.findElement(By.id("errorMessage"));
        String errorText = errorMessage.getText();
        
        // Verify error message is human-readable (not raw JSON or stack trace)
        assertNotNull(errorText, "Error message should be present");
        assertFalse(errorText.contains("{"), "Error message should not contain raw JSON");
        assertFalse(errorText.contains("}"), "Error message should not contain raw JSON");
        assertFalse(errorText.contains("Exception"), "Error message should not contain stack trace");
        assertFalse(errorText.contains("at "), "Error message should not contain stack trace");
        
        // Verify error message mentions payment or balance (human-readable context)
        assertTrue(
            errorText.toLowerCase().contains("payment") || 
            errorText.toLowerCase().contains("balance") ||
            errorText.toLowerCase().contains("exceeds"),
            "Error message should be human-readable and mention payment/balance context"
        );
        
        // Verify error message is not just a status code
        assertFalse(errorText.matches("^\\d+$"), "Error message should not be just a status code");
        assertFalse(errorText.startsWith("400") || errorText.startsWith("4"), 
            "Error message should not start with HTTP status code");
    }

    @Test
    @DisplayName("Should not display raw JSON error in DOM")
    void testNoRawJsonErrorInDom() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table")));
        
        // Click Pay button
        driver.findElement(By.cssSelector("button[onclick*='showPaymentModal']")).click();
        
        // Wait for modal and submit payment
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("paymentModal")));
        
        // Fill and submit
        driver.findElement(By.id("paymentFeeType")).sendKeys("TUITION");
        driver.findElement(By.id("paymentAmount")).sendKeys("99999.99");
        driver.findElement(By.id("paymentMethod")).sendKeys("CASH");
        driver.findElement(By.id("referenceNumber")).sendKeys("TEST-REF");
        driver.findElement(By.cssSelector("#paymentModal .btn-primary")).click();
        
        // Check page source for raw JSON
        String pageSource = driver.getPageSource();
        
        // Verify no raw JSON error response in page
        assertFalse(pageSource.contains("\"success\":false"), "Page should not contain raw JSON success field");
        assertFalse(pageSource.contains("\"message\":"), "Page should not contain raw JSON message field");
        assertFalse(pageSource.contains("\"error\":"), "Page should not contain raw JSON error field");
    }

    @Test
    @DisplayName("Should display error in alert div with proper styling")
    void testErrorDisplayHasProperStyling() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table")));
        
        // Trigger error by submitting payment
        driver.findElement(By.cssSelector("button[onclick*='showPaymentModal']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("paymentModal")));
        
        driver.findElement(By.id("paymentFeeType")).sendKeys("TUITION");
        driver.findElement(By.id("paymentAmount")).sendKeys("99999.99");
        driver.findElement(By.id("paymentMethod")).sendKeys("CASH");
        driver.findElement(By.cssSelector("#paymentModal .btn-primary")).click();
        
        // Wait for error state
        WebElement errorState = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("errorState")));
        
        // Verify error state has alert-danger class (Bootstrap error styling)
        assertTrue(errorState.getAttribute("class").contains("alert"), 
            "Error state should have alert class");
        assertTrue(errorState.getAttribute("class").contains("alert-danger"), 
            "Error state should have alert-danger class for proper styling");
        
        // Verify error message is in a span with proper ID
        WebElement errorMessage = driver.findElement(By.id("errorMessage"));
        assertNotNull(errorMessage, "Error message should be in a properly identified element");
        
        // Verify icon is present for visual feedback
        WebElement errorIcon = errorState.findElement(By.cssSelector(".fa-exclamation-triangle"));
        assertNotNull(errorIcon, "Error state should have warning icon for visual feedback");
    }

    @Test
    @DisplayName("Should clear error state when successful operation occurs")
    void testErrorStateClearsOnSuccess() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table")));
        
        // Trigger error
        driver.findElement(By.cssSelector("button[onclick*='showPaymentModal']")).click();
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("paymentModal")));
        
        driver.findElement(By.id("paymentFeeType")).sendKeys("TUITION");
        driver.findElement(By.id("paymentAmount")).sendKeys("99999.99");
        driver.findElement(By.id("paymentMethod")).sendKeys("CASH");
        driver.findElement(By.cssSelector("#paymentModal .btn-primary")).click();
        
        // Wait for error state
        WebElement errorState = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("errorState")));
        
        // Close modal and reload page to clear error
        driver.findElement(By.cssSelector("#paymentModal .btn-close")).click();
        driver.navigate().refresh();
        
        // Wait for page to reload
        wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("h1")));
        
        // Verify error state is hidden after reload
        WebElement errorStateAfterReload = driver.findElement(By.id("errorState"));
        assertTrue(errorStateAfterReload.getAttribute("class").contains("d-none"), 
            "Error state should be hidden after page reload");
    }
}
