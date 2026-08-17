package com.finance.smartLedger.web.presentation;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.finance.smartLedger.security.service.PasswordResetService;
import com.finance.smartLedger.security.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.data.redis.enabled=false",
    "spring.cache.type=none",
    "app.scheduled.enabled=false",
    "app.data-loader.enabled=false",
    "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration"
})
class WebControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockBean private UserService userService;

  @MockBean private PasswordResetService passwordResetService;

  @Test
  @WithMockUser(authorities = "FEE:READ")
  void feesPage_WithFeeReadPermission_ShouldReturn200() throws Exception {
    mockMvc.perform(get("/fees")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "FEE:READ")
  void feesPage_WithoutFeeCreatePermission_ShouldNotShowCreateButton() throws Exception {
    mockMvc
        .perform(get("/fees"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Fee Management")))
        .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Create Fee Invoice"))));
  }

  @Test
  @WithMockUser(authorities = {"FEE:READ", "FEE:CREATE"})
  void feesPage_WithFeeCreatePermission_ShouldShowCreateButton() throws Exception {
    mockMvc
        .perform(get("/fees"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Create Fee Invoice")));
  }

  @Test
  @WithMockUser(authorities = "PAYMENT:READ")
  void paymentsPage_WithPaymentReadPermission_ShouldReturn200() throws Exception {
    mockMvc.perform(get("/payments")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "PAYMENT:READ")
  void paymentsPage_WithoutPaymentCreatePermission_ShouldNotShowCreateButton()
      throws Exception {
    mockMvc
        .perform(get("/payments"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Payments")))
        .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Record Payment"))));
  }

  @Test
  @WithMockUser(authorities = {"PAYMENT:READ", "PAYMENT:CREATE"})
  void paymentsPage_WithPaymentCreatePermission_ShouldShowCreateButton() throws Exception {
    mockMvc
        .perform(get("/payments"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Record Payment")));
  }

  @Test
  @WithMockUser(authorities = "LEDGER:READ")
  void ledgerPage_WithLedgerReadPermission_ShouldReturn200() throws Exception {
    mockMvc.perform(get("/ledger")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "LEDGER:READ")
  void ledgerPage_WithoutLedgerUpdatePermission_ShouldNotShowCreateButton() throws Exception {
    mockMvc
        .perform(get("/ledger"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Chart of Accounts")))
        .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Create Account"))));
  }

  @Test
  @WithMockUser(authorities = {"LEDGER:READ", "LEDGER:UPDATE"})
  void ledgerPage_WithLedgerUpdatePermission_ShouldShowCreateButton() throws Exception {
    mockMvc
        .perform(get("/ledger"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("New Account")));
  }

  @Test
  @WithMockUser(authorities = "RECONCILIATION:READ")
  void reconciliationPage_WithReconciliationReadPermission_ShouldReturn200() throws Exception {
    mockMvc.perform(get("/reconciliation")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "RECONCILIATION:READ")
  void reconciliationPage_WithoutReconciliationCreatePermission_ShouldNotShowCreateButton()
      throws Exception {
    mockMvc
        .perform(get("/reconciliation"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Reconciliation")))
        .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Create Reconciliation"))));
  }

  @Test
  @WithMockUser(authorities = "RECONCILIATION:READ")
  void reconciliationPage_WithoutReconciliationExecutePermission_ShouldNotShowStartButton()
      throws Exception {
    mockMvc
        .perform(get("/reconciliation"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Reconciliation")))
        .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Start Reconciliation"))));
  }

  @Test
  @WithMockUser(authorities = {"RECONCILIATION:READ", "RECONCILIATION:CREATE"})
  void reconciliationPage_WithReconciliationCreatePermission_ShouldShowCreateButton()
      throws Exception {
    mockMvc
        .perform(get("/reconciliation"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("New Reconciliation")));
  }

  @Test
  @WithMockUser(authorities = {"RECONCILIATION:READ", "RECONCILIATION:EXECUTE"})
  void reconciliationPage_WithReconciliationExecutePermission_ShouldShowStartButton()
      throws Exception {
    mockMvc
        .perform(get("/reconciliation"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Start Reconciliation")));
  }

  @Test
  @WithMockUser(authorities = "REPORT:READ")
  void reportsPage_WithReportReadPermission_ShouldReturn200() throws Exception {
    mockMvc.perform(get("/reports")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "REPORT:READ")
  void reportsPage_WithoutReportGeneratePermission_ShouldNotShowGenerateButtons()
      throws Exception {
    mockMvc
        .perform(get("/reports"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Financial Reports")))
        .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Generate Report"))));
  }

  @Test
  @WithMockUser(authorities = {"REPORT:READ", "REPORT:GENERATE"})
  void reportsPage_WithReportGeneratePermission_ShouldShowGenerateButtons() throws Exception {
    mockMvc
        .perform(get("/reports"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Generate Report")));
  }

  @Test
  @WithMockUser(authorities = "AI:READ")
  void aiInsightsPage_WithAiReadPermission_ShouldReturn200() throws Exception {
    mockMvc.perform(get("/ai-insights")).andExpect(status().isOk());
  }

  @Test
  @WithMockUser(authorities = "AI:READ")
  void aiInsightsPage_WithoutReportGeneratePermission_ShouldNotShowGenerateButton()
      throws Exception {
    mockMvc
        .perform(get("/ai-insights"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("AI Insights")))
        .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Generate Insights"))));
  }

  @Test
  @WithMockUser(authorities = {"AI:READ", "REPORT:GENERATE"})
  void aiInsightsPage_WithReportGeneratePermission_ShouldShowGenerateButton() throws Exception {
    mockMvc
        .perform(get("/ai-insights"))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("Generate Insights")));
  }

  @Test
  @WithMockUser
  void dashboardPage_WithAnyAuthenticatedUser_ShouldReturn200() throws Exception {
    mockMvc.perform(get("/dashboard")).andExpect(status().isOk());
  }
}
