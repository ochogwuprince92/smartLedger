package com.finance.smartLedger.reconciliation.application;

import com.finance.smartLedger.ai.application.AIInsightService;
import com.finance.smartLedger.ledger.domain.Transaction;
import com.finance.smartLedger.reconciliation.domain.MatchStatus;
import com.finance.smartLedger.reconciliation.domain.Reconciliation;
import com.finance.smartLedger.reconciliation.domain.ReconciliationItem;
import com.finance.smartLedger.reconciliation.domain.ReconciliationStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReconciliationWorkflowService {

  private final ReconciliationService reconciliationService;
  private final ReconciliationRulesEngine rulesEngine;
  private final AIInsightService aiInsightService;

  @Value("${reconciliation.approval.required-for-variance:true}")
  private boolean approvalRequiredForVariance;

  @Value("${reconciliation.approval.variance-threshold:1000.00}")
  private BigDecimal varianceThreshold;

  @Value("${reconciliation.approval.auto-approve-below:100.00}")
  private BigDecimal autoApproveBelow;

  /** Execute complete reconciliation workflow with approval process */
  @Transactional
  public Reconciliation executeWorkflow(
      UUID reconciliationId, List<Transaction> availableTransactions, String executedBy) {
    log.info("Starting reconciliation workflow for: {}", reconciliationId);

    // Step 1: Start reconciliation
    reconciliationService.startReconciliation(reconciliationId, executedBy);

    // Step 2: Auto-match items using rules engine
    Reconciliation reconciliation =
        rulesEngine.autoMatchItems(reconciliationId, availableTransactions);

    // Step 3: Detect variances
    Map<String, Object> varianceReport = rulesEngine.detectVariances(reconciliationId);
    log.info("Variance report: {}", varianceReport);

    // Step 4: Identify items requiring manual review
    List<ReconciliationItem> reviewItems =
        rulesEngine.identifyItemsForManualReview(reconciliationId);
    log.info("Items requiring manual review: {}", reviewItems.size());

    // Step 5: Check if approval is required
    boolean requiresApproval = checkApprovalRequired(reconciliation, varianceReport);

    // Step 6: Generate AI insights if there are significant variances
    if (requiresApproval || reviewItems.size() > 0) {
      generateReconciliationInsights(reconciliation, varianceReport, reviewItems, executedBy);
    }

    // Step 7: Auto-approve if conditions are met
    if (!requiresApproval && reconciliation.isBalanced()) {
      reconciliationService.completeReconciliation(reconciliationId, executedBy);
      log.info("Reconciliation auto-approved and completed: {}", reconciliationId);
    } else if (requiresApproval) {
      reconciliation.setStatus(ReconciliationStatus.PENDING_APPROVAL);
      log.info("Reconciliation requires approval: {}", reconciliationId);
    }

    return reconciliationService.findById(reconciliationId).orElseThrow();
  }

  /** Check if reconciliation requires approval based on variance and rules */
  private boolean checkApprovalRequired(
      Reconciliation reconciliation, Map<String, Object> varianceReport) {
    if (!approvalRequiredForVariance) {
      return false;
    }

    BigDecimal totalVariance = (BigDecimal) varianceReport.get("total_variance");
    if (totalVariance == null) {
      totalVariance = BigDecimal.ZERO;
    }

    // Auto-approve if variance is below threshold
    if (totalVariance.abs().compareTo(autoApproveBelow) <= 0) {
      return false;
    }

    // Require approval if variance exceeds threshold
    if (totalVariance.abs().compareTo(varianceThreshold) > 0) {
      return true;
    }

    // Require approval if not balanced
    if (!reconciliation.isBalanced()) {
      return true;
    }

    return false;
  }

  /** Generate AI-powered insights for reconciliation */
  private void generateReconciliationInsights(
      Reconciliation reconciliation,
      Map<String, Object> varianceReport,
      List<ReconciliationItem> reviewItems,
      String executedBy) {

    Map<String, Object> context = new HashMap<>();
    context.put("reconciliation_id", reconciliation.getId());
    context.put("reconciliation_number", reconciliation.getReconciliationNumber());
    context.put("variance_report", varianceReport);
    context.put("review_items_count", reviewItems.size());
    context.put("total_variance", varianceReport.get("total_variance"));
    context.put("is_balanced", reconciliation.isBalanced());
    context.put("status", reconciliation.getStatus());

    try {
      BigDecimal totalVariance = (BigDecimal) varianceReport.get("total_variance");
      if (totalVariance == null) {
        totalVariance = BigDecimal.ZERO;
      }
      
      aiInsightService.createReconciliationInsight(
          reconciliation.getId(),
          reconciliation.getReconciliationNumber(),
          "LEDGER",
          reviewItems.size(),
          0, // missingSettlements - not available in context
          0, // amountMismatches - not available in context
          0, // negativeBalances - not available in context
          0, // transactionCount - not available in context
          reconciliation.getStatus().name(),
          executedBy);
      log.info("AI reconciliation insights generated for: {}", reconciliation.getId());
    } catch (Exception e) {
      log.error("Failed to generate AI reconciliation insights", e);
    }
  }

  /** Approve reconciliation manually */
  @Transactional
  public Reconciliation approveReconciliation(
      UUID reconciliationId, String approvedBy, String comments) {
    Reconciliation reconciliation =
        reconciliationService
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    if (reconciliation.getStatus() != ReconciliationStatus.PENDING_APPROVAL) {
      throw new IllegalStateException(
          "Can only approve reconciliations in PENDING_APPROVAL status");
    }

    if (reconciliation.isBalanced()) {
      reconciliationService.completeReconciliation(reconciliationId, approvedBy);
      log.info("Reconciliation approved and completed: {} by: {}", reconciliationId, approvedBy);
    } else {
      reconciliation.markPartiallyMatched(approvedBy);
      log.info(
          "Reconciliation approved (partially matched): {} by: {}", reconciliationId, approvedBy);
    }

    return reconciliationService.findById(reconciliationId).orElseThrow();
  }

  /** Reject reconciliation */
  @Transactional
  public Reconciliation rejectReconciliation(
      UUID reconciliationId, String rejectedBy, String reason) {
    Reconciliation reconciliation =
        reconciliationService
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    if (reconciliation.getStatus() != ReconciliationStatus.PENDING_APPROVAL) {
      throw new IllegalStateException("Can only reject reconciliations in PENDING_APPROVAL status");
    }

    reconciliationService.failReconciliation(reconciliationId, rejectedBy);
    log.info("Reconciliation rejected: {} by: {} reason: {}", reconciliationId, rejectedBy, reason);

    return reconciliationService.findById(reconciliationId).orElseThrow();
  }

  /** Request additional information for reconciliation */
  @Transactional
  public Reconciliation requestAdditionalInfo(
      UUID reconciliationId, String requestedBy, String infoRequired) {
    Reconciliation reconciliation =
        reconciliationService
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    // Add info request to description or create a separate entity
    String currentDescription =
        reconciliation.getDescription() != null ? reconciliation.getDescription() : "";
    String updatedDescription =
        currentDescription
            + "\n[INFO REQUEST - "
            + LocalDateTime.now()
            + "]: "
            + infoRequired
            + " (Requested by: "
            + requestedBy
            + ")";

    reconciliation.setDescription(updatedDescription);
    reconciliation.setStatus(ReconciliationStatus.PENDING_APPROVAL);

    log.info("Additional info requested for reconciliation: {}", reconciliationId);

    return reconciliationService.findById(reconciliationId).orElseThrow();
  }

  /** Escalate reconciliation for higher-level approval */
  @Transactional
  public Reconciliation escalateReconciliation(
      UUID reconciliationId, String escalatedBy, String reason) {
    Reconciliation reconciliation =
        reconciliationService
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    // In a real implementation, this would trigger notification to higher-level approvers
    String currentDescription =
        reconciliation.getDescription() != null ? reconciliation.getDescription() : "";
    String updatedDescription =
        currentDescription
            + "\n[ESCALATED - "
            + LocalDateTime.now()
            + "]: "
            + reason
            + " (Escalated by: "
            + escalatedBy
            + ")";

    reconciliation.setDescription(updatedDescription);
    reconciliation.setStatus(ReconciliationStatus.PENDING_APPROVAL);

    log.info(
        "Reconciliation escalated: {} by: {} reason: {}", reconciliationId, escalatedBy, reason);

    return reconciliationService.findById(reconciliationId).orElseThrow();
  }

  /** Get workflow status for a reconciliation */
  public Map<String, Object> getWorkflowStatus(UUID reconciliationId) {
    Reconciliation reconciliation =
        reconciliationService
            .findById(reconciliationId)
            .orElseThrow(() -> new IllegalArgumentException("Reconciliation not found"));

    List<ReconciliationItem> items =
        reconciliationService.findItemsByReconciliationId(reconciliationId);
    Map<String, Object> varianceReport = rulesEngine.detectVariances(reconciliationId);
    List<ReconciliationItem> reviewItems =
        rulesEngine.identifyItemsForManualReview(reconciliationId);

    Map<String, Object> status = new HashMap<>();
    status.put("reconciliation_id", reconciliationId);
    status.put("reconciliation_number", reconciliation.getReconciliationNumber());
    status.put("status", reconciliation.getStatus());
    status.put("is_balanced", reconciliation.isBalanced());
    status.put("total_variance", varianceReport.get("total_variance"));
    status.put("variance_count", varianceReport.get("variance_count"));
    status.put("total_items", items.size());
    status.put(
        "matched_items",
        items.stream().filter(i -> i.getMatchStatus() == MatchStatus.MATCHED).count());
    status.put(
        "unmatched_items",
        items.stream().filter(i -> i.getMatchStatus() == MatchStatus.UNMATCHED).count());
    status.put(
        "suspense_items",
        items.stream().filter(i -> i.getMatchStatus() == MatchStatus.SUSPENSE).count());
    status.put("requires_manual_review", reviewItems.size() > 0);
    status.put("review_items_count", reviewItems.size());
    status.put(
        "requires_approval", reconciliation.getStatus() == ReconciliationStatus.PENDING_APPROVAL);

    return status;
  }
}
