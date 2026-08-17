package com.finance.smartLedger.UITests;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class NavigationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "admin", authorities = {"DASHBOARD:READ"})
    void testDashboardPageLoads() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(xpath("//h1[contains(text(), 'Dashboard')]").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"FEE:READ"})
    void testFeesPageLoads() throws Exception {
        mockMvc.perform(get("/fees"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(xpath("//h1[contains(text(), 'Fee Management')]").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"PAYMENT:READ"})
    void testPaymentsPageLoads() throws Exception {
        mockMvc.perform(get("/payments"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(xpath("//h1[contains(text(), 'Payments')]").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"LEDGER:READ"})
    void testLedgerPageLoads() throws Exception {
        mockMvc.perform(get("/ledger"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(xpath("//h1[contains(text(), 'Chart of Accounts')]").exists());
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
                .andExpect(xpath("//h1[contains(text(), 'Reconciliation')]").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"REPORT:READ"})
    void testReportsPageLoads() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(xpath("//h1[contains(text(), 'Financial Reports')]").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"AI:READ"})
    void testAiInsightsPageLoads() throws Exception {
        mockMvc.perform(get("/ai-insights"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(xpath("//h1[contains(text(), 'AI Insights')]").exists());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"DASHBOARD:READ"})
    void testSharedLayoutFragmentPresent() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(xpath("//nav[@class='navbar']").exists())
                .andExpect(xpath("//a[@href='/dashboard']").exists())
                .andExpect(xpath("//a[@href='/fees']").exists())
                .andExpect(xpath("//a[@href='/payments']").exists())
                .andExpect(xpath("//a[@href='/ledger']").exists())
                .andExpect(xpath("//a[@href='/journal']").exists())
                .andExpect(xpath("//a[@href='/reconciliation']").exists())
                .andExpect(xpath("//a[@href='/reports']").exists())
                .andExpect(xpath("//a[@href='/ai-insights']").exists());
    }
}
