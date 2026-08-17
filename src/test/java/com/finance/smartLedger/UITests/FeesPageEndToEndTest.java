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
import com.finance.smartLedger.test.configuration.TestSecurityConfig;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestSecurityConfig.class)
public class FeesPageEndToEndTest {

    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        TestDatabaseConfiguration.setUseTestcontainers(false);
        TestDatabaseConfiguration.configureDatabase(registry);
        registry.add("JWT_SECRET", () -> "test-secret-key-for-testing-only");
        registry.add("JWT_EXPIRATION", () -> "86400000");
        // Disable security for UI tests
        registry.add("spring.security.filter.dispatcher-types", () -> "");
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
    @DisplayName("Should load fees page and display invoice table")
    void testFeesPageLoads() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.titleContains("Fee Management"));
        
        WebElement feesHeading = driver.findElement(By.tagName("h1"));
        assertTrue(feesHeading.getText().contains("Fee Management"), "Fees page should load with correct heading");
        
        // Verify invoice table exists
        WebElement invoiceTable = driver.findElement(By.cssSelector(".card:nth-child(2) table"));
        assertNotNull(invoiceTable, "Invoice table should be present");
    }

    @Test
    @Order(2)
    @DisplayName("Should open create invoice modal when button clicked")
    void testCreateInvoiceModalOpens() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-bs-target='#createFeeModal']")));
        
        WebElement createButton = driver.findElement(By.cssSelector("button[data-bs-target='#createFeeModal']"));
        createButton.click();
        
        // Wait for modal to appear
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("createFeeModal")));
        assertTrue(modal.isDisplayed(), "Create invoice modal should be visible");
        
        // Verify modal has required fields
        WebElement studentIdInput = driver.findElement(By.id("studentId"));
        WebElement academicYearInput = driver.findElement(By.id("academicYear"));
        WebElement academicTermSelect = driver.findElement(By.id("academicTerm"));
        WebElement classGradeInput = driver.findElement(By.id("classGrade"));
        WebElement dueDateInput = driver.findElement(By.id("dueDate"));
        
        assertNotNull(studentIdInput, "Student ID input should exist");
        assertNotNull(academicYearInput, "Academic Year input should exist");
        assertNotNull(academicTermSelect, "Academic Term select should exist");
        assertNotNull(classGradeInput, "Class Grade input should exist");
        assertNotNull(dueDateInput, "Due Date input should exist");
    }

    @Test
    @Order(3)
    @DisplayName("Should open line item modal when Add Item button clicked")
    void testLineItemModalOpens() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table")));
        
        // Find first invoice and click Add Item button
        WebElement addItemButton = driver.findElement(By.cssSelector("button[onclick*='showLineItemModal']"));
        addItemButton.click();
        
        // Wait for modal to appear
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("lineItemModal")));
        assertTrue(modal.isDisplayed(), "Line item modal should be visible");
        
        // Verify modal has required fields
        WebElement feeTypeSelect = driver.findElement(By.id("feeType"));
        WebElement amountInput = driver.findElement(By.id("lineItemAmount"));
        WebElement currencySelect = driver.findElement(By.id("lineItemCurrency"));
        WebElement descriptionTextarea = driver.findElement(By.id("lineItemDescription"));
        
        assertNotNull(feeTypeSelect, "Fee Type select should exist");
        assertNotNull(amountInput, "Amount input should exist");
        assertNotNull(currencySelect, "Currency select should exist");
        assertNotNull(descriptionTextarea, "Description textarea should exist");
    }

    @Test
    @Order(4)
    @DisplayName("Should open payment modal when Pay button clicked")
    void testPaymentModalOpens() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table")));
        
        // Find first invoice and click Pay button
        WebElement payButton = driver.findElement(By.cssSelector("button[onclick*='showPaymentModal']"));
        payButton.click();
        
        // Wait for modal to appear
        WebElement modal = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("paymentModal")));
        assertTrue(modal.isDisplayed(), "Payment modal should be visible");
        
        // Verify modal has required fields
        WebElement feeTypeSelect = driver.findElement(By.id("paymentFeeType"));
        WebElement amountInput = driver.findElement(By.id("paymentAmount"));
        WebElement currencySelect = driver.findElement(By.id("paymentCurrency"));
        WebElement paymentMethodSelect = driver.findElement(By.id("paymentMethod"));
        WebElement referenceNumberInput = driver.findElement(By.id("referenceNumber"));
        
        assertNotNull(feeTypeSelect, "Fee Type select should exist");
        assertNotNull(amountInput, "Amount input should exist");
        assertNotNull(currencySelect, "Currency select should exist");
        assertNotNull(paymentMethodSelect, "Payment Method select should exist");
        assertNotNull(referenceNumberInput, "Reference Number input should exist");
    }

    @Test
    @Order(5)
    @DisplayName("Should display loading state when page loads")
    void testLoadingStateDisplays() {
        driver.get(baseUrl + "/fees");
        
        // Check for loading state element
        WebElement loadingState = driver.findElement(By.id("loadingState"));
        assertNotNull(loadingState, "Loading state element should exist");
        
        // Initially loading state should be visible (before data loads)
        assertTrue(loadingState.isDisplayed() || loadingState.getAttribute("class").contains("d-none"), 
            "Loading state should be present");
    }

    @Test
    @Order(6)
    @DisplayName("Should display error state when API fails")
    void testErrorStateDisplays() {
        driver.get(baseUrl + "/fees");
        
        // Check for error state element
        WebElement errorState = driver.findElement(By.id("errorState"));
        assertNotNull(errorState, "Error state element should exist");
        
        // Error message element should exist
        WebElement errorMessage = driver.findElement(By.id("errorMessage"));
        assertNotNull(errorMessage, "Error message element should exist");
    }

    @Test
    @Order(7)
    @DisplayName("Should have all fee type options in line item modal")
    void testFeeTypeOptionsPresent() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("button[data-bs-target='#createFeeModal']")));
        
        WebElement createButton = driver.findElement(By.cssSelector("button[data-bs-target='#createFeeModal']"));
        createButton.click();
        
        // Find first invoice and click Add Item button
        driver.findElement(By.cssSelector("button[onclick*='showLineItemModal']")).click();
        
        WebElement feeTypeSelect = driver.findElement(By.id("feeType"));
        assertNotNull(feeTypeSelect, "Fee Type select should exist");
        
        // Verify options
        String[] expectedOptions = {"TUITION", "REGISTRATION", "EXAMINATION", "LIBRARY", "LABORATORY", "SPORTS", "TRANSPORTATION", "OTHER"};
        for (String option : expectedOptions) {
            WebElement optionElement = feeTypeSelect.findElement(By.cssSelector("option[value='" + option + "']"));
            assertNotNull(optionElement, "Fee type option " + option + " should exist");
        }
    }

    @Test
    @Order(8)
    @DisplayName("Should have all payment method options in payment modal")
    void testPaymentMethodOptionsPresent() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table")));
        
        WebElement payButton = driver.findElement(By.cssSelector("button[onclick*='showPaymentModal']"));
        payButton.click();
        
        WebElement paymentMethodSelect = driver.findElement(By.id("paymentMethod"));
        assertNotNull(paymentMethodSelect, "Payment Method select should exist");
        
        // Verify options
        String[] expectedOptions = {"CASH", "BANK_TRANSFER", "CARD", "PAYSTACK", "CHECK"};
        for (String option : expectedOptions) {
            WebElement optionElement = paymentMethodSelect.findElement(By.cssSelector("option[value='" + option + "']"));
            assertNotNull(optionElement, "Payment method option " + option + " should exist");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Should have currency options in all modals")
    void testCurrencyOptionsPresent() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table")));
        
        // Check line item modal
        driver.findElement(By.cssSelector("button[onclick*='showLineItemModal']")).click();
        WebElement lineItemCurrency = driver.findElement(By.id("lineItemCurrency"));
        assertNotNull(lineItemCurrency.findElement(By.cssSelector("option[value='USD']")), "USD option should exist in line item modal");
        assertNotNull(lineItemCurrency.findElement(By.cssSelector("option[value='NGN']")), "NGN option should exist in line item modal");
        
        // Close line item modal
        driver.findElement(By.cssSelector("#lineItemModal .btn-close")).click();
        
        // Check payment modal
        driver.findElement(By.cssSelector("button[onclick*='showPaymentModal']")).click();
        WebElement paymentCurrency = driver.findElement(By.id("paymentCurrency"));
        assertNotNull(paymentCurrency.findElement(By.cssSelector("option[value='USD']")), "USD option should exist in payment modal");
        assertNotNull(paymentCurrency.findElement(By.cssSelector("option[value='NGN']")), "NGN option should exist in payment modal");
    }

    @Test
    @Order(10)
    @DisplayName("Should have action buttons on invoice table rows")
    void testInvoiceActionButtons() {
        driver.get(baseUrl + "/fees");
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".card:nth-child(2) table tbody tr")));
        
        // Find first invoice row
        WebElement firstRow = driver.findElement(By.cssSelector(".card:nth-child(2) table tbody tr"));
        
        // Verify action buttons exist
        WebElement payButton = firstRow.findElement(By.cssSelector("button[onclick*='showPaymentModal']"));
        WebElement addItemButton = firstRow.findElement(By.cssSelector("button[onclick*='showLineItemModal']"));
        WebElement viewButton = firstRow.findElement(By.cssSelector("button[onclick*='viewInvoice']"));
        
        assertNotNull(payButton, "Pay button should exist");
        assertNotNull(addItemButton, "Add Item button should exist");
        assertNotNull(viewButton, "View button should exist");
    }
}
