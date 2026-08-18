package com.finance.smartLedger.UITests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.flyway.enabled=false",
    "JWT_SECRET=test-secret-key-for-testing-only",
    "JWT_EXPIRATION=86400000"
})
public class NavigationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", authorities = {"DASHBOARD:READ"})
    void testDashboardPageLoads() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("Dashboard")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"FEE:READ"})
    void testFeesPageLoads() throws Exception {
        mockMvc.perform(get("/fees"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("Fee Management")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"PAYMENT:READ"})
    void testPaymentsPageLoads() throws Exception {
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("Payments")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"LEDGER:READ"})
    void testLedgerPageLoads() throws Exception {
        mockMvc.perform(get("/ledger"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("Chart of Accounts")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"JOURNAL:READ"})
    void testJournalPageLoads() throws Exception {
        mockMvc.perform(get("/journal"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"RECONCILIATION:READ"})
    void testReconciliationPageLoads() throws Exception {
        mockMvc.perform(get("/reconciliation"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("Reconciliation")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"REPORT:READ"})
    void testReportsPageLoads() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("Financial Reports")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"AI:READ"})
    void testAiInsightsPageLoads() throws Exception {
        mockMvc.perform(get("/ai-insights"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("AI Insights")));
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"DASHBOARD:READ"})
    void testSharedLayoutFragmentPresent() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("SmartLedger")))
                .andExpect(content().string(containsString("navbar")));
    }
}
