package com.finance.smartLedger.ai.infrastructure.n8n;

import com.finance.smartLedger.ai.application.dto.AIInsightRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class N8nClientImpl implements N8nClient {

  @Qualifier("n8nRestTemplate")
  private final RestTemplate restTemplate;

  @Value("${n8n.base-url}")
  private String n8nBaseUrl;

  @Value("${n8n.webhook-path}")
  private String webhookPath;

  @Override
  public void requestInsight(AIInsightRequest request) {
    try {
      String url = n8nBaseUrl + webhookPath;
      
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_JSON);
      
      HttpEntity<AIInsightRequest> entity = new HttpEntity<>(request, headers);
      
      log.info("Sending AI insight request to n8n: requestId={}, reconciliationId={}", 
          request.getRequestId(), request.getReconciliationId());
      
      restTemplate.postForObject(url, entity, String.class);
      
      log.info("AI insight request sent successfully to n8n: requestId={}", request.getRequestId());
      
    } catch (Exception e) {
      log.error("Failed to send AI insight request to n8n: requestId={}", request.getRequestId(), e);
      throw new RuntimeException("Failed to send AI insight request to n8n", e);
    }
  }

  @Override
  public boolean isHealthy() {
    try {
      String healthUrl = n8nBaseUrl + "/healthz";
      restTemplate.getForObject(healthUrl, String.class);
      return true;
    } catch (Exception e) {
      log.warn("n8n health check failed", e);
      return false;
    }
  }
}
