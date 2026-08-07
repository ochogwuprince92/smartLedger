package com.finance.smartLedger.ai.presentation;

import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.InsightStatus;
import com.finance.smartLedger.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai-insights")
@RequiredArgsConstructor
@Tag(name = "AI Insights", description = "AI Insight management endpoints")
public class AIInsightController {

  private final com.finance.smartLedger.ai.application.AIInsightService aiInsightService;

  @PostMapping("/insights")
  @Operation(summary = "Create AI insight", description = "Creates a new AI insight")
  @PreAuthorize("hasAuthority('AI_INSIGHT:CREATE')")
  public ResponseEntity<ApiResponse<AIInsight>> createInsight(
      @RequestBody @Valid CreateAIInsightRequest request) {
    AIInsight insight =
        aiInsightService.createInsight(
            request.insightType(),
            request.title(),
            request.description(),
            request.severity(),
            request.recommendation(),
            request.confidenceScore(),
            request.dataSource(),
            request.referenceDate(),
            request.metadata(),
            request.isActionable(),
            "system");
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success("AI insight created successfully", insight));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get AI insight by ID", description = "Retrieves an AI insight by its ID")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<AIInsight>> getInsight(@PathVariable UUID id) {
    return aiInsightService
        .findById(id)
        .map(insight -> ResponseEntity.ok(ApiResponse.success(insight)))
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/type/{insightType}")
  @Operation(
      summary = "Get insights by type",
      description = "Retrieves all insights of a specific type")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getInsightsByType(
      @PathVariable String insightType) {
    List<AIInsight> insights = aiInsightService.findByInsightType(insightType);
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @GetMapping("/status/{status}")
  @Operation(
      summary = "Get insights by status",
      description = "Retrieves all insights with a specific status")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getInsightsByStatus(
      @PathVariable InsightStatus status) {
    List<AIInsight> insights = aiInsightService.findByStatus(status);
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @GetMapping("/severity/{severity}")
  @Operation(
      summary = "Get insights by severity",
      description = "Retrieves all insights with a specific severity")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getInsightsBySeverity(
      @PathVariable String severity) {
    List<AIInsight> insights = aiInsightService.findBySeverity(severity);
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @GetMapping("/source/{dataSource}")
  @Operation(
      summary = "Get insights by data source",
      description = "Retrieves all insights from a specific data source")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getInsightsByDataSource(
      @PathVariable String dataSource) {
    List<AIInsight> insights = aiInsightService.findByDataSource(dataSource);
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @GetMapping("/pending")
  @Operation(
      summary = "Get pending insights",
      description = "Retrieves all pending insights that haven't been reviewed")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getPendingInsights() {
    List<AIInsight> insights = aiInsightService.findPendingInsights();
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @GetMapping("/actionable")
  @Operation(
      summary = "Get actionable insights",
      description = "Retrieves all actionable insights that haven't been resolved")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getActionableInsights() {
    List<AIInsight> insights = aiInsightService.findActionableInsights();
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @GetMapping
  @Operation(summary = "Get all insights", description = "Retrieves all AI insights")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getAllInsights() {
    List<AIInsight> insights = aiInsightService.findAllInsights();
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update AI insight", description = "Updates an existing AI insight")
  @PreAuthorize("hasAuthority('AI_INSIGHT:UPDATE')")
  public ResponseEntity<ApiResponse<AIInsight>> updateInsight(
      @PathVariable UUID id, @RequestBody @Valid UpdateAIInsightRequest request) {
    AIInsight insight =
        aiInsightService.updateInsight(
            id,
            request.title(),
            request.description(),
            request.recommendation(),
            request.severity(),
            request.confidenceScore(),
            "system");
    return ResponseEntity.ok(ApiResponse.success("AI insight updated successfully", insight));
  }

  @PostMapping("/{id}/review")
  @Operation(summary = "Mark insight as reviewed", description = "Marks an insight as reviewed")
  @PreAuthorize("hasAuthority('AI_INSIGHT:REVIEW')")
  public ResponseEntity<ApiResponse<Void>> markAsReviewed(@PathVariable UUID id) {
    aiInsightService.markAsReviewed(id, "system");
    return ResponseEntity.ok(ApiResponse.success("AI insight marked as reviewed", null));
  }

  @PostMapping("/{id}/resolve")
  @Operation(summary = "Mark insight as resolved", description = "Marks an insight as resolved")
  @PreAuthorize("hasAuthority('AI_INSIGHT:RESOLVE')")
  public ResponseEntity<ApiResponse<Void>> markAsResolved(@PathVariable UUID id) {
    aiInsightService.markAsResolved(id, "system");
    return ResponseEntity.ok(ApiResponse.success("AI insight marked as resolved", null));
  }

  @PostMapping("/{id}/dismiss")
  @Operation(summary = "Dismiss insight", description = "Dismisses an insight")
  @PreAuthorize("hasAuthority('AI_INSIGHT:DISMISS')")
  public ResponseEntity<ApiResponse<Void>> dismissInsight(@PathVariable UUID id) {
    aiInsightService.dismissInsight(id, "system");
    return ResponseEntity.ok(ApiResponse.success("AI insight dismissed", null));
  }

  @PostMapping("/generate/anomalies")
  @Operation(
      summary = "Generate anomaly insights",
      description = "Generates insights based on anomaly detection")
  @PreAuthorize("hasAuthority('AI_INSIGHT:GENERATE')")
  public ResponseEntity<ApiResponse<Void>> generateAnomalyInsights() {
    aiInsightService.generateAnomalyInsights("system");
    return ResponseEntity.ok(ApiResponse.success("Anomaly insights generation initiated", null));
  }

  @PostMapping("/generate/cashflow")
  @Operation(
      summary = "Generate cash flow forecast insights",
      description = "Generates insights based on cash flow forecasting")
  @PreAuthorize("hasAuthority('AI_INSIGHT:GENERATE')")
  public ResponseEntity<ApiResponse<Void>> generateCashFlowForecastInsights() {
    aiInsightService.generateCashFlowForecastInsights("system");
    return ResponseEntity.ok(
        ApiResponse.success("Cash flow forecast insights generation initiated", null));
  }

  @PostMapping("/generate/reconciliation")
  @Operation(
      summary = "Generate reconciliation insights",
      description = "Generates insights based on reconciliation analysis")
  @PreAuthorize("hasAuthority('AI_INSIGHT:GENERATE')")
  public ResponseEntity<ApiResponse<Void>> generateReconciliationInsights() {
    aiInsightService.generateReconciliationInsights("system");
    return ResponseEntity.ok(
        ApiResponse.success("Reconciliation insights generation initiated", null));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete AI insight", description = "Deletes an AI insight")
  @PreAuthorize("hasAuthority('AI_INSIGHT:DELETE')")
  public ResponseEntity<ApiResponse<Void>> deleteInsight(@PathVariable UUID id) {
    aiInsightService.deleteInsight(id);
    return ResponseEntity.ok(ApiResponse.success("AI insight deleted successfully", null));
  }

  record CreateAIInsightRequest(
      String insightType,
      String title,
      String description,
      String severity,
      String recommendation,
      Double confidenceScore,
      String dataSource,
      @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate referenceDate,
      String metadata,
      Boolean isActionable) {}

  record UpdateAIInsightRequest(
      String title,
      String description,
      String recommendation,
      String severity,
      Double confidenceScore) {}
}
