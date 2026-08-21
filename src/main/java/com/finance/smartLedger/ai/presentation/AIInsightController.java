package com.finance.smartLedger.ai.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.finance.smartLedger.ai.application.dto.AICallbackRequest;
import com.finance.smartLedger.ai.application.dto.AICallbackResponse;
import com.finance.smartLedger.ai.domain.AIInsight;
import com.finance.smartLedger.ai.domain.AIInsightType;
import com.finance.smartLedger.ai.domain.InsightStatus;
import com.finance.smartLedger.ai.domain.RiskLevel;
import com.finance.smartLedger.shared.dto.ApiResponse;
import com.finance.smartLedger.shared.security.HmacSignatureUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ai-insights")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "AI Insights", description = "AI Insight management endpoints")
public class AIInsightController {

  private final com.finance.smartLedger.ai.application.AIInsightService aiInsightService;
  private final HmacSignatureUtil hmacSignatureUtil;
  private final ObjectMapper objectMapper;

  @Value("${n8n.callback-secret}")
  private String callbackSecret;

  @PostMapping("/callback")
  @Operation(summary = "AI insight callback", description = "Callback endpoint for n8n to return AI insights")
  public ResponseEntity<AICallbackResponse> handleCallback(@RequestBody AICallbackRequest request) {
    try {
      // Verify HMAC signature - exclude signature field from payload
      AICallbackRequest requestForSignature = new AICallbackRequest(
          request.getRequestId(),
          request.getReconciliationId(),
          request.getRiskLevel(),
          request.getSummary(),
          request.getRootCause(),
          request.getRecommendations(),
          null // Exclude signature from signature calculation
      );
      String payload = objectMapper.writeValueAsString(requestForSignature);
      String expectedSignature = hmacSignatureUtil.calculateSignature(payload, callbackSecret);
      
      log.info("AI Callback - requestId: {}", request.getRequestId());
      log.info("Payload (without signature): {}", payload);
      log.info("Received signature: {}", request.getSignature());
      log.info("Expected signature: {}", expectedSignature);
      
      if (!hmacSignatureUtil.verifySignature(payload, request.getSignature(), callbackSecret)) {
        log.warn("Invalid HMAC signature for callback: requestId={}", request.getRequestId());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(AICallbackResponse.builder().success(false).message("Invalid signature").build());
      }

      aiInsightService.handleCallback(request);
      return ResponseEntity.ok(AICallbackResponse.builder().success(true).message("Callback processed").build());

    } catch (Exception e) {
      log.error("Failed to process AI insight callback: requestId={}", request.getRequestId(), e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(AICallbackResponse.builder().success(false).message("Processing failed").build());
    }
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
      @PathVariable AIInsightType insightType) {
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

  @GetMapping("/risk/{riskLevel}")
  @Operation(
      summary = "Get insights by risk level",
      description = "Retrieves all insights with a specific risk level")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getInsightsByRiskLevel(
      @PathVariable RiskLevel riskLevel) {
    List<AIInsight> insights = aiInsightService.findByRiskLevel(riskLevel);
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @GetMapping("/reconciliation/{reconciliationId}")
  @Operation(
      summary = "Get insights by reconciliation",
      description = "Retrieves all insights for a specific reconciliation")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getInsightsByReconciliation(
      @PathVariable UUID reconciliationId) {
    List<AIInsight> insights = aiInsightService.findByReconciliationId(reconciliationId);
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @GetMapping("/failed")
  @Operation(
      summary = "Get failed insights",
      description = "Retrieves all failed AI insights")
  @PreAuthorize("hasAuthority('AI_INSIGHT:READ')")
  public ResponseEntity<ApiResponse<List<AIInsight>>> getFailedInsights() {
    List<AIInsight> insights = aiInsightService.findFailedInsights();
    return ResponseEntity.ok(ApiResponse.success(insights));
  }

  @PostMapping("/retry-failed")
  @Operation(
      summary = "Retry failed insights",
      description = "Retries all failed AI insights that are within retry limits")
  @PreAuthorize("hasAuthority('AI_INSIGHT:RETRY')")
  public ResponseEntity<ApiResponse<Void>> retryFailedInsights() {
    aiInsightService.retryFailedInsights();
    return ResponseEntity.ok(ApiResponse.success("Failed insights retry initiated", null));
  }
}
